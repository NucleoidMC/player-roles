package dev.gegy.roles.api;

import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.mixin.ServerCommandSourceAccessor;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

public final class RoleLookup {
    @Nullable
    public static RoleReader bySource(ServerCommandSource source) {
        var entity = source.getEntity();
        if (entity instanceof RoleReader) {
            return (RoleReader) entity;
        }

        if (entity == null && source.getRotation() == Vec2f.ZERO && source.hasPermissionLevel(2)) {
            if (!source.hasPermissionLevel(3)) {
                var pos = source.getPosition();
                if (source.getWorld().getBlockEntity(new BlockPos(pos)) instanceof CommandBlockBlockEntity) {
                    return PlayerRolesConfig.get().getCommandBlockRoles();
                }
            }

            if (((ServerCommandSourceAccessor) source).isSilent()) {
                return PlayerRolesConfig.get().getFunctionRoles();
            }
        }

        return null;
    }
}
