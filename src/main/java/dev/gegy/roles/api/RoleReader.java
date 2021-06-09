package dev.gegy.roles.api;

import dev.gegy.roles.Role;
import dev.gegy.roles.api.override.RoleOverrideReader;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RoleReader extends Iterable<Role> {
    default Stream<Role> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    default boolean has(String name) {
        return this.stream().anyMatch(role -> role.getName().equals(name));
    }

    RoleOverrideReader overrides();
}
