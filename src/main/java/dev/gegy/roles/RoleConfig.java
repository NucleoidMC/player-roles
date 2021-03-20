package dev.gegy.roles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.roles.override.RoleOverrideMap;
import xyz.nucleoid.codecs.MoreCodecs;

public final class RoleConfig {
    public static final Codec<RoleConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("level", 0).forGetter(c -> c.level),
                RoleOverrideMap.CODEC.fieldOf("overrides").orElseGet(RoleOverrideMap::new).forGetter(c -> c.overrides),
                MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("includes", new String[0]).forGetter(c -> c.includes)
        ).apply(instance, RoleConfig::new);
    });

    public final int level;
    public final RoleOverrideMap overrides;
    public final String[] includes;

    public RoleConfig(int level, RoleOverrideMap overrides, String[] includes) {
        this.level = level;
        this.overrides = overrides;
        this.includes = includes;
    }

    public Role create(String name, int level) {
        return new Role(name, this.overrides, level);
    }
}
