package dev.gegy.roles.mixin.command;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.api.RoleReader;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin {
    @Redirect(
            method = "sendToOps",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;isOperator(Lcom/mojang/authlib/GameProfile;)Z"
            )
    )
    private boolean shouldReceiveCommandFeedback(PlayerManager playerManager, GameProfile profile) {
        if (playerManager.isOperator(profile)) {
            return true;
        }

        ServerPlayerEntity player = playerManager.getPlayer(profile.getId());
        if (player instanceof PlayerRoleSource) {
            RoleReader roles = ((PlayerRoleSource) player).getPlayerRoles();
            return roles.test(PlayerRoles.COMMAND_FEEDBACK);
        }

        return false;
    }
}
