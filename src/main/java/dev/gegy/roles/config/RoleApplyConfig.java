package dev.gegy.roles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RoleApplyConfig(boolean commandBlock, boolean functions) {
    public static final Codec<RoleApplyConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("command_block", false).forGetter(c -> c.commandBlock),
            Codec.BOOL.optionalFieldOf("functions", false).forGetter(c -> c.functions)
    ).apply(i, RoleApplyConfig::new));

    public static final RoleApplyConfig DEFAULT = new RoleApplyConfig(false, false);
}
