package dev.gegy.roles;

import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class RolesMessageDecorator implements MessageDecorator {
	@Override
	public CompletableFuture<Text> decorate(@Nullable ServerPlayerEntity sender, Text message) {
		if (sender == null) {
			return CompletableFuture.completedFuture(message);
		}
		var roles = PlayerRolesApi.lookup().byPlayer(sender);
		var chatFormat = roles.overrides().select(PlayerRoles.CHAT_FORMAT);
		if (chatFormat != null) {
			return CompletableFuture.completedFuture(chatFormat.make(sender.getDisplayName(), message.getString()));
		} else {
			return CompletableFuture.completedFuture(message);
		}
	}
}
