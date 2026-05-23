package dev.gegy.roles.override.legacypermission;

import com.mojang.serialization.Codec;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.override.RoleOverrideType;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionEvents;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record LegacyPermissionKeyOverride(LegacyPermissionKeyRules rules) {
    public static final Codec<LegacyPermissionKeyOverride> CODEC = LegacyPermissionKeyRules.CODEC.xmap(LegacyPermissionKeyOverride::new, override -> override.rules);

    public static void register() {
        var override = RoleOverrideType.register(PlayerRoles.identifier("permission_keys"), LegacyPermissionKeyOverride.CODEC)
                .withChangeListener(player -> {
                    var server = player.level().getServer();
                    server.getCommands().sendCommands(player);
                });

        if (FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0")) {
            registerLegacyPermissionsApiCompatibility(override);
        }

        registerFabricPermissionApiCompatibility(override);
    }

    private static void registerFabricPermissionApiCompatibility(RoleOverrideType<LegacyPermissionKeyOverride> override) {
        PermissionEvents.ON_REQUEST.register(new PermissionEvents.OnRequest() {
            @Override
            public @Nullable <T> T handlePermissionRequest(@NonNull PermissionContext context, @NonNull PermissionNode<T> node) {
                if (node.codec() != Codec.BOOL) {
                    return null;
                }

                var roles = PlayerRolesApi.lookup().byPermissionContext(context);
                var result = roles.overrides().test(override, permissions -> permissions.rules.test(node.key()));

                return node.cast(switch (result) {
                    case ALLOW -> Boolean.TRUE;
                    case DENY -> Boolean.FALSE;
                    default -> null;
                });
            }
        });
    }

    private static void registerLegacyPermissionsApiCompatibility(RoleOverrideType<LegacyPermissionKeyOverride> override) {
        PermissionCheckEvent.EVENT.register((source, permission) -> {
            if (source instanceof CommandSourceStack serverSource) {
                var roles = PlayerRolesApi.lookup().bySource(serverSource);
                var result = roles.overrides().test(override, permissions -> permissions.rules.test(permission));
                return switch (result) {
                    case ALLOW -> TriState.TRUE;
                    case DENY -> TriState.FALSE;
                    default -> TriState.DEFAULT;
                };
            }

            return TriState.DEFAULT;
        });
    }
}
