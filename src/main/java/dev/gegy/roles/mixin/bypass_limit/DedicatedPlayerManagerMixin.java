package dev.gegy.roles.mixin.bypass_limit;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerRoles;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedPlayerManager.class)
public class DedicatedPlayerManagerMixin extends PlayerManager {
    public DedicatedPlayerManagerMixin(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, WorldSaveHandler saveHandler, int maxPlayers) {
        super(server, registryManager, saveHandler, maxPlayers);
    }

    @Inject(method = "canBypassPlayerLimit", at = @At("HEAD"), cancellable = true)
    private void canPlayerBypassLimitWithRole(GameProfile profile, CallbackInfoReturnable<Boolean> cir) {
        if (profile.getId() != null) {
            if (PlayerRoles.canBypassPlayerLimit(this.getServer(), profile.getId())) {
                cir.setReturnValue(true);
            }
        }
    }
}
