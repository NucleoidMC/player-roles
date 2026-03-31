package dev.gegy.roles.mixin.bypass_limit;

import dev.gegy.roles.PlayerRoles;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedPlayerList.class)
public class DedicatedPlayerManagerMixin extends PlayerList {
    public DedicatedPlayerManagerMixin(MinecraftServer server, LayeredRegistryAccess<RegistryLayer> registryManager, PlayerDataStorage saveHandler, NotificationService managementListener) {
        super(server, registryManager, saveHandler, managementListener);
    }

    @Inject(method = "canBypassPlayerLimit", at = @At("HEAD"), cancellable = true)
    private void canPlayerBypassLimitWithRole(NameAndId profile, CallbackInfoReturnable<Boolean> cir) {
        if (profile.id() != null) {
            if (PlayerRoles.canBypassPlayerLimit(this.getServer(), profile.id())) {
                cir.setReturnValue(true);
            }
        }
    }
}
