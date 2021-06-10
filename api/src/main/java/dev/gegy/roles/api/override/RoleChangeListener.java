package dev.gegy.roles.api.override;

import net.minecraft.server.network.ServerPlayerEntity;

public interface RoleChangeListener {
    void onRoleChange(ServerPlayerEntity player);
}
