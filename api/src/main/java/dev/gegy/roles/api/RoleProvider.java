package dev.gegy.roles.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RoleProvider extends Iterable<Role> {
    RoleProvider EMPTY = new RoleProvider() {
        @Override
        @Nullable
        public Role get(String id) {
            return null;
        }

        @NotNull
        @Override
        public Iterator<Role> iterator() {
            return Collections.emptyIterator();
        }
    };

    @Nullable
    Role get(String id);

    default Stream<Role> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}
