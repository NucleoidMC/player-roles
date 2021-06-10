package dev.gegy.roles.mixin;

import dev.gegy.roles.IdentifiableCommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerCommandSource.class)
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
