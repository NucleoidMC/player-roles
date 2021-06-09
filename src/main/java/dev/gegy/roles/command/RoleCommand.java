package dev.gegy.roles.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.gegy.roles.Role;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.override.command.CommandOverride;
import dev.gegy.roles.store.PlayerRoleManager;
import dev.gegy.roles.store.PlayerRoleSet;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RoleCommand {
    public static final DynamicCommandExceptionType ROLE_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
        return new TranslatableText("Role with name '%s' was not found!", arg);
    });

    public static final SimpleCommandExceptionType ROLE_POWER_TOO_LOW = new SimpleCommandExceptionType(
            new LiteralText("You do not have sufficient power to manage this role")
    );

    public static final SimpleCommandExceptionType TOO_MANY_SELECTED = new SimpleCommandExceptionType(
            new LiteralText("Too many players selected!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("role")
                .requires(s -> s.hasPermissionLevel(4))
                .then(literal("assign")
                    .then(argument("targets", GameProfileArgumentType.gameProfile())
                    .then(argument("role", StringArgumentType.word()).suggests(roleSuggestions())
                    .executes(ctx -> {
                        var source = ctx.getSource();
                        var targets = GameProfileArgumentType.getProfileArgument(ctx, "targets");
                        var roleName = StringArgumentType.getString(ctx, "role");
                        return updateRoles(source, targets, roleName, PlayerRoleSet::add, "'%s' assigned to %s players");
                    })
                )))
                .then(literal("remove")
                    .then(argument("targets", GameProfileArgumentType.gameProfile())
                    .then(argument("role", StringArgumentType.word()).suggests(roleSuggestions())
                    .executes(ctx -> {
                        var source = ctx.getSource();
                        var targets = GameProfileArgumentType.getProfileArgument(ctx, "targets");
                        var roleName = StringArgumentType.getString(ctx, "role");
                        return updateRoles(source, targets, roleName, PlayerRoleSet::remove, "'%s' removed from %s players");
                    })
                )))
                .then(literal("list")
                    .then(argument("target", GameProfileArgumentType.gameProfile()).executes(ctx -> {
                        var source = ctx.getSource();
                        var gameProfiles = GameProfileArgumentType.getProfileArgument(ctx, "target");
                        if (gameProfiles.size() != 1) {
                            throw TOO_MANY_SELECTED.create();
                        }
                        return listRoles(source, gameProfiles.iterator().next());
                    }))
                )
                .then(literal("reload").executes(ctx -> reloadRoles(ctx.getSource())))
        );
    }
    // @formatter:on

    private static int updateRoles(ServerCommandSource source, Collection<GameProfile> players, String roleName, BiPredicate<PlayerRoleSet, Role> apply, String success) throws CommandSyntaxException {
        var role = getRole(roleName);
        assertHasPower(source, role);

        var roleManager = PlayerRoleManager.get();
        MinecraftServer server = source.getMinecraftServer();

        int count = 0;
        for (var player : players) {
            boolean applied = roleManager.updateRoles(server, player.getId(), roles -> apply.test(roles, role));
            if (applied) {
                count++;
            }
        }

        source.sendFeedback(new TranslatableText(success, roleName, count), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int listRoles(ServerCommandSource source, GameProfile player) {
        var roleManager = PlayerRoleManager.get();
        var server = source.getMinecraftServer();

        var roles = roleManager.peekRoles(server, player.getId())
                .stream().collect(Collectors.toList());
        var rolesComponent = Texts.join(roles, role -> new LiteralText(role.getName()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        source.sendFeedback(new TranslatableText("Found %s roles on player: %s", roles.size(), rolesComponent), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int reloadRoles(ServerCommandSource source) {
        var server = source.getMinecraftServer();

        server.execute(() -> {
            var errors = PlayerRolesConfig.setup();

            var players = server.getPlayerManager().getPlayerList();
            for (var entity : players) {
                if (entity instanceof PlayerRoleSource roleSource) {
                    roleSource.notifyPlayerRoleReload();
                }
            }

            if (errors.isEmpty()) {
                source.sendFeedback(new TranslatableText("Role configuration successfully reloaded"), false);
            } else {
                MutableText errorFeedback = new LiteralText("Failed to reload roles configuration!");
                for (String error : errors) {
                    errorFeedback = errorFeedback.append("\n - " + error);
                }
                source.sendError(errorFeedback);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void assertHasPower(ServerCommandSource source, Role role) throws CommandSyntaxException {
        if (CommandOverride.doesBypassPermissions(source)) return;

        int highestPower = getHighestPowerLevel(source);
        if (highestPower <= role.getLevel()) {
            throw ROLE_POWER_TOO_LOW.create();
        }
    }

    private static Role getRole(String roleName) throws CommandSyntaxException {
        var role = PlayerRolesConfig.get().get(roleName);
        if (role == null) throw ROLE_NOT_FOUND.create(roleName);
        return role;
    }

    private static SuggestionProvider<ServerCommandSource> roleSuggestions() {
        return (ctx, builder) -> {
            int highestPowerLevel = getHighestPowerLevel(ctx.getSource());
            return CommandSource.suggestMatching(
                    PlayerRolesConfig.get().stream()
                            .filter(role -> role.getLevel() < highestPowerLevel)
                            .map(Role::getName),
                    builder
            );
        };
    }

    private static int getHighestPowerLevel(ServerCommandSource source) {
        var entity = source.getEntity();
        if (entity == null || CommandOverride.doesBypassPermissions(source)) return Integer.MAX_VALUE;

        if (entity instanceof PlayerRoleSource roleSource) {
            var roles = roleSource.getPlayerRoles();
            var levels = roles.stream().mapToInt(Role::getLevel);
            return levels.max().orElse(0);
        }

        return 0;
    }
}
