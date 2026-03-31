package dev.gegy.roles.mixin;

import dev.gegy.roles.IdentifiableCommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CommandSourceStack.class)
public class ServerCommandSourceMixin implements IdentifiableCommandSource {
    @Unique
    private Type player_roles$identityType = Type.UNKNOWN;

    @Override
    public void player_roles$setIdentityType(Type type) {
        this.player_roles$identityType = type;
    }

    @Override
    public Type player_roles$getIdentityType() {
        return this.player_roles$identityType;
    }
}
