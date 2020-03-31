package net.gegy1000.roles.mixin;

import net.gegy1000.roles.RoleCollection;
import net.gegy1000.roles.api.HasRoles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity implements HasRoles {
    private final RoleCollection roles = new RoleCollection(this);

    @Override
    public RoleCollection getRoles() {
        return this.roles;
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        tag.put("roles", this.roles.serialize());
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        this.roles.deserialize(tag.getList("roles", 8));
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void copyFrom(ServerPlayerEntity old, boolean alive, CallbackInfo ci) {
        this.roles.copyFrom(((HasRoles) old).getRoles());
    }
}
