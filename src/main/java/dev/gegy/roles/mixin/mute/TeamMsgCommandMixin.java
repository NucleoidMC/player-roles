package dev.gegy.roles.mixin.mute;

import dev.gegy.roles.PlayerRoles;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
	@Inject(method = "execute", at = @At("HEAD"), cancellable = true)
	private static void execute(ServerCommandSource source, Entity entity, Team team, List<ServerPlayerEntity> recipients, SignedMessage message, CallbackInfo ci) {
		if (!PlayerRoles.trySendChat(source)) {
			ci.cancel();
		}
	}
}
