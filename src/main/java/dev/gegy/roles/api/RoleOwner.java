package dev.gegy.roles.api;

import dev.gegy.roles.Role;
import dev.gegy.roles.override.RoleOverrideType;
import dev.gegy.roles.store.PlayerRoleSet;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public interface RoleOwner extends RoleReader {
    PlayerRoleSet getRoles();

    @Override
    default Stream<Role> stream() {
        return this.getRoles().stream();
    }

    @Override
    default boolean hasRole(String name) {
        return this.getRoles().hasRole(name);
    }

    @Override
    default <T> Stream<T> overrides(RoleOverrideType<T> type) {
        return this.getRoles().overrides(type);
    }

    @Override
    default <T> PermissionResult test(RoleOverrideType<T> type, Function<T, PermissionResult> function) {
        return this.getRoles().test(type, function);
    }

    @Override
    default boolean test(RoleOverrideType<Boolean> type) {
        return this.getRoles().test(type);
    }

    @Override
    @Nullable
    default <T> T select(RoleOverrideType<T> type) {
        return this.getRoles().select(type);
    }
}
