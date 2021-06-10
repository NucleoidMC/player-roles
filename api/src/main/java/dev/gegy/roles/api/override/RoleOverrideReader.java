package dev.gegy.roles.api.override;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public interface RoleOverrideReader {
    RoleOverrideReader EMPTY = new RoleOverrideReader() {
        @Override
        @Nullable
        public <T> Collection<T> getOrNull(RoleOverrideType<T> type) {
            return null;
        }

        @Override
        public <T> RoleOverrideResult test(RoleOverrideType<T> type, Function<T, RoleOverrideResult> function) {
            return RoleOverrideResult.PASS;
        }

        @Override
        @Nullable
        public <T> T select(RoleOverrideType<T> type) {
            return null;
        }

        @Override
        public boolean test(RoleOverrideType<Boolean> type) {
            return false;
        }

        @Override
        public Set<RoleOverrideType<?>> typeSet() {
            return Collections.emptySet();
        }
    };

    @Nullable <T> Collection<T> getOrNull(RoleOverrideType<T> type);

    @NotNull
    default <T> Collection<T> get(RoleOverrideType<T> type) {
        var overrides = this.getOrNull(type);
        return overrides != null ? overrides : Collections.emptyList();
    }

    default <T> Stream<T> streamOf(RoleOverrideType<T> type) {
        return this.get(type).stream();
    }

    default <T> RoleOverrideResult test(RoleOverrideType<T> type, Function<T, RoleOverrideResult> function) {
        var overrides = this.getOrNull(type);
        if (overrides == null) {
            return RoleOverrideResult.PASS;
        }

        for (var override : overrides) {
            var result = function.apply(override);
            if (result.isDefinitive()) {
                return result;
            }
        }

        return RoleOverrideResult.PASS;
    }

    @Nullable
    default <T> T select(RoleOverrideType<T> type) {
        var overrides = this.getOrNull(type);
        if (overrides != null) {
            for (var override : overrides) {
                return override;
            }
        }
        return null;
    }

    default boolean test(RoleOverrideType<Boolean> type) {
        var result = this.select(type);
        return result != null ? result : false;
    }

    Set<RoleOverrideType<?>> typeSet();
}
