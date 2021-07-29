package dev.gegy.roles.mixin.entity_selectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(EntityArgumentType.class)
public class EntityArgumentTypeMixin {
    @Redirect(method = "listSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/CommandSource;hasPermissionLevel(I)Z"))
    private boolean hasPermissionLevel(CommandSource source, int level) {
        if (source.hasPermissionLevel(level)) return true;
        
        if (source instanceof ServerCommandSource serverSource) {
            var roles = PlayerRolesApi.lookup().bySource(serverSource);
            if (roles.overrides().test(PlayerRoles.ENTITY_SELECTORS)) {
                return true;
            }
        }

        return false;
    }
}
