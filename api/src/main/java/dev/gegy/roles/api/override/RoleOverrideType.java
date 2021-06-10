package dev.gegy.roles.api.override;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.util.TinyRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class RoleOverrideType<T> {
    public static final TinyRegistry<RoleOverrideType<?>> REGISTRY = TinyRegistry.create(PlayerRolesApi.ID);

    private final Identifier id;
    private final Codec<T> codec;
    private RoleChangeListener changeListener;

    private RoleOverrideType(Identifier id, Codec<T> codec) {
        this.id = id;
        this.codec = codec;
    }

    public static <T> RoleOverrideType<T> register(Identifier id, Codec<T> codec) {
        var type = new RoleOverrideType<>(id, codec);
        REGISTRY.register(id, type);
        return type;
    }

    public RoleOverrideType<T> withChangeListener(RoleChangeListener listener) {
        this.changeListener = listener;
        return this;
    }

    public Identifier getId() {
        return this.id;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public void notifyChange(ServerPlayerEntity player) {
        if (this.changeListener != null) {
            this.changeListener.onRoleChange(player);
        }
    }

    @Nullable
    public static RoleOverrideType<?> byId(Identifier id) {
        return REGISTRY.get(id);
    }

    @Override
    public String toString() {
        return "RoleOverrideType(" + this.id + ")";
    }
}
