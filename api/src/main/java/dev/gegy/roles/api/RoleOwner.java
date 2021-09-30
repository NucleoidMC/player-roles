package dev.gegy.roles.api;

/**
 * Can be implemented on custom {@link net.minecraft.entity.Entity Entities} or
 * {@link net.minecraft.server.command.ServerCommandSource ServerCommandSources}
 * to allow overriding the set of roles that the entity/source is assumed to have.
 *
 * @see VirtualServerCommandSource
 */
public interface RoleOwner {
    RoleReader getRoles();
}
