package dev.gegy.roles.store;

import dev.gegy.roles.Role;
import dev.gegy.roles.api.RoleWriter;
import dev.gegy.roles.api.override.RoleOverrideReader;
import dev.gegy.roles.override.RoleOverrideMap;

import java.util.Iterator;
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
    public Iterator<Role> iterator() {
        return this.roles.iterator();
    }

    @Override
    public Stream<Role> stream() {
        return this.roles.stream();
    }

    @Override
    public boolean has(String name) {
        return this.roles.containsId(name);
    }

    @Override
    public RoleOverrideReader overrides() {
        return this.overrides;
    }
}
