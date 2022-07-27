package dev.gegy.roles.mixin.mute;

import com.mojang.brigadier.Command;
import dev.gegy.roles.PlayerRoles;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
	@Inject(method = "execute", at = @At("HEAD"), cancellable = true)
	private static void execute(ServerCommandSource source, MessageArgumentType.SignedMessage message, CallbackInfoReturnable<Integer> ci) {
		if (!PlayerRoles.trySendChat(source)) {
			final SignedMessage signedMessage = message.signedArgument();
			if (!signedMessage.headerSignature().isEmpty()) {
				source.getServer().getPlayerManager().sendMessageHeader(signedMessage, Set.of());
			}
			ci.setReturnValue(Command.SINGLE_SUCCESS);
		}
	}
}
