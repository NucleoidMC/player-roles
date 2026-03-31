package dev.gegy.roles.mixin.entity_selectors;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {
    @WrapOperation(method = "checkPermissions", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/permissions/PermissionSet;hasPermission(Lnet/minecraft/server/permissions/Permission;)Z"))
    private boolean hasPermissionLevel(PermissionSet predicate, Permission permission, Operation<Boolean> original, CommandSourceStack source) {
        if (original.call(predicate, permission)) {
            return true;
        }

        var roles = PlayerRolesApi.lookup().bySource(source);
        return roles.overrides().test(PlayerRoles.ENTITY_SELECTORS);
    }
}
