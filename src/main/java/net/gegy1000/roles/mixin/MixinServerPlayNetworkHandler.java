package net.gegy1000.roles.mixin;

import net.gegy1000.roles.RoleCollection;
import net.gegy1000.roles.api.HasRoles;
import net.gegy1000.roles.override.ChatStyleOverride;
import net.gegy1000.roles.override.RoleOverrideType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(
            method = "onGameMessage",
            ordinal = 0,
            at = @At(value = "STORE", ordinal = 0)
    )
    private Text formatChat(Text message, ChatMessageC2SPacket packet) {
        if (this.player instanceof HasRoles) {
            RoleCollection roles = ((HasRoles) this.player).getRoles();
            ChatStyleOverride chatStyle = roles.getHighest(RoleOverrideType.CHAT_STYLE);
            if (chatStyle != null) {
                return chatStyle.make(this.player.getDisplayName().getString(), packet.getChatMessage());
            }
        }
        return message;
    }
}
