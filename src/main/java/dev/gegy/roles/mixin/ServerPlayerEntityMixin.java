package dev.gegy.roles.mixin;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerWithRoles;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.store.PlayerRoleManager;
import dev.gegy.roles.store.PlayerRoleSet;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerWithRoles {
    @Unique
    private PlayerRoleSet playerRoleSet;

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public PlayerRoleSet loadPlayerRoles(PlayerRolesConfig config) {
        var self = (ServerPlayerEntity) (Object) this;

        var oldRoles = this.playerRoleSet;
        var newRoles = new PlayerRoleSet(config.everyone(), self);
        if (oldRoles != null) {
            this.reloadPlayerRoles(config, newRoles, oldRoles);
        } else {
            this.playerRoleSet = newRoles;
        }

        return newRoles;
    }

    private void reloadPlayerRoles(PlayerRolesConfig config, PlayerRoleSet newRoles, PlayerRoleSet oldRoles) {
        newRoles.reloadFrom(config, oldRoles);
        this.playerRoleSet = newRoles;
        newRoles.rebuildOverridesAndNotify();
    }

    @Override
    public PlayerRoleSet getPlayerRoleSet() {
        return Preconditions.checkNotNull(this.playerRoleSet, "player roles were not initialized");
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
        if (tag.contains("roles", NbtType.LIST)) {
            PlayerRoleManager.get().addLegacyRoles(this, tag.getList("roles", NbtType.STRING));
        }
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        var oldRoles = ((PlayerWithRoles) oldPlayer).getPlayerRoleSet();
        this.playerRoleSet = oldRoles.copy();
    }
}
