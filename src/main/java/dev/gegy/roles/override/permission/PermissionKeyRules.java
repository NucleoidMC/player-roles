package dev.gegy.roles.override.permission;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;


public final class PermissionKeyRules {
    public static final Codec<PermissionKeyRules> CODEC = Codec.unboundedMap(Codec.either(Identifier.CODEC, PartialIdentifier.CODEC), PermissionValue.CODEC).xmap(
            map -> {
                var exactPermissions = new Object2ObjectOpenHashMap<Identifier, PermissionValue>();
                var partialMatches = new Object2ObjectOpenHashMap<String, List<Pair<PartialIdentifier, PermissionValue>>>();

                for (var entry : map.entrySet()) {
                    entry.getKey().ifLeft(x -> exactPermissions.putIfAbsent(x, entry.getValue()));
                    entry.getKey().ifRight(x -> partialMatches.computeIfAbsent(x.namespace(), _ -> new ArrayList<>()).add(Pair.of(x, entry.getValue())));
                }

                for (var list : partialMatches.values()) {
                    list.sort(Comparator.comparingLong(x -> -x.getFirst().startingPath.chars().filter(y -> y == '/').count()));
                }
                //noinspection unchecked
                return new PermissionKeyRules(exactPermissions, partialMatches);
            },
            override -> {
                var map = new HashMap<Either<Identifier, PartialIdentifier>, PermissionValue>();
                override.exactPermissions.forEach((k, v) -> map.put(Either.left(k), v));
                for (var c : override.keyMatchers.values()) {
                    for (var p : c) {
                    map.put(Either.right(p.getFirst()), p.getSecond());
                    }
                }

                return map;
            }
    );

    private final Map<Identifier, PermissionValue> exactPermissions;
    private final Map<String, List<Pair<PartialIdentifier, PermissionValue>>> keyMatchers;

    private PermissionKeyRules(Map<Identifier, PermissionValue> exactPermissions, Map<String, List<Pair<PartialIdentifier, PermissionValue>>> keyMatchers) {
        this.exactPermissions = exactPermissions;
        this.keyMatchers = keyMatchers;
    }

    @Nullable
    public <T> T test(Identifier key, Codec<T> codec) {
        var result = this.exactPermissions.get(key);
        if (result != null) {
            return result.get(codec);
        }

        for (var matcher : this.keyMatchers.getOrDefault(key.getNamespace(), List.of())) {
            if (matcher.getFirst().test(key)) {
                return matcher.getSecond().get(codec);
            }
        }

        return null;
    }

    public record PartialIdentifier(String namespace, String startingPath) {
        public static final Codec<PartialIdentifier> CODEC = Codec.STRING.comapFlatMap(PartialIdentifier::read, PartialIdentifier::toString);

        private static DataResult<? extends PartialIdentifier> read(String string) {
            var parts = string.split(":", 2);
            String namespace;
            String path;
            if (parts.length == 1) {
                namespace = "minecraft";
                path = parts[0];
            } else {
                namespace = parts[0];
                path = parts[1];
            }

            if (!Identifier.isValidNamespace(namespace)) {
                return DataResult.error(() -> string + " contains invalid identifier namespace!");
            }

            if (path.equals("*")) {
                return DataResult.success(new PartialIdentifier(namespace, ""));
            }

            if (!path.endsWith("/*")) {
                return DataResult.error(() -> string + " path doesn't end with a '/*'!");
            }

            path = path.substring(0, parts.length - 1);

            if (!Identifier.isValidPath(path)) {
                return DataResult.error(() -> string + " contains invalid identifier path!");
            }

            return DataResult.success(new PartialIdentifier(namespace, path));
        }

        @Override
        public @NonNull String toString() {
            return this.namespace + ":" + this.startingPath + "*";
        }

        public boolean test(Identifier key) {
            return this.namespace.equals(key.getNamespace()) && key.getPath().startsWith(this.startingPath);
        }
    }

    public record PermissionValue(Dynamic<?> value, Map<Codec<?>, Object> map) {
        public static final Codec<PermissionValue> CODEC = Codec.PASSTHROUGH.xmap(PermissionValue::new, PermissionValue::value);

        public PermissionValue(Dynamic<?> value) {
            this(value, Collections.synchronizedMap(new IdentityHashMap<>()));
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T get(Codec<T> codec) {
            Optional<T> val = (Optional<T>) this.map.get(codec);

            //noinspection OptionalAssignedToNull
            if (val != null) {
                return val.orElse(null);
            }

            Optional<T> parse = codec.parse(this.value).result();
            this.map.put(codec, parse);
            return parse.orElse(null);
        }
    }
}
