package dev.gegy.roles.mixin.mute;

import com.mojang.brigadier.Command;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text message, CallbackInfoReturnable<Integer> ci) {
        var entity = source.getEntity();

        if (entity instanceof ServerPlayerEntity player) {
            var roles = PlayerRolesApi.lookup().byPlayer(player);
            if (roles.overrides().test(PlayerRoles.MUTE)) {
                PlayerRoles.sendMuteFeedback(player);
                ci.setReturnValue(Command.SINGLE_SUCCESS);
            }
        }
    }
}
