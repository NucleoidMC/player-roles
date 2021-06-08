package dev.gegy.roles.mixin.mute;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRoleSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 999)
public class ServerPlayNetworkHandlerMixin {
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
        if (this.player instanceof PlayerRoleSource roleSource) {
            var roles = roleSource.getPlayerRoles();
            if (roles.test(PlayerRoles.MUTE)) {
                PlayerRoles.sendMuteFeedback(this.player);
                ci.cancel();
            }
        }
    }
}
