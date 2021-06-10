package dev.gegy.roles.store;

import dev.gegy.roles.api.Role;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.api.override.RoleOverrideReader;
import dev.gegy.roles.override.RoleOverrideMap;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

import java.util.Iterator;
import java.util.stream.Stream;

public final class ServerRoleSet implements RoleReader {
    private final ObjectSortedSet<Role> roles;
    private final RoleOverrideMap overrides = new RoleOverrideMap();

    private ServerRoleSet(ObjectSortedSet<Role> roles) {
        this.roles = roles;
        for (var role : this.roles) {
            this.overrides.addAll(role.getOverrides());
        }
    }

    public static ServerRoleSet of(ObjectSortedSet<Role> roles) {
        return new ServerRoleSet(roles);
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
    public boolean has(Role role) {
        return this.roles.contains(role);
    }

    @Override
    public RoleOverrideReader overrides() {
        return this.overrides;
    }
}
