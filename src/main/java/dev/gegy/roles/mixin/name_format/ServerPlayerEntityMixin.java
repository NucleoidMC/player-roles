package dev.gegy.roles.mixin.name_format;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.mixin.TeamAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerRoleSource {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public Text getDisplayName() {
        var displayName = super.getDisplayName();

        var team = this.getScoreboardTeam();
        if (team == null || ((TeamAccessor) team).getFormattingColor() == Formatting.RESET) {
            var nameFormat = this.getPlayerRoles().overrides().select(PlayerRoles.NAME_FORMAT);
            if (nameFormat != null) {
                displayName = nameFormat.apply(displayName.shallowCopy());
            }
        }

        return displayName;
    }
}
