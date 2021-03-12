package dev.gegy.roles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.override.RoleChangeListener;
import dev.gegy.roles.override.RoleOverrideType;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Role implements Comparable<Role> {
    public static final String EVERYONE = "everyone";

    private final String name;
    private int level;

    private static final Codec<? extends Map<RoleOverrideType<?>, ?>> OVERRIDES_CODEC = MoreCodecs.dispatchByMapKey(RoleOverrideType.REGISTRY, RoleOverrideType::getCodec);

    private final Map<RoleOverrideType<?>, Object> overrides = new HashMap<>();

    Role(String name) {
        this.name = name;
    }

    public static Role empty(String name) {
        return new Role(name);
    }

    public static <T> Role parse(String name, Dynamic<T> root) {
        Role role = new Role(name);

        role.level = root.get("level").asInt(0);

        DataResult<? extends Map<RoleOverrideType<?>, ?>> result = OVERRIDES_CODEC.parse(root.get("overrides").orElseEmptyMap());
        if (result.error().isPresent()) {
            PlayerRolesInitializer.LOGGER.warn("Encountered invalid role override definition for '{}': {}", name, result.error().get());
            return role;
        }

        result.result().ifPresent(role.overrides::putAll);

        return role;
    }

    public void notifyChange(@Nullable RoleOwner owner) {
        for (Object override : this.overrides.values()) {
            if (override instanceof RoleChangeListener) {
                ((RoleChangeListener) override).notifyChange(owner);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getOverride(RoleOverrideType<T> type) {
        return (T) this.overrides.get(type);
    }

    public Set<RoleOverrideType<?>> getOverrides() {
        return this.overrides.keySet();
    }

    @Override
    public int compareTo(Role role) {
        return Integer.compare(role.level, this.level);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof Role) {
            Role role = (Role) obj;
            return role.name.equalsIgnoreCase(this.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return "\"" + this.name + "\" (" + this.level + ")";
    }
}
