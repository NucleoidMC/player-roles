package dev.gegy.roles.override.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.RoleLookup;
import dev.gegy.roles.api.override.OverrideResult;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;

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
                hookCommands(server.getCommandManager().getDispatcher());
            });
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            hookCommands(server.getCommandManager().getDispatcher());
        });
    }

    private static void hookCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        var hooks = CommandRequirementHooks.<ServerCommandSource>create((nodes, parent) -> {
            var command = MatchableCommand.compile(nodes);

            return source -> {
                return switch (canUseCommand(source, command)) {
                    case ALLOW -> true;
                    case DENY -> false;
                    case HIDDEN -> !CommandTestContext.isSuggesting();
                    default -> parent.test(source);
                };
            };
        });

        hooks.applyTo(dispatcher);
    }

    private static OverrideResult canUseCommand(ServerCommandSource source, MatchableCommand command) {
        if (doesBypassPermissions(source)) return OverrideResult.PASS;

        var roles = RoleLookup.bySource(source);
        if (roles != null) {
            return roles.overrides().test(PlayerRoles.COMMANDS, m -> m.test(command));
        }

        return OverrideResult.PASS;
    }

    public static boolean doesBypassPermissions(ServerCommandSource source) {
        return source.hasPermissionLevel(4);
    }

    public OverrideResult test(MatchableCommand command) {
        return this.rules.test(command);
    }
}
