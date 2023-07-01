package dev.gegy.roles.mixin.name_decoration;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.mixin.TeamAccessor;
import dev.gegy.roles.override.NameDecorationOverride;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Bump priority to apply name decorations after other mods
@Mixin(value = ServerPlayerEntity.class, priority = 1001)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public Text getDisplayName() {
        var displayName = super.getDisplayName();

        var team = this.getScoreboardTeam();
        if (team == null || ((TeamAccessor) team).getFormattingColor() == Formatting.RESET) {
            var roles = PlayerRolesApi.lookup().byPlayer(this);

            var nameDecoration = roles.overrides().select(PlayerRoles.NAME_DECORATION);
            if (nameDecoration != null) {
                displayName = nameDecoration.apply(displayName.copy(), NameDecorationOverride.Context.CHAT);
            }
        }

        return displayName;
    }

    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    private void getPlayerListName(CallbackInfoReturnable<Text> cir) {
        var roles = PlayerRolesApi.lookup().byPlayer(this);

        var nameDecoration = roles.overrides().select(PlayerRoles.NAME_DECORATION);
        if (nameDecoration != null) {
            var currentName = cir.getReturnValue();
            if (currentName != null) {
                cir.setReturnValue(nameDecoration.apply(currentName.copy(), NameDecorationOverride.Context.TAB_LIST));
            } else {
                cir.setReturnValue(nameDecoration.apply(Text.literal(getGameProfile().getName()), NameDecorationOverride.Context.TAB_LIST));
            }
        }
    }
}
