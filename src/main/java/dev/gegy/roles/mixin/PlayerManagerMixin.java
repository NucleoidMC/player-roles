package dev.gegy.roles.mixin;

import dev.gegy.roles.store.PlayerRoleManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        var roleManager = PlayerRoleManager.get();
        roleManager.onPlayerJoin(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        var roleManager = PlayerRoleManager.get();
        roleManager.onPlayerLeave(player);
    }
}
