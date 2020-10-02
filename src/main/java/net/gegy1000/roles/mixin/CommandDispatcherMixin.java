package net.gegy1000.roles.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.gegy1000.roles.override.command.CommandTestContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin<S> {
    @Inject(method = "getSmartUsage(Lcom/mojang/brigadier/tree/CommandNode;Ljava/lang/Object;)Ljava/util/Map;", at = @At("HEAD"))
    private void beforeGetSmartUsage(CommandNode<S> node, S source, CallbackInfoReturnable<Map<CommandNode<S>, String>> cir) {
        CommandTestContext.startSuggesting();
    }

    @Inject(method = "getSmartUsage(Lcom/mojang/brigadier/tree/CommandNode;Ljava/lang/Object;)Ljava/util/Map;", at = @At("RETURN"))
    private void afterGetSmartUsage(CommandNode<S> node, S source, CallbackInfoReturnable<Map<CommandNode<S>, String>> cir) {
        CommandTestContext.stopSuggesting();
    }
}
