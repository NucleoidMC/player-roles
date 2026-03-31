package dev.gegy.roles.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.gegy.roles.IdentifiableCommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseCommandBlock.class)
public class CommandBlockExecutorMixin {
    @Inject(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"))
    private void executeCommand(ServerLevel world, CallbackInfoReturnable<Boolean> cir, @Local CommandSourceStack source) {
        var identifiableSource = (IdentifiableCommandSource) source;
        identifiableSource.player_roles$setIdentityType(IdentifiableCommandSource.Type.COMMAND_BLOCK);
    }
}
