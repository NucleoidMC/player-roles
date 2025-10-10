package dev.gegy.roles.mixin.command;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin {
    @WrapOperation(
            method = "sendToOps",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;isOperator(Lnet/minecraft/server/PlayerConfigEntry;)Z"
            )
    )
    private boolean shouldReceiveCommandFeedback(PlayerManager playerManager, PlayerConfigEntry profile, Operation<Boolean> original) {
        if (original.call(playerManager, profile)) {
            return true;
        }

        var player = playerManager.getPlayer(profile.id());
        var roles = PlayerRolesApi.lookup().byPlayer(player);
        return roles.overrides().test(PlayerRoles.COMMAND_FEEDBACK);
    }
}
