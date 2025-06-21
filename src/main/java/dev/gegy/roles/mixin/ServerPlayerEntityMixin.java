package dev.gegy.roles.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import dev.gegy.roles.store.PlayerRoleManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerEntity.class, priority = 900)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "readCustomData", at = @At("RETURN"))
    private void readLegacyRolesData(ReadView view, CallbackInfo ci) {
        view.read("roles", Codec.STRING.listOf()).ifPresent(names -> {
            PlayerRoleManager.get().addLegacyRoles((ServerPlayerEntity) (Object) this, names);
        });
    }
}
