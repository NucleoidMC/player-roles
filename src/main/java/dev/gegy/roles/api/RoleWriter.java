package dev.gegy.roles.api;

import dev.gegy.roles.Role;

public interface RoleWriter extends RoleReader {
    boolean add(Role role);

    boolean remove(Role role);
}
