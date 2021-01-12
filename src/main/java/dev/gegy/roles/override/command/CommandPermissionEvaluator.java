package dev.gegy.roles.override.command;

import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.override.RoleOverrideType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

public final class CommandPermissionEvaluator {
    public static PermissionResult canUseCommand(ServerCommandSource source, MatchableCommand command) {
        if (doesBypassPermissions(source)) return PermissionResult.PASS;

        Entity entity = source.getEntity();
        if (entity instanceof RoleOwner) {
            RoleReader roles = ((RoleOwner) entity).getRoles();
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
