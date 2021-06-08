package dev.gegy.roles.api.override;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.override.RoleChangeListener;
import dev.gegy.roles.util.TinyRegistry;
import org.jetbrains.annotations.Nullable;

public final class RoleOverrideType<T> {
    public static final TinyRegistry<RoleOverrideType<?>> REGISTRY = TinyRegistry.newStable();

    private final String key;
    private final Codec<T> codec;
    private RoleChangeListener changeListener;

    private RoleOverrideType(String key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
    }

    public static <T> RoleOverrideType<T> register(String key, Codec<T> codec) {
        var type = new RoleOverrideType<>(key, codec);
        REGISTRY.register(key, type);
        return type;
    }

    public RoleOverrideType<T> withChangeListener(RoleChangeListener listener) {
        this.changeListener = listener;
        return this;
    }

    public String getKey() {
        return this.key;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public void notifyChange(PlayerRoleSource owner) {
        if (this.changeListener != null) {
            this.changeListener.notifyChange(owner);
        }
    }

    @Nullable
    public static RoleOverrideType<?> byKey(String key) {
        return REGISTRY.get(key);
    }

    @Override
    public String toString() {
        return "RoleOverrideType(" + this.key + ")";
    }
}
