package dev.gegy.roles.mixin.command;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSourceStack.class)
public class ServerCommandSourceMixin {
    @WrapOperation(
            method = "broadcastToAdmins",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;isOp(Lnet/minecraft/server/players/NameAndId;)Z"
            )
    )
    private boolean shouldReceiveCommandFeedback(PlayerList playerManager, NameAndId profile, Operation<Boolean> original) {
        if (original.call(playerManager, profile)) {
            return true;
        }

        var player = playerManager.getPlayer(profile.id());
        var roles = PlayerRolesApi.lookup().byPlayer(player);
        return roles.overrides().test(PlayerRoles.COMMAND_FEEDBACK);
    }
}
