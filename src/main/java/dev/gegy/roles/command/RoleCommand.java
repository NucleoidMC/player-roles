package dev.gegy.roles.command;

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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiPredicate;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class RoleCommand {
    public static final DynamicCommandExceptionType ROLE_NOT_FOUND = new DynamicCommandExceptionType(arg ->
            Component.translatableEscape("Role with name '%s' was not found!", arg)
    );

    public static final SimpleCommandExceptionType ROLE_POWER_TOO_LOW = new SimpleCommandExceptionType(
            Component.literal("You do not have sufficient power to manage this role")
    );

    public static final SimpleCommandExceptionType TOO_MANY_SELECTED = new SimpleCommandExceptionType(
            Component.literal("Too many players selected!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("role")
                .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                .then(literal("assign")
                    .then(argument("targets", GameProfileArgument.gameProfile())
                    .then(argument("role", StringArgumentType.word()).suggests(roleSuggestions())
                    .executes(ctx -> {
                        var source = ctx.getSource();
                        var targets = GameProfileArgument.getGameProfiles(ctx, "targets");
                        var roleName = StringArgumentType.getString(ctx, "role");
                        return updateRoles(source, targets, roleName, PlayerRoleSet::add, "'%s' assigned to %s players");
                    })
                )))
                .then(literal("remove")
                    .then(argument("targets", GameProfileArgument.gameProfile())
                    .then(argument("role", StringArgumentType.word()).suggests(roleSuggestions())
                    .executes(ctx -> {
                        var source = ctx.getSource();
                        var targets = GameProfileArgument.getGameProfiles(ctx, "targets");
                        var roleName = StringArgumentType.getString(ctx, "role");
                        return updateRoles(source, targets, roleName, PlayerRoleSet::remove, "'%s' removed from %s players");
                    })
                )))
                .then(literal("list")
                    .then(argument("target", GameProfileArgument.gameProfile()).executes(ctx -> {
                        var source = ctx.getSource();
                        var gameProfiles = GameProfileArgument.getGameProfiles(ctx, "target");
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

    private static int updateRoles(CommandSourceStack source, Collection<NameAndId> players, String roleName, BiPredicate<PlayerRoleSet, SimpleRole> apply, String success) throws CommandSyntaxException {
        var role = getRole(roleName);
        requireHasPower(source, role);

        var roleManager = PlayerRoleManager.get();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (var player : players) {
            boolean applied = roleManager.updateRoles(server, player.id(), roles -> apply.test(roles, role));
            if (applied) {
                count++;
            }
        }

        int finalCount = count;
        source.sendSuccess(() -> Component.translatable(success, roleName, finalCount), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int listRoles(CommandSourceStack source, NameAndId player) {
        var roleManager = PlayerRoleManager.get();
        var server = source.getServer();

        var roles = roleManager.peekRoles(server, player.id()).stream().toList();
        source.sendSuccess(() -> {
            var rolesComponent = ComponentUtils.formatList(roles, role -> Component.literal(role.getId()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
            return Component.translatable("Found %s roles on player: %s", roles.size(), rolesComponent);
        }, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int reloadRoles(CommandSourceStack source) {
        var server = source.getServer();

        server.execute(() -> {
            var errors = PlayerRolesConfig.setup();

            var roleManager = PlayerRoleManager.get();
            roleManager.onRoleReload(server, PlayerRolesConfig.get());

            if (errors.isEmpty()) {
                source.sendSuccess(() -> Component.literal("Role configuration successfully reloaded"), false);
            } else {
                MutableComponent errorFeedback = Component.literal("Failed to reload roles configuration!");
                for (String error : errors) {
                    errorFeedback = errorFeedback.append("\n - " + error);
                }
                source.sendFailure(errorFeedback);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void requireHasPower(CommandSourceStack source, SimpleRole role) throws CommandSyntaxException {
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

    private static SuggestionProvider<CommandSourceStack> roleSuggestions() {
        return (ctx, builder) -> {
            var source = ctx.getSource();

            boolean admin = hasAdminPower(source);
            var highestRole = getHighestRole(source);
            Comparator<Role> comparator = Comparator.nullsLast(Comparator.naturalOrder());

            return SharedSuggestionProvider.suggest(
                    PlayerRolesConfig.get().stream()
                            .filter(role -> admin || comparator.compare(role, highestRole) > 0)
                            .map(Role::getId),
                    builder
            );
        };
    }

    @Nullable
    private static Role getHighestRole(CommandSourceStack source) {
        return PlayerRolesApi.lookup().bySource(source).stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private static boolean hasAdminPower(CommandSourceStack source) {
        return source.getEntity() == null || CommandOverride.doesBypassPermissions(source);
    }
}
