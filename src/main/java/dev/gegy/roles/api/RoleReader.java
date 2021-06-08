package dev.gegy.roles.api;

import dev.gegy.roles.Role;
import dev.gegy.roles.api.override.RoleOverrideType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public interface RoleReader {
    Stream<Role> stream();

    default boolean hasRole(String name) {
        return this.stream().anyMatch(role -> role.getName().equals(name));
    }

    <T> Stream<T> overrides(RoleOverrideType<T> type);

    <T> PermissionResult test(RoleOverrideType<T> type, Function<T, PermissionResult> function);

    @Nullable <T> T select(RoleOverrideType<T> type);

    default boolean test(RoleOverrideType<Boolean> type) {
        var result = this.select(type);
        return result != null ? result : false;
    }
}
