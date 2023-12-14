package dev.gegy.roles.mixin;

import com.mojang.brigadier.CommandDispatcher;
import dev.gegy.roles.IdentifiableCommandSource;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CommandFunction.class)
public interface CommandFunctionMixin {
    @Inject(method = "create", at = @At("HEAD"))
    private static void create(
            Identifier id, CommandDispatcher<AbstractServerCommandSource> dispatcher,
            AbstractServerCommandSource source, List<String> lines,
            CallbackInfoReturnable<CommandFunction> ci
    ) {
        var identifiableSource = (IdentifiableCommandSource) source;
        identifiableSource.player_roles$setIdentityType(IdentifiableCommandSource.Type.FUNCTION);
    }
}
