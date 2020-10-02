package net.gegy1000.roles.mixin;

import com.mojang.authlib.GameProfile;
import net.gegy1000.roles.RoleCollection;
import net.gegy1000.roles.api.HasRoles;
import net.gegy1000.roles.override.CommandFeedbackOverride;
import net.gegy1000.roles.override.RoleOverrideType;
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
        if (player instanceof HasRoles) {
            RoleCollection roles = ((HasRoles) player).getRoles();
            CommandFeedbackOverride feedbackOverride = roles.getHighest(RoleOverrideType.COMMAND_FEEDBACK);
            if (feedbackOverride != null) {
                return feedbackOverride.shouldReceiveFeedback();
            }
        }

        return false;
    }
}
