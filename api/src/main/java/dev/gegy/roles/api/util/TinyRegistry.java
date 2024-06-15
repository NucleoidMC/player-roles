package dev.gegy.roles.api.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

public final class TinyRegistry<T> implements Codec<T>, Iterable<T> {
    private final BiMap<Identifier, T> map = HashBiMap.create();
    private final String defaultNamespace;

    private TinyRegistry(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public static <T> TinyRegistry<T> create() {
        return new TinyRegistry<>("minecraft");
    }

    public static <T> TinyRegistry<T> create(String defaultNamespace) {
        return new TinyRegistry<>(defaultNamespace);
    }

    public void register(Identifier id, T value) {
        this.map.put(id, value);
    }

    @Nullable
    public T get(Identifier id) {
        return this.map.get(id);
    }

    @Nullable
    public Identifier getId(T value) {
        return this.map.inverse().get(value);
    }

    public boolean containsId(Identifier id) {
        return this.map.containsKey(id);
    }

    @Override
    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U input) {
        return Codec.STRING.decode(ops, input)
                .flatMap(pair -> {
                    var id = this.parseId(pair.getFirst());
                    var entry = this.get(id);
                    if (entry == null) {
                        return DataResult.error(() ->"Unknown registry key: " + pair.getFirst());
                    }
                    return DataResult.success(Pair.of(entry, pair.getSecond()));
                });
    }

    private Identifier parseId(String string) {
        if (string.indexOf(Identifier.NAMESPACE_SEPARATOR) != -1) {
            return Identifier.of(string);
        } else {
            return Identifier.of(this.defaultNamespace, string);
        }
    }

    @Override
    public <U> DataResult<U> encode(T input, DynamicOps<U> ops, U prefix) {
        var id = this.getId(input);
        if (id == null) {
            return DataResult.error(() -> "Unknown registry element " + input);
        }
        return ops.mergeToPrimitive(prefix, ops.createString(id.toString()));
    }

    public Set<Identifier> ids() {
        return this.map.keySet();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.map.values().iterator();
    }
}
