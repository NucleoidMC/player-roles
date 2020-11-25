package dev.gegy.roles.mixin;

import dev.gegy.roles.RoleCollection;
import dev.gegy.roles.api.HasRoles;
import dev.gegy.roles.override.ChatFormatOverride;
import dev.gegy.roles.override.RoleOverrideType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(
            method = "method_31286",
            ordinal = 0,
            at = @At(value = "STORE", ordinal = 0)
    )
    private Text formatChat(Text text, String message) {
        if (this.player instanceof HasRoles) {
            RoleCollection roles = ((HasRoles) this.player).getRoles();
            ChatFormatOverride chatStyle = roles.getHighest(RoleOverrideType.CHAT_STYLE);
            if (chatStyle != null) {
                return chatStyle.make(this.player.getDisplayName(), message);
            }
        }
        return text;
    }

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
        if (this.player instanceof HasRoles) {
            RoleCollection roles = ((HasRoles) this.player).getRoles();
            if (roles.test(RoleOverrideType.MUTE)) {
                ci.cancel();
            }
        }
    }
}
