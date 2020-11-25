package dev.gegy.roles;

import dev.gegy.roles.api.HasRoles;
import dev.gegy.roles.api.RoleWriter;
import dev.gegy.roles.override.RoleOverrideType;
import dev.gegy.roles.override.command.PermissionResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RoleStorage implements RoleWriter {
    private final HasRoles owner;

    private final TreeSet<String> roleIds = new TreeSet<>((n1, n2) -> {
        RoleConfiguration config = RoleConfiguration.get();
        Role r1 = config.get(n1);
        Role r2 = config.get(n2);
        if (r1 == null || r2 == null) return 0;
        return r1.compareTo(r2);
    });

    private final Map<RoleOverrideType<?>, Collection<Object>> overrideCache = new Reference2ObjectOpenHashMap<>();

    public RoleStorage(HasRoles owner) {
        this.owner = owner;
    }

    public void notifyReload() {
        this.removeInvalidRoles();
        this.stream().forEach(role -> role.notifyChange(this.owner));

        this.rebuildOverrideCache();
    }

    private void rebuildOverrideCache() {
        this.overrideCache.clear();
        this.stream().forEach(role -> {
            for (RoleOverrideType<?> type : role.getOverrides()) {
                Collection<Object> overrides = this.overrideCache.computeIfAbsent(type, t -> new ArrayList<>());
                overrides.add(role.getOverride(type));
            }
        });
    }

    @Override
    public boolean add(Role role) {
        if (this.roleIds.add(role.getName())) {
            this.rebuildOverrideCache();
            role.notifyChange(this.owner);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Role role) {
        if (this.roleIds.remove(role.getName())) {
            this.rebuildOverrideCache();
            role.notifyChange(this.owner);
            return true;
        }
        return false;
    }

    @Override
    public Stream<Role> stream() {
        RoleConfiguration roleConfig = RoleConfiguration.get();
        return Stream.concat(
                this.roleIds.stream().map(roleConfig::get).filter(Objects::nonNull),
                Stream.of(roleConfig.everyone())
        );
    }

    @Override
    public boolean hasRole(String name) {
        return name.equals(Role.EVERYONE) || this.roleIds.contains(name);
    }

    @Override
    public <T> Stream<T> overrides(RoleOverrideType<T> type) {
        Collection<T> overrides = this.getOverridesOrNull(type);
        return overrides != null ? overrides.stream() : Stream.empty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> Collection<T> getOverridesOrNull(RoleOverrideType<T> type) {
        return (Collection<T>) this.overrideCache.get(type);
    }

    @Override
    public <T> PermissionResult test(RoleOverrideType<T> type, Function<T, PermissionResult> function) {
        Collection<T> overrides = this.getOverridesOrNull(type);
        if (overrides == null) {
            return PermissionResult.PASS;
        }

        for (T override : overrides) {
            PermissionResult result = function.apply(override);
            if (result.isDefinitive()) {
                return result;
            }
        }

        return PermissionResult.PASS;
    }

    @Override
    public boolean test(RoleOverrideType<Boolean> type) {
        Collection<Boolean> overrides = this.getOverridesOrNull(type);
        if (overrides != null) {
            for (Boolean override : overrides) {
                return override;
            }
        }
        return false;
    }

    @Override
    @Nullable
    public <T> T select(RoleOverrideType<T> type) {
        Collection<T> overrides = this.getOverridesOrNull(type);
        if (overrides != null) {
            for (T override : overrides) {
                return override;
            }
        }
        return null;
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

    public void copyFrom(RoleStorage old) {
        this.deserialize(old.serialize());
    }
}
