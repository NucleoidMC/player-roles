package dev.gegy.roles.mixin.mute;

import com.mojang.brigadier.Command;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
	@Inject(method = "execute", at = @At("HEAD"), cancellable = true)
	private static void execute(ServerCommandSource source, MessageArgumentType.SignedMessage message, CallbackInfoReturnable<Integer> ci) {
		var player = source.getPlayer();
		if (player != null) {
			var roles = PlayerRolesApi.lookup().byPlayer(player);
			if (roles.overrides().test(PlayerRoles.MUTE)) {
				PlayerRoles.sendMuteFeedback(player);
				ci.setReturnValue(Command.SINGLE_SUCCESS);
			}
		}
	}
}
