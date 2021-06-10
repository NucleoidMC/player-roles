package dev.gegy.roles.api;

import dev.gegy.roles.api.override.RoleOverrideReader;
import org.jetbrains.annotations.NotNull;

public interface Role extends Comparable<Role> {
    String getId();

    int getIndex();

    RoleOverrideReader getOverrides();

    @Override
    default int compareTo(@NotNull Role role) {
        int compareIndex = Integer.compare(role.getIndex(), this.getIndex());
        return compareIndex != 0 ? compareIndex : this.getId().compareTo(role.getId());
    }
}
