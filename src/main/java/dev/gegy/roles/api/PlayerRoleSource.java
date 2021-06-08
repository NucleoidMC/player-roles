package dev.gegy.roles.api;

import dev.gegy.roles.store.PlayerRoleSet;

public interface PlayerRoleSource {
    void notifyPlayerRoleReload();

    PlayerRoleSet getPlayerRoles();
}
