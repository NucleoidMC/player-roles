package dev.gegy.roles.mixin.chat_style;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.server.filter.TextStream;
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
            method = "handleMessage",
            ordinal = 1,
            at = @At(value = "STORE", ordinal = 0)
    )
    private Text formatChat(Text text, TextStream.Message message) {
        var roles = PlayerRolesApi.lookup().byPlayer(this.player);
        var chatFormat = roles.overrides().select(PlayerRoles.CHAT_FORMAT);
        if (chatFormat != null) {
            return chatFormat.make(this.player.getDisplayName(), message.getFiltered());
        }
        return text;
    }
}
