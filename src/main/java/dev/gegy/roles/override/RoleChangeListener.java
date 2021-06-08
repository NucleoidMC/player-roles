package dev.gegy.roles.override;

import dev.gegy.roles.api.PlayerRoleSource;

public interface RoleChangeListener {
    void notifyChange(PlayerRoleSource owner);
}
