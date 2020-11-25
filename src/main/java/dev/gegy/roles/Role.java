package dev.gegy.roles;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import dev.gegy.roles.api.HasRoles;
import dev.gegy.roles.override.RoleChangeListener;
import dev.gegy.roles.override.RoleOverrideType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Role implements Comparable<Role> {
    public static final String EVERYONE = "everyone";

    private final String name;
    private int level;

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

        Map<Dynamic<T>, Dynamic<T>> overrides = root.get("overrides").orElseEmptyMap().getMapValues().result()
                .orElse(ImmutableMap.of());

        for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : overrides.entrySet()) {
            Optional<String> key = entry.getKey().asString().result();
            Optional<RoleOverrideType<?>> overrideTypeOpt = key.map(RoleOverrideType::byKey);
            if (overrideTypeOpt.isPresent()) {
                RoleOverrideType<?> overrideType = overrideTypeOpt.get();
                Dynamic<T> element = entry.getValue();
                role.overrides.put(overrideType, overrideType.parse(element));
            } else {
                RolesInitializer.LOGGER.warn("Encountered invalid override type: '{}'", entry.getKey());
            }
        }

        return role;
    }

    public void notifyChange(HasRoles owner) {
        for (Object override : this.overrides.values()) {
            if (override instanceof RoleChangeListener) {
                ((RoleChangeListener) override).notifyChange(owner);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public int getLevel() {
        return this.level;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getOverride(RoleOverrideType<T> type) {
        return (T) this.overrides.get(type);
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
