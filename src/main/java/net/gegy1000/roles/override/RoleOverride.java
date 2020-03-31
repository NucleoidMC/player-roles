package net.gegy1000.roles.override;

import net.gegy1000.roles.api.HasRoles;

public interface RoleOverride {
    default void notifyChange(HasRoles entity) {
    }
}
