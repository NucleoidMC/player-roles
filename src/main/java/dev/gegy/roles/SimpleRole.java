package dev.gegy.roles;

import dev.gegy.roles.api.Role;
import dev.gegy.roles.config.RoleApplyConfig;
import dev.gegy.roles.override.RoleOverrideMap;

public final class SimpleRole implements Role {
    private final String id;
    private final RoleOverrideMap overrides;
    private final int index;
    private final RoleApplyConfig apply;

    public SimpleRole(String id, RoleOverrideMap overrides, int index, RoleApplyConfig apply) {
        this.id = id;
        this.overrides = overrides;
        this.index = index;
        this.apply = apply;
    }

    public static SimpleRole empty(String id) {
        return new SimpleRole(id, new RoleOverrideMap(), 0, RoleApplyConfig.DEFAULT);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public RoleOverrideMap getOverrides() {
        return this.overrides;
    }

    public RoleApplyConfig getApply() {
        return this.apply;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof SimpleRole role) {
            return this.index == role.index && role.id.equalsIgnoreCase(this.id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return "\"" + this.id + "\" (" + this.index + ")";
    }
}
