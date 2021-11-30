package dev.gegy.roles.mixin.mute;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
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
            method = "onChatMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        var roles = PlayerRolesApi.lookup().byPlayer(this.player);
        if (roles.overrides().test(PlayerRoles.MUTE)) {
            String message = packet.getChatMessage();
            if (!message.startsWith("/")) {
                PlayerRoles.sendMuteFeedback(this.player);
                ci.cancel();
            }
        }
    }
}
