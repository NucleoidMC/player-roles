package dev.gegy.roles.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface RoleLookup {
    RoleLookup EMPTY = new RoleLookup() {
        @Override
        @NotNull
        public RoleReader byEntity(Entity entity) {
            return RoleReader.EMPTY;
        }

        @Override
        @NotNull
        public RoleReader bySource(CommandSourceStack source) {
            return RoleReader.EMPTY;
        }
    };

    @NotNull
    default RoleReader byPlayer(Player player) {
        return this.byEntity(player);
    }

    @NotNull
    RoleReader byEntity(Entity entity);

    @NotNull
    RoleReader bySource(CommandSourceStack source);
}
