package dev.gegy.roles.api;

import dev.gegy.roles.PlayerRolesConfig;
import dev.gegy.roles.Role;
import dev.gegy.roles.mixin.ServerCommandSourceAccessor;
import dev.gegy.roles.override.RoleOverrideType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public interface RoleReader {
    Stream<Role> stream();

    default boolean hasRole(String name) {
        return this.stream().anyMatch(role -> role.getName().equals(name));
    }

    <T> Stream<T> overrides(RoleOverrideType<T> type);

    <T> PermissionResult test(RoleOverrideType<T> type, Function<T, PermissionResult> function);

    @Nullable <T> T select(RoleOverrideType<T> type);

    default boolean test(RoleOverrideType<Boolean> type) {
        Boolean result = this.select(type);
        return result != null ? result : false;
    }

    @Nullable
    static RoleReader get(ServerCommandSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof RoleReader) {
            return (RoleReader) entity;
        }

        if (entity == null && source.getRotation() == Vec2f.ZERO && source.hasPermissionLevel(2)) {
            if (!source.hasPermissionLevel(3)) {
                Vec3d pos = source.getPosition();
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
