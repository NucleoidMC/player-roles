package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import dev.gegy.roles.override.command.CommandPermissionOverride;
import dev.gegy.roles.util.TinyRegistry;
import org.jetbrains.annotations.Nullable;

public final class RoleOverrideType<T> {
    public static final TinyRegistry<RoleOverrideType<?>> REGISTRY = TinyRegistry.newStable();

    public static final RoleOverrideType<CommandPermissionOverride> COMMANDS = RoleOverrideType.register("commands", CommandPermissionOverride.CODEC);
    public static final RoleOverrideType<ChatFormatOverride> CHAT_STYLE = RoleOverrideType.register("chat_format", ChatFormatOverride.CODEC);
    public static final RoleOverrideType<NameStyleOverride> NAME_FORMAT = RoleOverrideType.register("name_style", NameStyleOverride.CODEC);
    public static final RoleOverrideType<Boolean> COMMAND_FEEDBACK = RoleOverrideType.register("command_feedback", Codec.BOOL);
    public static final RoleOverrideType<Boolean> MUTE = RoleOverrideType.register("mute", Codec.BOOL);
    public static final RoleOverrideType<Integer> PERMISSION_LEVEL = RoleOverrideType.register("permission_level", Codec.intRange(0, 4));

    private final String key;
    private final Codec<T> codec;

    private RoleOverrideType(String key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
    }

    public static <T> RoleOverrideType<T> register(String key, Codec<T> codec) {
        RoleOverrideType<T> type = new RoleOverrideType<>(key, codec);
        REGISTRY.register(key, type);
        return type;
    }

    public String getKey() {
        return this.key;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    @Nullable
    public static RoleOverrideType<?> byKey(String key) {
        return REGISTRY.get(key);
    }
}
