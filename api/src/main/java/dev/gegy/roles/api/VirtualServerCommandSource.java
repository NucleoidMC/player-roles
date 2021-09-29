package dev.gegy.roles.api;

import com.mojang.brigadier.ResultConsumer;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * An extension of {@link ServerCommandSource} that allows specifying a
 * custom list of roles to use instead of the default empty set when no
 * entity is passed.
 */
public class VirtualServerCommandSource extends ServerCommandSource {
    private final RoleReader roles;

    public VirtualServerCommandSource(RoleReader roles, CommandOutput output, Vec3d pos, Vec2f rot, ServerWorld world, int level, String simpleName, Text name, MinecraftServer server, @Nullable Entity entity) {
        this(roles, output, pos, rot, world, level, simpleName, name, server, entity, false, (context, success, result) -> {}, EntityAnchorArgumentType.EntityAnchor.FEET);
    }

    protected VirtualServerCommandSource(RoleReader roles, CommandOutput output, Vec3d pos, Vec2f rot, ServerWorld world, int level, String simpleName, Text name, MinecraftServer server, @Nullable Entity entity, boolean silent, ResultConsumer<ServerCommandSource> consumer, EntityAnchorArgumentType.EntityAnchor entityAnchor) {
        super(output, pos, rot, world, level, simpleName, name, server, entity, silent, consumer, entityAnchor);
        this.roles = roles;
    }

    public RoleReader getRoles() {
        return this.roles;
    }
}
