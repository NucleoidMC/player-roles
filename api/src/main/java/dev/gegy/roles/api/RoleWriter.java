package dev.gegy.roles.api;

public interface RoleWriter extends RoleReader {
    boolean add(Role role);

    boolean remove(Role role);
}
