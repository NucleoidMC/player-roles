package dev.gegy.roles.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.roles.SimpleRole;
import dev.gegy.roles.override.RoleOverrideMap;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.Optional;
import java.util.function.Supplier;

public final class RoleConfig {
    public static final Codec<RoleConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("level", 0).forGetter(c -> c.level),
            RoleOverrideMap.CODEC.optionalFieldOf("overrides", new RoleOverrideMap()).forGetter(c -> c.overrides),
            MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("includes", new String[0]).forGetter(c -> c.includes),
            RoleApplyConfig.CODEC.optionalFieldOf("apply").forGetter(c -> Optional.ofNullable(c.apply))
    ).apply(i, RoleConfig::new));

    public final int level;
    public final RoleOverrideMap overrides;
    public final String[] includes;
    public final @Nullable RoleApplyConfig apply;

    private RoleConfig(int level, RoleOverrideMap overrides, String[] includes, Optional<RoleApplyConfig> apply) {
        this.level = level;
        this.overrides = new RoleOverrideMap(overrides);
        this.includes = includes;
        this.apply = apply.orElse(null);
    }

    public RoleConfig(int level, RoleOverrideMap overrides, String[] includes, @Nullable RoleApplyConfig apply) {
        this.level = level;
        this.overrides = overrides;
        this.includes = includes;
        this.apply = apply;
    }

    public SimpleRole create(String name, int index) {
        return new SimpleRole(name, this.overrides, index, this.apply != null ? this.apply : RoleApplyConfig.DEFAULT);
    }
}
