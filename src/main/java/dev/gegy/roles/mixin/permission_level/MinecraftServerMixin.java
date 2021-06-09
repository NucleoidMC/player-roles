package dev.gegy.roles.mixin.permission_level;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract PlayerManager getPlayerManager();

    @Inject(method = "getPermissionLevel", at = @At("HEAD"), cancellable = true)
    public void getPermissionLevel(GameProfile profile, CallbackInfoReturnable<Integer> ci) {
        var player = this.getPlayerManager().getPlayer(profile.getId());
        if (player instanceof PlayerRoleSource roleSource) {
            var roles = roleSource.getPlayerRoles();
            var permissionLevel = roles.overrides().select(PlayerRoles.PERMISSION_LEVEL);
            if (permissionLevel != null) {
                ci.setReturnValue(permissionLevel);
            }
        }
    }
}
