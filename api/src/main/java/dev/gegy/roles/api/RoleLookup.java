package dev.gegy.roles.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public interface RoleLookup {
    RoleLookup EMPTY = new RoleLookup() {
        @Override
        @NotNull
        public RoleReader byEntity(Entity entity) {
            return RoleReader.EMPTY;
        }

        @Override
        @NotNull
        public RoleReader bySource(ServerCommandSource source) {
            return RoleReader.EMPTY;
        }
    };

    @NotNull
    default RoleReader byPlayer(PlayerEntity player) {
        return this.byEntity(player);
    }

    @NotNull
    RoleReader byEntity(Entity entity);

    @NotNull
    RoleReader bySource(ServerCommandSource source);
}
