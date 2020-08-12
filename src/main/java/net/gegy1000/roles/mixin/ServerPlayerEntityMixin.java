package net.gegy1000.roles.mixin;

import com.mojang.authlib.GameProfile;
import net.gegy1000.roles.RoleCollection;
import net.gegy1000.roles.api.HasRoles;
import net.gegy1000.roles.override.NameStyleOverride;
import net.gegy1000.roles.override.RoleOverrideType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements HasRoles {
    private final RoleCollection roles = new RoleCollection(this);

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

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

    @Override
    public Text getDisplayName() {
        Text displayName = super.getDisplayName();

        if (this.getScoreboardTeam() == null) {
            NameStyleOverride nameFormat = this.roles.getHighest(RoleOverrideType.NAME_FORMAT);
            if (nameFormat != null) {
                displayName = nameFormat.apply(displayName.shallowCopy());
            }
        }

        return displayName;
    }
}
