package dev.gegy.roles.mixin.permission_level;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.store.PlayerRoleManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "getProfilePermissions", at = @At("HEAD"), cancellable = true)
    public void getPermissionLevel(NameAndId profile, CallbackInfoReturnable<LevelBasedPermissionSet> ci) {
        var roles = PlayerRoleManager.get().peekRoles((MinecraftServer) (Object) this, profile.id());
        var permissionLevel = roles.overrides().select(PlayerRoles.PERMISSION_LEVEL);
        if (permissionLevel != null) {
            ci.setReturnValue(LevelBasedPermissionSet.forLevel(permissionLevel));
        }
    }
}
