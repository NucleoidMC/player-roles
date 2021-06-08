package dev.gegy.roles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class RoleApplyConfig {
    public static final Codec<RoleApplyConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.BOOL.optionalFieldOf("command_block", false).forGetter(c -> c.commandBlock),
                Codec.BOOL.optionalFieldOf("functions", false).forGetter(c -> c.functions)
        ).apply(instance, RoleApplyConfig::new);
    });

    public static final RoleApplyConfig DEFAULT = new RoleApplyConfig(false, false);

    public final boolean commandBlock;
    public final boolean functions;

    public RoleApplyConfig(boolean commandBlock, boolean functions) {
        this.commandBlock = commandBlock;
        this.functions = functions;
    }
}
