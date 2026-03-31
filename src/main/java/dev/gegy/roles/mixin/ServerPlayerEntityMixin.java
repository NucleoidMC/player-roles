package dev.gegy.roles.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import dev.gegy.roles.store.PlayerRoleManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 900)
public abstract class ServerPlayerEntityMixin extends Player {
    private ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readLegacyRolesData(ValueInput view, CallbackInfo ci) {
        view.read("roles", Codec.STRING.listOf()).ifPresent(names -> {
            PlayerRoleManager.get().addLegacyRoles((ServerPlayer) (Object) this, names);
        });
    }
}
