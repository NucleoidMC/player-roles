package dev.gegy.roles.mixin.entity_selectors;

import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MessageArgumentType.MessageFormat.class)
public class MessageArgumentTypeMixin {
    @Redirect(method = "format(Lnet/minecraft/server/command/ServerCommandSource;)Lnet/minecraft/text/Text;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
    private boolean canUseEntitySelectors(ServerCommandSource source, int level) {
        if (source.hasPermissionLevel(level)) {
            return true;
        }

        var roles = PlayerRolesApi.lookup().bySource(source);
        return roles.overrides().test(PlayerRoles.ENTITY_SELECTORS);
    }
}
