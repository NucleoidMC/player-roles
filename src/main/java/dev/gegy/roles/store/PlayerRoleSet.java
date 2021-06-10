package dev.gegy.roles.store;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.Role;
import dev.gegy.roles.api.RoleProvider;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.api.override.RoleOverrideReader;
import dev.gegy.roles.override.RoleOverrideMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;

public final class PlayerRoleSet implements RoleReader {
    private final Role everyoneRole;

    @Nullable
    private final ServerPlayerEntity player;

    private final ObjectSortedSet<Role> roles = new ObjectAVLTreeSet<>();
    private final RoleOverrideMap overrides = new RoleOverrideMap();

    private boolean dirty;

    public PlayerRoleSet(Role everyoneRole, @Nullable ServerPlayerEntity player) {
        this.everyoneRole = everyoneRole;
        this.player = player;

        this.rebuildOverrides();
    }

    public void rebuildOverridesAndNotify() {
        this.rebuildOverrides();
        if (this.player != null) {
            this.overrides.notifyChange(this.player);
        }
    }

    private void rebuildOverrides() {
        this.overrides.clear();
        this.stream().forEach(role -> this.overrides.addAll(role.getOverrides()));
    }

    public boolean add(Role role) {
        if (this.roles.add(role)) {
            this.dirty = true;
            this.rebuildOverridesAndNotify();
            return true;
        }

        return false;
    }

    public boolean remove(Role role) {
        if (this.roles.remove(role)) {
            this.dirty = true;
            this.rebuildOverridesAndNotify();
            return true;
        }

        return false;
    }

    @Override
    public Iterator<Role> iterator() {
        return this.roles.iterator();
    }

    @Override
    public Stream<Role> stream() {
        return Stream.concat(
                this.roles.stream(),
                Stream.of(this.everyoneRole)
        );
    }

    @Override
    public boolean has(Role role) {
        return role == this.everyoneRole || this.roles.contains(role);
    }

    @Override
    public RoleOverrideReader overrides() {
        return this.overrides;
    }

    public NbtList serialize() {
        var list = new NbtList();
        for (var role : this.roles) {
            list.add(NbtString.of(role.getId()));
        }
        return list;
    }

    public void deserialize(RoleProvider roleProvider, NbtList list) {
        this.roles.clear();

        for (int i = 0; i < list.size(); i++) {
            var name = list.getString(i);
            var role = roleProvider.get(name);
            if (role == null || name.equalsIgnoreCase(PlayerRoles.EVERYONE)) {
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

    public void reloadFrom(RoleProvider roleProvider, PlayerRoleSet roles) {
        var nbt = roles.serialize();
        this.deserialize(roleProvider, nbt);

        this.dirty |= roles.dirty;
    }

    public void copyFrom(PlayerRoleSet roles) {
        this.roles.clear();
        this.roles.addAll(roles.roles);
        this.dirty = roles.dirty;

        this.rebuildOverrides();
    }
}
