package dev.gegy.roles.mixin.chat_type;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.override.ChatTypeOverride;
import net.minecraft.Optionull;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayer player;

	@ModifyArg(method = "broadcastChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ChatType;bind(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/ChatType$Bound;"))
	private ResourceKey<ChatType> overrideChatType(ResourceKey<ChatType> defaultChatType) {
		var roles = PlayerRolesApi.lookup().byPlayer(this.player);
		var override = roles.overrides().select(PlayerRoles.CHAT_TYPE);
		return Optionull.mapOrDefault(override, ChatTypeOverride::chatType, defaultChatType);
	}
}
