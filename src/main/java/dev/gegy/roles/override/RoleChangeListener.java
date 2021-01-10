package dev.gegy.roles.override;

import dev.gegy.roles.api.RoleOwner;
import org.jetbrains.annotations.Nullable;

public interface RoleChangeListener {
    void notifyChange(@Nullable RoleOwner entity);
}
