package dev.gegy.roles.override.permission;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.override.RoleChangeListener;
import dev.gegy.roles.override.RoleOverrideType;
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class PermissionKeyOverride implements RoleChangeListener {
    public static void register() {
        RoleOverrideType<PermissionKeyRules> override = RoleOverrideType.<PermissionKeyRules>builder()
                .key("permission_keys")
                .parse(PermissionKeyOverride::parse)
                .register();

        PermissionCheckEvent.EVENT.register((source, permission) -> {
            if (source instanceof ServerCommandSource) {
                Entity entity = ((ServerCommandSource) source).getEntity();
                if (entity instanceof RoleOwner) {
                    PermissionResult result = ((RoleOwner) entity).test(override, permissions -> permissions.test(permission));
                    return result.asTriState();
                }
            }
            return TriState.DEFAULT;
        });
    }

    private static <T> PermissionKeyRules parse(Dynamic<T> root) {
        PermissionKeyRules.Builder builder = PermissionKeyRules.builder();

        Map<Dynamic<T>, Dynamic<T>> map = root.getMapValues().result().orElse(ImmutableMap.of());
        for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : map.entrySet()) {
            String permission = entry.getKey().asString("");
            String ruleName = entry.getValue().asString("pass");
            builder.add(permission, PermissionResult.byName(ruleName));
        }

        return builder.build();
    }

    @Override
    public void notifyChange(@Nullable RoleOwner owner) {
        if (owner instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) owner;
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getCommandManager().sendCommandTree(player);
            }
        }
    }
}
