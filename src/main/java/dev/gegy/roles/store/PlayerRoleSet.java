package dev.gegy.roles.store;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.Role;
import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.api.RoleWriter;
import dev.gegy.roles.override.RoleOverrideMap;
import dev.gegy.roles.api.override.RoleOverrideType;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

public final class PlayerRoleSet implements RoleWriter {
    @Nullable
    private final ServerPlayerEntity player;

    private final RoleSet roles = new RoleSet();
    private final RoleOverrideMap overrides = new RoleOverrideMap();

    private boolean dirty;

    public PlayerRoleSet(@Nullable ServerPlayerEntity player) {
        this.player = player;
        this.rebuildOverrides();
    }

    public void rebuildOverridesAndNotify() {
        this.rebuildOverrides();
        if (this.player != null) {
            this.overrides.notifyChange((PlayerRoleSource) this.player);
        }
    }

    private void rebuildOverrides() {
        this.overrides.clear();
        this.stream().forEach(role -> this.overrides.addAll(role.getOverrides()));
    }

    @Override
    public boolean add(Role role) {
        if (this.roles.add(role)) {
            this.dirty = true;
            this.rebuildOverridesAndNotify();
            return true;
        }

        return false;
    }

    @Override
    public boolean remove(Role role) {
        if (this.roles.remove(role)) {
            this.dirty = true;
            this.rebuildOverridesAndNotify();
            return true;
        }

        return false;
    }

    @Override
    public Stream<Role> stream() {
        PlayerRolesConfig roleConfig = PlayerRolesConfig.get();
        return Stream.concat(
                this.roles.stream(),
                Stream.of(roleConfig.everyone())
        );
    }

    @Override
    public boolean hasRole(String name) {
        return name.equals(Role.EVERYONE) || this.roles.containsId(name);
    }

    @Override
    public <T> Stream<T> overrides(RoleOverrideType<T> type) {
        return this.overrides.streamOf(type);
    }

    @Override
    public <T> PermissionResult test(RoleOverrideType<T> type, Function<T, PermissionResult> function) {
        return this.overrides.test(type, function);
    }

    @Override
    @Nullable
    public <T> T select(RoleOverrideType<T> type) {
        return this.overrides.select(type);
    }

    public ListTag serialize() {
        ListTag list = new ListTag();
        for (Role role : this.roles) {
            list.add(StringTag.of(role.getName()));
        }
        return list;
    }

    public void deserialize(ListTag list) {
        PlayerRolesConfig config = PlayerRolesConfig.get();

        this.roles.clear();
        for (int i = 0; i < list.size(); i++) {
            String name = list.getString(i);
            Role role = config.get(name);
            if (role == null || name.equalsIgnoreCase(Role.EVERYONE)) {
                this.dirty = true;
                PlayerRoles.LOGGER.warn("Encountered invalid role '{}'", name);
                continue;
            }

            this.roles.add(role);
        }

        this.rebuildOverrides();
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public boolean isEmpty() {
        return this.roles.isEmpty();
    }

    public void copyFrom(PlayerRoleSet roles) {
        this.roles.clear();
        this.roles.addAll(roles.roles);
        this.dirty = roles.dirty;

        this.rebuildOverrides();
    }
}
