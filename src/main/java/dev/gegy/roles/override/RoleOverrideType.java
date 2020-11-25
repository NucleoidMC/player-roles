package dev.gegy.roles.override;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import dev.gegy.roles.override.command.CommandPermissionOverride;
import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RoleOverrideType<T> {
    private static final Map<String, RoleOverrideType<?>> REGISTRY = new HashMap<>();

    public static final RoleOverrideType<CommandPermissionOverride> COMMANDS = RoleOverrideType.<CommandPermissionOverride>builder()
            .key("commands")
            .parse(CommandPermissionOverride::parse)
            .register();

    public static final RoleOverrideType<ChatFormatOverride> CHAT_STYLE = RoleOverrideType.<ChatFormatOverride>builder()
            .key("chat_format")
            .parse(element -> new ChatFormatOverride(element.asString("")))
            .register();

    public static final RoleOverrideType<NameStyleOverride> NAME_FORMAT = RoleOverrideType.<NameStyleOverride>builder()
            .key("name_style")
            .parse(NameStyleOverride::parse)
            .register();

    public static final RoleOverrideType<Boolean> COMMAND_FEEDBACK = RoleOverrideType.<Boolean>builder()
            .key("command_feedback")
            .parse(element -> element.asBoolean(false))
            .register();

    public static final RoleOverrideType<Boolean> MUTE = RoleOverrideType.<Boolean>builder()
            .key("mute")
            .parse(element -> element.asBoolean(false))
            .register();

    public static final RoleOverrideType<Integer> PERMISSION_LEVEL = RoleOverrideType.<Integer>builder()
            .key("permission_level")
            .parse(element -> MathHelper.clamp(element.asInt(0), 0, 4))
            .register();

    private final String key;
    private final Function<Dynamic<?>, T> parse;

    private RoleOverrideType(String key, Function<Dynamic<?>, T> parse) {
        this.key = key;
        this.parse = parse;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public String getKey() {
        return this.key;
    }

    public <U> T parse(Dynamic<U> root) {
        return this.parse.apply(root);
    }

    @Nullable
    public static RoleOverrideType<?> byKey(String key) {
        return REGISTRY.get(key);
    }

    public static class Builder<T> {
        private String key;
        private Function<Dynamic<?>, T> parse;

        private Builder() {
        }

        public Builder<T> key(String key) {
            this.key = key;
            return this;
        }

        public Builder<T> parse(Function<Dynamic<?>, T> deserialize) {
            this.parse = deserialize;
            return this;
        }

        public RoleOverrideType<T> register() {
            Preconditions.checkNotNull(this.key, "key not set");
            Preconditions.checkNotNull(this.parse, "parser not set");

            Preconditions.checkState(!REGISTRY.containsKey(this.key), "override with key already exists");

            RoleOverrideType<T> overrideType = new RoleOverrideType<>(this.key, this.parse);
            REGISTRY.put(this.key, overrideType);

            return overrideType;
        }
    }
}
