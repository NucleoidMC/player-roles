package dev.gegy.roles.mixin.mute;

import dev.gegy.roles.PlayerRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.TeamMsgCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
	@Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
	private static void execute(CommandSourceStack source, Entity entity, PlayerTeam team, List<ServerPlayer> recipients, PlayerChatMessage message, CallbackInfo ci) {
		if (!PlayerRoles.trySendChat(source)) {
			ci.cancel();
		}
	}
}
