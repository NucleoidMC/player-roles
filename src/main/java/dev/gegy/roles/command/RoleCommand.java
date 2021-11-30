package dev.gegy.roles.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.gegy.roles.SimpleRole;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.Role;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.override.command.CommandOverride;
import dev.gegy.roles.store.PlayerRoleManager;
import dev.gegy.roles.store.PlayerRoleSet;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
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

    private static int updateRoles(ServerCommandSource source, Collection<GameProfile> players, String roleName, BiPredicate<PlayerRoleSet, SimpleRole> apply, String success) throws CommandSyntaxException {
        var role = getRole(roleName);
        requireHasPower(source, role);

        var roleManager = PlayerRoleManager.get();
        MinecraftServer server = source.getServer();

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
        var server = source.getServer();

        var roles = roleManager.peekRoles(server, player.getId())
                .stream().collect(Collectors.toList());
        var rolesComponent = Texts.join(roles, role -> new LiteralText(role.getId()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        source.sendFeedback(new TranslatableText("Found %s roles on player: %s", roles.size(), rolesComponent), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int reloadRoles(ServerCommandSource source) {
        var server = source.getServer();

        server.execute(() -> {
            var errors = PlayerRolesConfig.setup();

            var roleManager = PlayerRoleManager.get();
            roleManager.onRoleReload(server, PlayerRolesConfig.get());

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

    private static void requireHasPower(ServerCommandSource source, SimpleRole role) throws CommandSyntaxException {
        if (hasAdminPower(source)) {
            return;
        }

        var highestRole = getHighestRole(source);
        if (highestRole == null || role.compareTo(highestRole) <= 0) {
            throw ROLE_POWER_TOO_LOW.create();
        }
    }

    private static SimpleRole getRole(String roleName) throws CommandSyntaxException {
        var role = PlayerRolesConfig.get().get(roleName);
        if (role == null) throw ROLE_NOT_FOUND.create(roleName);
        return role;
    }

    private static SuggestionProvider<ServerCommandSource> roleSuggestions() {
        return (ctx, builder) -> {
            var source = ctx.getSource();

            boolean admin = hasAdminPower(source);
            var highestRole = getHighestRole(source);
            Comparator<Role> comparator = Comparator.nullsLast(Comparator.naturalOrder());

            return CommandSource.suggestMatching(
                    PlayerRolesConfig.get().stream()
                            .filter(role -> admin || comparator.compare(role, highestRole) < 0)
                            .map(Role::getId),
                    builder
            );
        };
    }

    @Nullable
    private static Role getHighestRole(ServerCommandSource source) {
        return PlayerRolesApi.lookup().bySource(source).stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private static boolean hasAdminPower(ServerCommandSource source) {
        return source.getEntity() == null || CommandOverride.doesBypassPermissions(source);
    }
}
