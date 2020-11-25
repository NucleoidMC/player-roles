package dev.gegy.roles.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.gegy.roles.Role;
import dev.gegy.roles.RoleStorage;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.RoleConfiguration;
import dev.gegy.roles.api.HasRoles;
import dev.gegy.roles.override.command.CommandPermissionEvaluator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RoleCommand {
    public static final DynamicCommandExceptionType ROLE_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
        return new TranslatableText("Role with name '%s' was not found!", arg);
    });

    public static final SimpleCommandExceptionType ROLE_POWER_TOO_LOW = new SimpleCommandExceptionType(
            new LiteralText("You do not have sufficient power to manage this role")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("role")
                .requires(s -> s.hasPermissionLevel(4))
                .then(literal("assign")
                    .then(argument("targets", EntityArgumentType.players())
                    .then(argument("role", StringArgumentType.word()).suggests(roleSuggestions())
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(ctx, "targets");
                        String roleName = StringArgumentType.getString(ctx, "role");
                        return updateRoles(source, targets, roleName, RoleStorage::add, "'%s' assigned to %s players");
                }))))
                .then(literal("remove")
                    .then(argument("targets", EntityArgumentType.players())
                    .then(argument("role", StringArgumentType.word()).suggests(roleSuggestions())
                    .executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(ctx, "targets");
                        String roleName = StringArgumentType.getString(ctx, "role");
                        return updateRoles(source, targets, roleName, RoleStorage::remove, "'%s' removed from %s players");
                    }))))
                .then(literal("list")
                    .then(argument("target", EntityArgumentType.player()).executes(ctx -> {
                        ServerCommandSource source = ctx.getSource();
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                        return listRoles(source, target);
                    }))
                )
                .then(literal("reload").executes(ctx -> reloadRoles(ctx.getSource())))
        );
    }
    // @formatter:on

    private static int updateRoles(ServerCommandSource source, Collection<ServerPlayerEntity> players, String roleName, BiPredicate<RoleStorage, Role> apply, String success) throws CommandSyntaxException {
        Role role = getRole(roleName);
        assertHasPower(source, role);

        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (player instanceof HasRoles) {
                RoleStorage roles = ((HasRoles) player).getRoles();
                if (apply.test(roles, role)) {
                    count++;
                }
            }
        }

        source.sendFeedback(new TranslatableText(success, roleName, count), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listRoles(ServerCommandSource source, ServerPlayerEntity player) {
        if (player instanceof HasRoles) {
            Collection<Role> roles = ((HasRoles) player).getRoles()
                    .stream().collect(Collectors.toList());
            Text rolesComponent = Texts.join(roles, role -> new LiteralText(role.getName()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            source.sendFeedback(new TranslatableText("Found %s roles on player: %s", roles.size(), rolesComponent), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int reloadRoles(ServerCommandSource source) {
        MinecraftServer server = source.getMinecraftServer();

        server.execute(() -> {
            RoleConfiguration.setup();

            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
            for (ServerPlayerEntity entity : players) {
                if (entity instanceof HasRoles) {
                    RoleStorage roles = ((HasRoles) entity).getRoles();
                    roles.notifyReload();
                }
            }

            source.sendFeedback(new TranslatableText("Role configuration successfully reloaded"), false);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void assertHasPower(ServerCommandSource source, Role role) throws CommandSyntaxException {
        if (CommandPermissionEvaluator.doesBypassPermissions(source)) return;

        int highestPower = getHighestPowerLevel(source);
        if (highestPower <= role.getLevel()) {
            throw ROLE_POWER_TOO_LOW.create();
        }
    }

    private static Role getRole(String roleName) throws CommandSyntaxException {
        Role role = RoleConfiguration.get().get(roleName);
        if (role == null) throw ROLE_NOT_FOUND.create(roleName);
        return role;
    }

    private static SuggestionProvider<ServerCommandSource> roleSuggestions() {
        return (ctx, builder) -> {
            int highestPowerLevel = getHighestPowerLevel(ctx.getSource());
            return CommandSource.suggestMatching(
                    RoleConfiguration.get().stream()
                            .filter(role -> role.getLevel() < highestPowerLevel)
                            .map(Role::getName),
                    builder
            );
        };
    }

    private static int getHighestPowerLevel(ServerCommandSource source) {
        Entity entity = source.getEntity();
        if (entity == null || CommandPermissionEvaluator.doesBypassPermissions(source)) return Integer.MAX_VALUE;

        if (entity instanceof HasRoles) {
            RoleReader roles = ((HasRoles) entity).getRoles();
            IntStream levels = roles.stream().mapToInt(Role::getLevel);
            return levels.max().orElse(0);
        }

        return 0;
    }
}
