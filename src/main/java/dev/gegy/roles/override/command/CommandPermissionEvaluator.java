package dev.gegy.roles.override.command;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.RoleLookup;
import dev.gegy.roles.api.RoleReader;
import net.minecraft.server.command.ServerCommandSource;

public final class CommandPermissionEvaluator {
    public static PermissionResult canUseCommand(ServerCommandSource source, MatchableCommand command) {
        if (doesBypassPermissions(source)) return PermissionResult.PASS;

        RoleReader roles = RoleLookup.bySource(source);
        if (roles != null) {
            return roles.test(PlayerRoles.COMMANDS, m -> m.test(command));
        }

        return PermissionResult.PASS;
    }

    public static boolean doesBypassPermissions(ServerCommandSource source) {
        return source.hasPermissionLevel(4);
    }
}
