package net.gegy1000.roles.override;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import net.gegy1000.roles.override.command.CommandPermOverride;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RoleOverrideType<T extends RoleOverride> {
    private static final Map<String, RoleOverrideType<?>> REGISTRY = new HashMap<>();

    public static final RoleOverrideType<CommandPermOverride> COMMANDS = RoleOverrideType.<CommandPermOverride>builder()
            .key("commands")
            .parse(CommandPermOverride::parse)
            .register();

    public static final RoleOverrideType<ChatFormatOverride> CHAT_STYLE = RoleOverrideType.<ChatFormatOverride>builder()
            .key("chat_format")
            .parse(element -> new ChatFormatOverride(element.asString("")))
            .register();

    public static final RoleOverrideType<NameStyleOverride> NAME_FORMAT = RoleOverrideType.<NameStyleOverride>builder()
            .key("name_style")
            .parse(NameStyleOverride::parse)
            .register();

    public static final RoleOverrideType<CommandFeedbackOverride> COMMAND_FEEDBACK = RoleOverrideType.<CommandFeedbackOverride>builder()
            .key("command_feedback")
            .parse(element -> new CommandFeedbackOverride(element.asBoolean(false)))
            .register();

    private final String key;
    private final Function<Dynamic<?>, T> parse;

    private RoleOverrideType(String key, Function<Dynamic<?>, T> parse) {
        this.key = key;
        this.parse = parse;
    }

    public static <T extends RoleOverride> Builder<T> builder() {
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

    public static class Builder<T extends RoleOverride> {
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
