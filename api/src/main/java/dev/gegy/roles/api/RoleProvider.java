package dev.gegy.roles.api;

import org.jetbrains.annotations.Nullable;

public interface RoleProvider {
    @Nullable
    Role get(String id);
}
