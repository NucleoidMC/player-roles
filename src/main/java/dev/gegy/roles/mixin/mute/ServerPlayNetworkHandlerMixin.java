package dev.gegy.roles.mixin.mute;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 999)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "decorateChat", at = @At(value = "HEAD"), cancellable = true)
    private void onChatMessage(CallbackInfoReturnable<CompletableFuture<Text>> ci) {
        var roles = PlayerRolesApi.lookup().byPlayer(this.player);
        if (roles.overrides().test(PlayerRoles.MUTE)) {
            PlayerRoles.sendMuteFeedback(this.player);
            ci.cancel();
            // TODO need to set the return value to something that won't break everything
            //ci.setReturnValue(new CompletableFuture<>());
        }
    }
}
