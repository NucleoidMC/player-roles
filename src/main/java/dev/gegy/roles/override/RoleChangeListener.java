package dev.gegy.roles.override;

import dev.gegy.roles.api.RoleOwner;

public interface RoleChangeListener {
    void notifyChange(RoleOwner entity);
}
