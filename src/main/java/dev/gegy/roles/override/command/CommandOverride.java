package dev.gegy.roles.override.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.override.RoleOverrideResult;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

public record CommandOverride(CommandOverrideRules rules) {
    public static final Codec<CommandOverride> CODEC = CommandOverrideRules.CODEC.xmap(
            CommandOverride::new,
            override -> override.rules
    );

    private static boolean registered;

    public static void initialize() {
        // cursed solution to make sure we run our handler after everything else
        // worldedit registers commands in the server started listener, so we need to override that
        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            if (registered) {
                return;
            }
            registered = true;

            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                hookCommands(server.getCommands().getDispatcher());
            });
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            hookCommands(server.getCommands().getDispatcher());
        });
    }

    private static void hookCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        try {
            var hooks = CommandRequirementHooks.<CommandSourceStack>tryCreate((nodes, parent) -> {
                var command = MatchableCommand.compile(nodes);

                return source -> switch (canUseCommand(source, command)) {
                    case ALLOW -> true;
                    case DENY -> false;
                    case HIDDEN -> !CommandTestContext.isSuggesting();
                    default -> parent.test(source);
                };
            });

            hooks.applyTo(dispatcher);
        } catch (ReflectiveOperationException e) {
            PlayerRoles.LOGGER.error("Failed to reflect into command requirements!", e);
        }
    }

    private static RoleOverrideResult canUseCommand(CommandSourceStack source, MatchableCommand command) {
        if (doesBypassPermissions(source)) {
            return RoleOverrideResult.PASS;
        }

        var roles = PlayerRolesApi.lookup().bySource(source);
        return roles.overrides().test(PlayerRoles.COMMANDS, m -> m.test(command));
    }

    public static boolean doesBypassPermissions(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_OWNER);
    }

    public RoleOverrideResult test(MatchableCommand command) {
        return this.rules.test(command);
    }
}
