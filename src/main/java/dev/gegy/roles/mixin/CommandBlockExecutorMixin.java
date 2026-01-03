package dev.gegy.roles.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.gegy.roles.IdentifiableCommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.CommandBlockExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandBlockExecutor.class)
public class CommandBlockExecutorMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;parseAndExecute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V"))
    private void executeCommand(ServerWorld world, CallbackInfoReturnable<Boolean> cir, @Local ServerCommandSource source) {
        var identifiableSource = (IdentifiableCommandSource) source;
        identifiableSource.player_roles$setIdentityType(IdentifiableCommandSource.Type.COMMAND_BLOCK);
    }
}
