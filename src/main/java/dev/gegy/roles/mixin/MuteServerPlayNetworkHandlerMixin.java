package dev.gegy.roles.mixin;

import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.override.RoleOverrideType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 999)
public class MuteServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(
            method = "method_31286",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void broadcastMessage(String message, CallbackInfo ci) {
        if (this.player instanceof RoleOwner) {
            RoleReader roles = ((RoleOwner) this.player).getRoles();
            if (roles.test(RoleOverrideType.MUTE)) {
                ci.cancel();
            }
        }
    }
}
