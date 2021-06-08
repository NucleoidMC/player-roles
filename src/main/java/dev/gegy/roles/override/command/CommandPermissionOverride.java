package dev.gegy.roles.override.command;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.PermissionResult;

public final class CommandPermissionOverride {
    public static final Codec<CommandPermissionOverride> CODEC = CommandPermissionRules.CODEC.xmap(
            CommandPermissionOverride::new,
            override -> override.rules
    );

    private final CommandPermissionRules rules;

    public CommandPermissionOverride(CommandPermissionRules rules) {
        this.rules = rules;
    }

    public PermissionResult test(MatchableCommand command) {
        return this.rules.test(command);
    }

    @Override
    public String toString() {
        return "CommandPermissionOverride[" + this.rules.toString() + "]";
    }
}
