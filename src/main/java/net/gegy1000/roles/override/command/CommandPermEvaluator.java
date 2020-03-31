package net.gegy1000.roles.override.command;

import net.gegy1000.roles.RoleCollection;
import net.gegy1000.roles.api.HasRoles;
import net.gegy1000.roles.override.RoleOverrideType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

public class CommandPermEvaluator {
    public static PermissionResult canUseCommand(ServerCommandSource source, MatchableCommand command) {
        if (doesBypassPermissions(source)) return PermissionResult.PASS;

        Entity entity = source.getEntity();
        if (entity instanceof HasRoles) {
            RoleCollection roles = ((HasRoles) entity).getRoles();
            if (roles != null) {
                return roles.test(RoleOverrideType.COMMANDS, m -> m.test(command));
            }
        }

        return PermissionResult.PASS;
    }

    public static boolean doesBypassPermissions(ServerCommandSource source) {
        return source.hasPermissionLevel(4) || !(source.getEntity() instanceof PlayerEntity);
    }
}
