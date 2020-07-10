package net.gegy1000.roles;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.gegy1000.roles.command.RoleCommand;
import net.gegy1000.roles.override.command.CommandPermEvaluator;
import net.gegy1000.roles.override.command.CommandRequirementHooks;
import net.gegy1000.roles.override.command.MatchableCommand;
import net.gegy1000.roles.override.command.PermissionResult;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RolesInitializer implements ModInitializer {
    public static final String ID = "player-roles";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        RoleConfiguration.setup();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            RoleCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.submit(() -> this.hookCommands(server.getCommandManager().getDispatcher()));
        });
    }

    private void hookCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        try {
            CommandRequirementHooks<ServerCommandSource> hooks = CommandRequirementHooks.tryCreate((nodes, existing) -> {
                MatchableCommand command = MatchableCommand.compile(nodes);

                return source -> {
                    PermissionResult result = CommandPermEvaluator.canUseCommand(source, command);
                    if (result == PermissionResult.ALLOW) return true;
                    if (result == PermissionResult.DENY) return false;

                    return existing.test(source);
                };
            });

            hooks.hookAll(dispatcher);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to hook command requirements", e);
        }
    }
}
