package dev.gegy.roles;

import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.store.PlayerRoleSet;

public interface PlayerWithRoles {
    PlayerRoleSet loadPlayerRoles(PlayerRolesConfig config);

    PlayerRoleSet getPlayerRoleSet();
}
