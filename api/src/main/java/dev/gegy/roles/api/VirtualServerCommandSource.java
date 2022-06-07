package dev.gegy.roles.api;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.CommandArgumentSigner;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * An extension of {@link ServerCommandSource} that implements {@link RoleOwner}
 * to allow a custom list of roles to use instead of the default empty set when no
 * entity is passed.
 */
public class VirtualServerCommandSource extends ServerCommandSource implements RoleOwner {
    private final RoleReader roles;

    public VirtualServerCommandSource(RoleReader roles, CommandOutput output, Vec3d pos, Vec2f rot, ServerWorld world, int level, String simpleName, Text name, MinecraftServer server, @Nullable Entity entity) {
        super(output, pos, rot, world, level, simpleName, name, server, entity);
        this.roles = roles;
    }

    @Override
    public RoleReader getRoles() {
        return this.roles;
    }
}
