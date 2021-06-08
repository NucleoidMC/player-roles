package dev.gegy.roles.store;

import dev.gegy.roles.Role;
import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.RoleWriter;
import dev.gegy.roles.override.RoleOverrideMap;
import dev.gegy.roles.api.override.RoleOverrideType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public final class ServerRoleSet implements RoleWriter {
    private final RoleSet roles = new RoleSet();
    private final RoleOverrideMap overrides = new RoleOverrideMap();

    public ServerRoleSet() {
        this.rebuildOverrides();
    }

    private void rebuildOverrides() {
        this.overrides.clear();
        for (var role : this.roles) {
            this.overrides.addAll(role.getOverrides());
        }
    }

    @Override
    public boolean add(Role role) {
        if (this.roles.add(role)) {
            this.rebuildOverrides();
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Role role) {
        if (this.roles.remove(role)) {
            this.rebuildOverrides();
            return true;
        }
        return false;
    }

    @Override
    public Stream<Role> stream() {
        return this.roles.stream();
    }

    @Override
    public boolean hasRole(String name) {
        return this.roles.containsId(name);
    }

    @Override
    public <T> Stream<T> overrides(RoleOverrideType<T> type) {
        return this.overrides.streamOf(type);
    }

    @Override
    public <T> PermissionResult test(RoleOverrideType<T> type, Function<T, PermissionResult> function) {
        return this.overrides.test(type, function);
    }

    @Override
    @Nullable
    public <T> T select(RoleOverrideType<T> type) {
        return this.overrides.select(type);
    }
}
