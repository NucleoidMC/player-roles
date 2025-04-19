package dev.gegy.roles.store;

import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.store.db.PlayerRoleDatabase;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class PlayerRoleManager {
    private static PlayerRoleManager instance;

    private final PlayerRoleDatabase database;
    private final Map<UUID, PlayerRoleSet> onlinePlayerRoles = new Object2ObjectOpenHashMap<>();

    private PlayerRoleManager(PlayerRoleDatabase database) {
        this.database = database;
    }

    public static void setup() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            instance = PlayerRoleManager.open(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            var instance = PlayerRoleManager.instance;
            if (instance != null) {
                PlayerRoleManager.instance = null;
                instance.close(server);
            }
        });
    }

    private static PlayerRoleManager open(MinecraftServer server) {
        try {
            var path = server.getSavePath(WorldSavePath.PLAYERDATA).resolve("player_roles");
            var database = PlayerRoleDatabase.open(path);
            return new PlayerRoleManager(database);
        } catch (IOException e) {
            throw new RuntimeException("failed to open player roles database");
        }
    }

    public static PlayerRoleManager get() {
        return Objects.requireNonNull(instance, "player role manager not initialized");
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        var config = PlayerRolesConfig.get();
        var roles = new PlayerRoleSet(config.everyone(), player);
        this.database.tryLoadInto(player.getUuid(), roles);
        this.onlinePlayerRoles.put(player.getUuid(), roles);
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        var roles = this.onlinePlayerRoles.remove(player.getUuid());
        if (roles != null && roles.isDirty()) {
            this.database.trySave(player.getUuid(), roles);
            roles.setDirty(false);
        }
    }

    public void onRoleReload(MinecraftServer server, PlayerRolesConfig config) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            var newRoles = new PlayerRoleSet(config.everyone(), player);
            var oldRoles = this.onlinePlayerRoles.put(player.getUuid(), newRoles);
            if (oldRoles != null) {
                newRoles.reloadFrom(config, oldRoles);
                newRoles.rebuildOverridesAndNotify();
            }
        }
    }

    private void close(MinecraftServer server) {
        try {
            for (var player : server.getPlayerManager().getPlayerList()) {
                this.onPlayerLeave(player);
            }
        } finally {
            IOUtils.closeQuietly(this.database);
        }
    }

    public void addLegacyRoles(ServerPlayerEntity player, List<String> names) {
        var roles = this.onlinePlayerRoles.get(player.getUuid());
        if (roles != null) {
            roles.deserialize(PlayerRolesConfig.get(), names);
            roles.setDirty(true);
        }
    }

    public <R> R updateRoles(MinecraftServer server, UUID uuid, Function<PlayerRoleSet, R> update) {
        var roles = this.onlinePlayerRoles.get(uuid);
        if (roles != null) {
            return update.apply(roles);
        } else {
            roles = this.loadOfflinePlayerRoles(uuid);

            try {
                return update.apply(roles);
            } finally {
                if (roles.isDirty()) {
                    this.database.trySave(uuid, roles);
                }
            }
        }
    }

    public PlayerRoleSet peekRoles(MinecraftServer server, UUID uuid) {
        var roles = this.onlinePlayerRoles.get(uuid);
        return roles != null ? roles : this.loadOfflinePlayerRoles(uuid);
    }

    private PlayerRoleSet loadOfflinePlayerRoles(UUID uuid) {
        var config = PlayerRolesConfig.get();

        PlayerRoleSet roles = new PlayerRoleSet(config.everyone(), null);
        this.database.tryLoadInto(uuid, roles);

        return roles;
    }

    @Nullable
    public PlayerRoleSet getOnlinePlayerRoles(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return this.onlinePlayerRoles.get(entity.getUuid());
        }
        return null;
    }
}
