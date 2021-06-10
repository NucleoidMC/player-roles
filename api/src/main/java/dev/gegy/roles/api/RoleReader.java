package dev.gegy.roles.api;

import dev.gegy.roles.api.override.RoleOverrideReader;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RoleReader extends Iterable<Role> {
    RoleReader EMPTY = new RoleReader() {
        @NotNull
        @Override
        public Iterator<Role> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public boolean has(Role role) {
            return false;
        }

        @Override
        public RoleOverrideReader overrides() {
            return RoleOverrideReader.EMPTY;
        }
    };

    default Stream<Role> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    boolean has(Role role);

    RoleOverrideReader overrides();
}
