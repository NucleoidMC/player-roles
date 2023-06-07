package dev.gegy.roles.mixin.chat_type;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.override.ChatTypeOverride;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Nullables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@ModifyArg(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/message/MessageType;params(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/entity/Entity;)Lnet/minecraft/network/message/MessageType$Parameters;"))
	private RegistryKey<MessageType> overrideChatType(RegistryKey<MessageType> defaultChatType) {
		var roles = PlayerRolesApi.lookup().byPlayer(this.player);
		var override = roles.overrides().select(PlayerRoles.CHAT_TYPE);
		return Nullables.mapOrElse(override, ChatTypeOverride::chatType, defaultChatType);
	}
}
