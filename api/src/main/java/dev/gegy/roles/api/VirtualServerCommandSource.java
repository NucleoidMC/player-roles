package dev.gegy.roles.api;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * An extension of {@link CommandSourceStack} that implements {@link RoleOwner}
 * to allow a custom list of roles to use instead of the default empty set when no
 * entity is passed.
 */
public class VirtualServerCommandSource extends CommandSourceStack implements RoleOwner {
    private final RoleReader roles;

    public VirtualServerCommandSource(RoleReader roles, CommandSource output, Vec3 pos, Vec2 rot, ServerLevel world, PermissionSet permissions, String simpleName, Component name, MinecraftServer server, @Nullable Entity entity) {
        super(output, pos, rot, world, permissions, simpleName, name, server, entity);
        this.roles = roles;
    }

    @Override
    public RoleReader getRoles() {
        return this.roles;
    }
}
