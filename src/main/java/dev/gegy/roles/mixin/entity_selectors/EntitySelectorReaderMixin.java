package dev.gegy.roles.mixin.entity_selectors;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin {
    @WrapOperation(method = "shouldAllowAtSelectors", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/permission/PermissionPredicate;hasPermission(Lnet/minecraft/command/permission/Permission;)Z"))
    private static <S> boolean hasPermissionLevel(PermissionPredicate predicate, Permission permission, Operation<Boolean> original, S source) {
        if (original.call(predicate, permission)) {
            return true;
        }

        if (source instanceof ServerCommandSource serverSource) {
            var roles = PlayerRolesApi.lookup().bySource(serverSource);
            return roles.overrides().test(PlayerRoles.ENTITY_SELECTORS);
        }

        return false;
    }
}
