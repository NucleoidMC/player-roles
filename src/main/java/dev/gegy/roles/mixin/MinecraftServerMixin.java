package dev.gegy.roles.mixin;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.api.RoleReader;
import dev.gegy.roles.api.HasRoles;
import dev.gegy.roles.override.RoleOverrideType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
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
        ServerPlayerEntity player = this.getPlayerManager().getPlayer(profile.getId());
        if (player instanceof HasRoles) {
            RoleReader roles = ((HasRoles) player).getRoles();
            Integer permissionLevel = roles.select(RoleOverrideType.PERMISSION_LEVEL);
            if (permissionLevel != null) {
                ci.setReturnValue(permissionLevel);
            }
        }
    }
}
