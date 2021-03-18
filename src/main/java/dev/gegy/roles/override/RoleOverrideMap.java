package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.RoleOwner;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.Map;
import java.util.Set;

public final class RoleOverrideMap {
    @SuppressWarnings("unchecked")
    public static final Codec<RoleOverrideMap> CODEC = MoreCodecs.dispatchByMapKey(RoleOverrideType.REGISTRY, t -> (Codec<Object>) t.getCodec())
            .xmap(RoleOverrideMap::new, m -> m.overrides);

    private final Map<RoleOverrideType<?>, Object> overrides;

    public RoleOverrideMap() {
        this.overrides = new Reference2ObjectOpenHashMap<>();
    }

    private RoleOverrideMap(Map<RoleOverrideType<?>, Object> overrides) {
        this.overrides = new Reference2ObjectOpenHashMap<>(overrides);
    }

    public void notifyChange(@Nullable RoleOwner owner) {
        for (Object override : this.overrides.values()) {
            if (override instanceof RoleChangeListener) {
                ((RoleChangeListener) override).notifyChange(owner);
            }
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(RoleOverrideType<T> type) {
        return (T) this.overrides.get(type);
    }

    public Set<RoleOverrideType<?>> keySet() {
        return this.overrides.keySet();
    }
}
