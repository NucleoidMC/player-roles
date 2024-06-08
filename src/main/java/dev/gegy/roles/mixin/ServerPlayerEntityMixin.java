package dev.gegy.roles.mixin;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.store.PlayerRoleManager;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerEntity.class, priority = 900)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
        if (tag.contains("roles", NbtElement.LIST_TYPE)) {
            PlayerRoleManager.get().addLegacyRoles((ServerPlayerEntity) (Object) this, tag.getList("roles", NbtElement.STRING_TYPE));
        }
    }
}
