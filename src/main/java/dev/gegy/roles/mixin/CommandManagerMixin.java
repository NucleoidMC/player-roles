package dev.gegy.roles.mixin;

import dev.gegy.roles.override.command.CommandTestContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "sendCommandTree", at = @At("HEAD"))
    private void beforeSendCommandTree(ServerPlayerEntity player, CallbackInfo ci) {
        CommandTestContext.startSuggesting();
    }

    @Inject(method = "sendCommandTree", at = @At("RETURN"))
    private void afterSendCommandTree(ServerPlayerEntity player, CallbackInfo ci) {
        CommandTestContext.stopSuggesting();
    }
}
