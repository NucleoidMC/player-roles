package dev.gegy.roles.mixin;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.store.PlayerRoleManager;
import dev.gegy.roles.store.PlayerRoleSet;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerRoleSource {
    @Unique
    private final PlayerRoleSet playerRoleSet = new PlayerRoleSet((ServerPlayerEntity) (Object) this);

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void notifyPlayerRoleReload() {
        this.playerRoleSet.rebuildOverridesAndNotify();
    }

    @Override
    public PlayerRoleSet getPlayerRoles() {
        return this.playerRoleSet;
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("roles", NbtType.LIST)) {
            PlayerRoleManager.get().addLegacyRoles(this, tag.getList("roles", NbtType.STRING));
        }
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.playerRoleSet.copyFrom(((PlayerRoleSource) oldPlayer).getPlayerRoles());
    }
}
