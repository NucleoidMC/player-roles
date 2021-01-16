package dev.gegy.roles.mixin;

import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.override.ChatFormatOverride;
import dev.gegy.roles.override.RoleOverrideType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1001)
public class FormatServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(
            method = "method_31286",
            ordinal = 0,
            at = @At(value = "STORE", ordinal = 0)
    )
    private Text formatChat(Text text, String message) {
        if (this.player instanceof RoleOwner) {
            RoleReader roles = ((RoleOwner) this.player).getRoles();
            ChatFormatOverride chatStyle = roles.select(RoleOverrideType.CHAT_STYLE);
            if (chatStyle != null) {
                return chatStyle.make(this.player.getDisplayName(), message);
            }
        }
        return text;
    }
}
