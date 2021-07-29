package dev.gegy.roles.mixin.entity_selectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(MessageArgumentType.class)
public class MessageArgumentTypeMixin {
    @Redirect(method = "getMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
    private static boolean hasPermissionLevel(ServerCommandSource source, int level) {
        if (source.hasPermissionLevel(level)) return true;

        var roles = PlayerRolesApi.lookup().bySource(source);
        if (roles.overrides().test(PlayerRoles.ENTITY_SELECTORS)) {
            return true;
        }

        return false;
    }
}
