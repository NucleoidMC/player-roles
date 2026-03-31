package dev.gegy.roles.mixin;

import com.mojang.brigadier.CommandDispatcher;
import dev.gegy.roles.IdentifiableCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;

@Mixin(CommandFunction.class)
public interface CommandFunctionMixin {
    @Inject(method = "fromLines", at = @At("HEAD"))
    private static <T extends ExecutionCommandSource<T>> void create(
            Identifier id, CommandDispatcher<T> dispatcher,
            T source, List<String> lines,
            CallbackInfoReturnable<CommandFunction<T>> ci
    ) {
        var identifiableSource = (IdentifiableCommandSource) source;
        identifiableSource.player_roles$setIdentityType(IdentifiableCommandSource.Type.FUNCTION);
    }
}
