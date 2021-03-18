package dev.gegy.roles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.roles.override.RoleOverrideMap;

public final class RoleConfig {
    public static final Codec<RoleConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.optionalFieldOf("level", 0).forGetter(c -> c.level),
                RoleOverrideMap.CODEC.fieldOf("overrides").orElseGet(RoleOverrideMap::new).forGetter(c -> c.overrides)
        ).apply(instance, RoleConfig::new);
    });

    public final int level;
    public final RoleOverrideMap overrides;

    public RoleConfig(int level, RoleOverrideMap overrides) {
        this.level = level;
        this.overrides = overrides;
    }

    public Role create(String name) {
        return new Role(name, this.overrides, this.level);
    }
}
