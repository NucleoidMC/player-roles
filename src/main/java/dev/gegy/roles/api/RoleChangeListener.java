package dev.gegy.roles.api;

import net.minecraft.server.network.ServerPlayerEntity;

public interface RoleChangeListener {
    void notifyChange(ServerPlayerEntity player);
}
