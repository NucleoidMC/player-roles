package dev.gegy.roles.mixin.entity_selectors;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntitySelectorParser.class)
public class EntitySelectorReaderMixin {
    @WrapOperation(method = "allowSelectors(Ljava/lang/Object;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/permissions/PermissionSet;hasPermission(Lnet/minecraft/server/permissions/Permission;)Z"))
    private static <S> boolean hasPermissionLevel(PermissionSet predicate, Permission permission, Operation<Boolean> original, S source) {
        if (original.call(predicate, permission)) {
            return true;
        }

        if (source instanceof CommandSourceStack serverSource) {
            var roles = PlayerRolesApi.lookup().bySource(serverSource);
            return roles.overrides().test(PlayerRoles.ENTITY_SELECTORS);
        }

        return false;
    }
}
