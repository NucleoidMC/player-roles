package dev.gegy.roles.override.command;

import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.override.RoleOverrideType;
import net.minecraft.server.command.ServerCommandSource;

public final class CommandPermissionEvaluator {
    public static PermissionResult canUseCommand(ServerCommandSource source, MatchableCommand command) {
        if (doesBypassPermissions(source)) return PermissionResult.PASS;

        RoleReader roles = RoleReader.get(source);
        if (roles != null) {
            return roles.test(RoleOverrideType.COMMANDS, m -> m.test(command));
        }

        return PermissionResult.PASS;
    }

    public static boolean doesBypassPermissions(ServerCommandSource source) {
        return source.hasPermissionLevel(4);
    }
}
