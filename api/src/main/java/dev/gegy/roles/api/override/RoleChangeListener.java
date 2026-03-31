package dev.gegy.roles.api.override;

import net.minecraft.server.level.ServerPlayer;

public interface RoleChangeListener {
    void onRoleChange(ServerPlayer player);
}
