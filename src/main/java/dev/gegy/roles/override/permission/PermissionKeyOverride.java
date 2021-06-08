package dev.gegy.roles.override.permission;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.PlayerRoleSource;
import dev.gegy.roles.api.override.RoleOverrideType;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PermissionKeyOverride {
    public static final Codec<PermissionKeyOverride> CODEC = PermissionKeyRules.CODEC.xmap(PermissionKeyOverride::new, override -> override.rules);

    private final PermissionKeyRules rules;

    public PermissionKeyOverride(PermissionKeyRules rules) {
        this.rules = rules;
    }

    public static void register() {
        RoleOverrideType<PermissionKeyOverride> override = RoleOverrideType.register("permission_keys", PermissionKeyOverride.CODEC)
                .withChangeListener(owner -> {
                    if (owner instanceof ServerPlayerEntity) {
                        ServerPlayerEntity player = (ServerPlayerEntity) owner;
                        MinecraftServer server = player.getServer();
                        if (server != null) {
                            server.getCommandManager().sendCommandTree(player);
                        }
                    }
                });

        PermissionCheckEvent.EVENT.register((source, permission) -> {
            if (source instanceof ServerCommandSource) {
                Entity entity = ((ServerCommandSource) source).getEntity();
                if (entity instanceof PlayerRoleSource) {
                    PermissionResult result = ((PlayerRoleSource) entity).getPlayerRoles().test(override, permissions -> permissions.rules.test(permission));
                    return result.asTriState();
                }
            }
            return TriState.DEFAULT;
        });
    }
}
