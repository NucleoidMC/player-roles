package dev.gegy.roles;

import dev.gegy.roles.override.RoleOverrideMap;

public final class Role implements Comparable<Role> {
    public static final String EVERYONE = "everyone";

    private final String name;
    private final RoleOverrideMap overrides;
    private final int level;
    private final RoleApplyConfig apply;

    Role(String name, RoleOverrideMap overrides, int level, RoleApplyConfig apply) {
        this.name = name;
        this.overrides = overrides;
        this.level = level;
        this.apply = apply;
    }

    public static Role empty(String name) {
        return new Role(name, new RoleOverrideMap(), 0, RoleApplyConfig.DEFAULT);
    }

    public String getName() {
        return this.name;
    }

    public int getLevel() {
        return this.level;
    }

    public RoleOverrideMap getOverrides() {
        return this.overrides;
    }

    public RoleApplyConfig getApply() {
        return this.apply;
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
