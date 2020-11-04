package dev.gegy.roles.override;

import dev.gegy.roles.api.HasRoles;

public interface RoleOverride {
    default void notifyChange(HasRoles entity) {
    }
}
