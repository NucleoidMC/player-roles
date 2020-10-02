package net.gegy1000.roles;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.gegy1000.roles.command.RoleCommand;
import net.gegy1000.roles.override.command.CommandPermissionEvaluator;
import net.gegy1000.roles.override.command.CommandRequirementHooks;
import net.gegy1000.roles.override.command.CommandTestContext;
import net.gegy1000.roles.override.command.MatchableCommand;
import net.gegy1000.roles.override.command.PermissionResult;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RolesInitializer implements ModInitializer {
    public static final String ID = "player-roles";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    private static boolean registered;

    @Override
    public void onInitialize() {
        RoleConfiguration.setup();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            RoleCommand.register(dispatcher);
        });

        // cursed solution to make sure we run our handler after everything else
        // worldedit registers commands in the server started listener, so we need to override that
        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            if (registered) {
                return;
            }
            registered = true;

            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                this.hookCommands(server.getCommandManager().getDispatcher());
            });
        });
    }

    private void hookCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        try {
            CommandRequirementHooks<ServerCommandSource> hooks = CommandRequirementHooks.tryCreate((nodes, existing) -> {
                MatchableCommand command = MatchableCommand.compile(nodes);

                return source -> {
                    PermissionResult result = CommandPermissionEvaluator.canUseCommand(source, command);
                    if (result == PermissionResult.ALLOW) return true;
                    if (result == PermissionResult.DENY) return false;
                    if (result == PermissionResult.HIDDEN) return !CommandTestContext.isSuggesting();

                    return existing.test(source);
                };
            });

            hooks.hookAll(dispatcher);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to hook command requirements", e);
        }
    }
}
