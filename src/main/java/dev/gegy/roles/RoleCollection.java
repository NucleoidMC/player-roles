package dev.gegy.roles;

import dev.gegy.roles.api.HasRoles;
import dev.gegy.roles.override.RoleOverride;
import dev.gegy.roles.override.RoleOverrideType;
import dev.gegy.roles.override.command.PermissionResult;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RoleCollection {
    private final HasRoles owner;

    private TreeSet<String> roleIds = new TreeSet<>((n1, n2) -> {
        RoleConfiguration config = RoleConfiguration.get();
        Role r1 = config.get(n1);
        Role r2 = config.get(n2);
        if (r1 == null || r2 == null) return 0;
        return r1.compareTo(r2);
    });

    public RoleCollection(HasRoles owner) {
        this.owner = owner;
    }

    public void notifyReload() {
        this.removeInvalidRoles();
        this.stream().forEach(role -> role.notifyChange(this.owner));
    }

    public boolean add(Role role) {
        if (this.roleIds.add(role.getName())) {
            role.notifyChange(this.owner);
            return true;
        }
        return false;
    }

    public boolean remove(Role role) {
        if (this.roleIds.remove(role.getName())) {
            role.notifyChange(this.owner);
            return true;
        }
        return false;
    }

    public Stream<Role> stream() {
        RoleConfiguration roleConfig = RoleConfiguration.get();
        return Stream.concat(
                this.roleIds.stream().map(roleConfig::get).filter(Objects::nonNull),
                Stream.of(roleConfig.everyone())
        );
    }

    public <T extends RoleOverride> Stream<T> overrides(RoleOverrideType<T> type) {
        return this.stream().map(role -> role.getOverride(type)).filter(Objects::nonNull);
    }

    public <T extends RoleOverride> PermissionResult test(RoleOverrideType<T> type, Function<T, PermissionResult> function) {
        return this.overrides(type).map(function)
                .filter(PermissionResult::isDefinitive)
                .findFirst().orElse(PermissionResult.PASS);
    }

    @Nullable
    public <T extends RoleOverride> T getHighest(RoleOverrideType<T> type) {
        return this.overrides(type).findFirst().orElse(null);
    }

    public ListTag serialize() {
        ListTag list = new ListTag();
        for (String role : this.roleIds) {
            list.add(StringTag.of(role));
        }
        return list;
    }

    public void deserialize(ListTag list) {
        this.roleIds.clear();
        for (int i = 0; i < list.size(); i++) {
            this.roleIds.add(list.getString(i));
        }
        this.removeInvalidRoles();
    }

    private void removeInvalidRoles() {
        this.roleIds.removeIf(name -> {
            Role role = RoleConfiguration.get().get(name);
            if (role == null || role.getName().equalsIgnoreCase(Role.EVERYONE)) {
                RolesInitializer.LOGGER.warn("Encountered invalid role '{}'", name);
                return true;
            }
            return false;
        });
    }

    public void copyFrom(RoleCollection old) {
        this.deserialize(old.serialize());
    }
}
