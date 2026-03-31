package dev.gegy.roles.mixin.command;

import dev.gegy.roles.override.command.CommandTestContext;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandManagerMixin {
    @Inject(method = "sendCommands", at = @At("HEAD"))
    private void beforeSendCommandTree(ServerPlayer player, CallbackInfo ci) {
        CommandTestContext.startSuggesting();
    }

    @Inject(method = "sendCommands", at = @At("RETURN"))
    private void afterSendCommandTree(ServerPlayer player, CallbackInfo ci) {
        CommandTestContext.stopSuggesting();
    }
}
