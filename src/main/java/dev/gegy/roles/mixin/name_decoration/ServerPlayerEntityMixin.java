package dev.gegy.roles.mixin.name_decoration;

import com.mojang.authlib.GameProfile;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.override.NameDecorationOverride;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Bump priority to apply name decorations after other mods
@Mixin(value = ServerPlayer.class, priority = 1001)
public abstract class ServerPlayerEntityMixin extends Player {
    private ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public Component getDisplayName() {
        var displayName = super.getDisplayName();

        var team = this.getTeam();
        if (team == null || team.getColor().isEmpty()) {
            var roles = PlayerRolesApi.lookup().byPlayer(this);

            var nameDecoration = roles.overrides().select(PlayerRoles.NAME_DECORATION);
            if (nameDecoration != null) {
                displayName = nameDecoration.apply(displayName.copy(), NameDecorationOverride.Context.CHAT);
            }
        }

        return displayName;
    }

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void getPlayerListName(CallbackInfoReturnable<Component> cir) {
        var roles = PlayerRolesApi.lookup().byPlayer(this);

        var nameDecoration = roles.overrides().select(PlayerRoles.NAME_DECORATION);
        if (nameDecoration != null) {
            var currentName = cir.getReturnValue();
            if (currentName != null) {
                cir.setReturnValue(nameDecoration.apply(currentName.copy(), NameDecorationOverride.Context.TAB_LIST));
            } else {
                cir.setReturnValue(nameDecoration.apply(Component.literal(getGameProfile().name()), NameDecorationOverride.Context.TAB_LIST));
            }
        }
    }
}
