package dev.gegy.roles.override.permission;

import com.mojang.serialization.Codec;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.override.RoleOverrideType;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionEvents;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record PermissionKeyOverride(PermissionKeyRules rules) {
    public static final Codec<PermissionKeyOverride> CODEC = PermissionKeyRules.CODEC.xmap(PermissionKeyOverride::new, override -> override.rules);

    public static void register() {
        var override = RoleOverrideType.register(PlayerRoles.identifier("permissions"), PermissionKeyOverride.CODEC)
                .withChangeListener(player -> {
                    var server = player.level().getServer();
                    server.getCommands().sendCommands(player);
                });

        registerFabricPermissionApiCompatibility(override);
    }

    private static void registerFabricPermissionApiCompatibility(RoleOverrideType<PermissionKeyOverride> override) {
        PermissionEvents.ON_REQUEST.register(new PermissionEvents.OnRequest() {
            @Override
            public @Nullable <T> T handlePermissionRequest(@NonNull PermissionContext context, @NonNull PermissionNode<T> node) {
                var roles = PlayerRolesApi.lookup().byPermissionContext(context);
                for (var x : roles.overrides().get(override)) {
                    var res = x.rules().test(node.key(), node.codec());
                    if (res != null) {
                        return res;
                    }
                }

                return null;
            }
        });
    }
}
