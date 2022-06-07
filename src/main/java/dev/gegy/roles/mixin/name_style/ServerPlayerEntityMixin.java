package dev.gegy.roles.mixin.name_style;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.mixin.TeamAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, profile, publicKey);
    }

    @Override
    public Text getDisplayName() {
        var displayName = super.getDisplayName();

        var team = this.getScoreboardTeam();
        if (team == null || ((TeamAccessor) team).getFormattingColor() == Formatting.RESET) {
            var roles = PlayerRolesApi.lookup().byPlayer(this);

            var nameFormat = roles.overrides().select(PlayerRoles.NAME_FORMAT);
            if (nameFormat != null) {
                displayName = nameFormat.apply(displayName.copyContentOnly());
            }
        }

        return displayName;
    }
}
