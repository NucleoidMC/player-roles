package dev.gegy.roles.override.permission;

import com.mojang.serialization.Codec;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.api.override.RoleOverrideType;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.command.ServerCommandSource;

public record PermissionKeyOverride(PermissionKeyRules rules) {
    public static final Codec<PermissionKeyOverride> CODEC = PermissionKeyRules.CODEC.xmap(PermissionKeyOverride::new, override -> override.rules);

    public static void register() {
        var override = RoleOverrideType.register(PlayerRoles.identifier("permission_keys"), PermissionKeyOverride.CODEC)
                .withChangeListener(player -> {
                    var server = player.getServer();
                    if (server != null) {
                        server.getCommandManager().sendCommandTree(player);
                    }
                });

        PermissionCheckEvent.EVENT.register((source, permission) -> {
            if (source instanceof ServerCommandSource serverSource) {
                var entity = serverSource.getEntity();
                if (entity instanceof PlayerRoleSource roleSource) {
                    var roles = roleSource.getPlayerRoles();
                    var result = roles.overrides().test(override, permissions -> permissions.rules.test(permission));
                    return result.asTriState();
                }
            }
            return TriState.DEFAULT;
        });
    }
}
