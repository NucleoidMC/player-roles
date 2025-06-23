package dev.gegy.roles.mixin.entity_selectors;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {
    @Redirect(method = "checkSourcePermission", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;hasElevatedPermissions()Z"))
    private boolean hasPermissionLevel(ServerCommandSource instance) {
        if (instance.hasElevatedPermissions()) {
            return true;
        }

        var roles = PlayerRolesApi.lookup().bySource(instance);
        return roles.overrides().test(PlayerRoles.ENTITY_SELECTORS);
    }
}
