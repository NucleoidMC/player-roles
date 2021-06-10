package dev.gegy.roles.override;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import dev.gegy.roles.api.override.RoleOverrideReader;
import dev.gegy.roles.api.override.RoleOverrideType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RoleOverrideMap implements RoleOverrideReader {
    @SuppressWarnings("unchecked")
    public static final Codec<RoleOverrideMap> CODEC = MoreCodecs.dispatchByMapKey(RoleOverrideType.REGISTRY, t -> MoreCodecs.listOrUnit((Codec<Object>) t.getCodec()))
            .xmap(RoleOverrideMap::new, m -> m.overrides);

    private final Map<RoleOverrideType<?>, List<Object>> overrides;

    public RoleOverrideMap() {
        this.overrides = new Reference2ObjectOpenHashMap<>();
    }

    private RoleOverrideMap(Map<RoleOverrideType<?>, List<Object>> overrides) {
        this.overrides = new Reference2ObjectOpenHashMap<>(overrides);
    }

    public void notifyChange(ServerPlayerEntity player) {
        for (var override : this.overrides.keySet()) {
            override.notifyChange(player);
        }
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> List<T> get(RoleOverrideType<T> type) {
        return (List<T>) this.overrides.getOrDefault(type, ImmutableList.of());
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> List<T> getOrNull(RoleOverrideType<T> type) {
        return (List<T>) this.overrides.get(type);
    }

    @Override
    public Set<RoleOverrideType<?>> typeSet() {
        return this.overrides.keySet();
    }

    public void clear() {
        this.overrides.clear();
    }

    public void addAll(RoleOverrideReader overrides) {
        for (var type : overrides.typeSet()) {
            this.addAllUnchecked(type, overrides.get(type));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addAllUnchecked(RoleOverrideType<T> type, Collection<?> overrides) {
        this.getOrCreateOverrides(type).addAll((Collection<T>) overrides);
    }

    public <T> void addAll(RoleOverrideType<T> type, Collection<T> overrides) {
        this.getOrCreateOverrides(type).addAll(overrides);
    }

    public <T> void add(RoleOverrideType<T> type, T override) {
        this.getOrCreateOverrides(type).add(override);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getOrCreateOverrides(RoleOverrideType<T> type) {
        return (List<T>) this.overrides.computeIfAbsent(type, t -> new ArrayList<>());
    }
}
