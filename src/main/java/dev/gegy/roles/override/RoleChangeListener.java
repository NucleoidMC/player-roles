package dev.gegy.roles.override;

import dev.gegy.roles.api.HasRoles;

public interface RoleChangeListener {
    void notifyChange(HasRoles entity);
}
