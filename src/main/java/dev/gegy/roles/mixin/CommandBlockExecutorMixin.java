package dev.gegy.roles.mixin;

import dev.gegy.roles.IdentifiableCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CommandBlockExecutor.class)
public class CommandBlockExecutorMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;executeWithPrefix(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void executeCommand(World world, CallbackInfoReturnable<Boolean> cir, MinecraftServer server, ServerCommandSource source) {
        var identifiableSource = (IdentifiableCommandSource) source;
        identifiableSource.player_roles$setIdentityType(IdentifiableCommandSource.Type.COMMAND_BLOCK);
    }
}
