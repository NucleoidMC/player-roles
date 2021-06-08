package dev.gegy.roles.mixin.chat_style;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRoleSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 2000)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @ModifyVariable(
            method = "method_31286",
            ordinal = 0,
            at = @At(value = "STORE", ordinal = 0)
    )
    private Text formatChat(Text text, String message) {
        if (this.player instanceof PlayerRoleSource roleSource) {
            var roles = roleSource.getPlayerRoles();
            var chatStyle = roles.select(PlayerRoles.CHAT_STYLE);
            if (chatStyle != null) {
                return chatStyle.make(this.player.getDisplayName(), message);
            }
        }
        return text;
    }
}
