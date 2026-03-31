package dev.gegy.roles.api;

/**
 * Can be implemented on custom {@link net.minecraft.world.entity.Entity Entities} or
 * {@link net.minecraft.commands.CommandSourceStack ServerCommandSources}
 * to allow overriding the set of roles that the entity/source is assumed to have.
 *
 * @see VirtualServerCommandSource
 */
public interface RoleOwner {
    RoleReader getRoles();
}
