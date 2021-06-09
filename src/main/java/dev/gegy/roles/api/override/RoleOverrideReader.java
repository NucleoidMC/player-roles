package dev.gegy.roles.api.override;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public interface RoleOverrideReader {
    <T> Stream<T> streamOf(RoleOverrideType<T> type);

    <T> OverrideResult test(RoleOverrideType<T> type, Function<T, OverrideResult> function);

    @Nullable <T> T select(RoleOverrideType<T> type);

    default boolean test(RoleOverrideType<Boolean> type) {
        var result = this.select(type);
        return result != null ? result : false;
    }
}
