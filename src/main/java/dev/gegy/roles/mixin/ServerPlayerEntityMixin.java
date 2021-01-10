package dev.gegy.roles.mixin;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.override.NameStyleOverride;
import dev.gegy.roles.override.RoleOverrideType;
import dev.gegy.roles.store.PlayerRoleManager;
import dev.gegy.roles.store.PlayerRoleSet;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements RoleOwner {
    @Unique
    private final PlayerRoleSet roles = new PlayerRoleSet(this);

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public PlayerRoleSet getRoles() {
        return this.roles;
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("roles", NbtType.LIST)) {
            PlayerRoleManager.get().addLegacyRoles(this, tag.getList("roles", NbtType.STRING));
        }
    }

    @Override
    public Text getDisplayName() {
        Text displayName = super.getDisplayName();

        AbstractTeam team = this.getScoreboardTeam();
        if (team == null || ((TeamAccessor) team).getFormattingColor() == Formatting.RESET) {
            NameStyleOverride nameFormat = this.roles.select(RoleOverrideType.NAME_FORMAT);
            if (nameFormat != null) {
                displayName = nameFormat.apply(displayName.shallowCopy());
            }
        }

        return displayName;
    }
}
