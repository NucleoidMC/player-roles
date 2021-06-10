package dev.gegy.roles.store;

import dev.gegy.roles.PlayerWithRoles;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.store.db.PlayerRoleDatabase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class PlayerRoleManager {
    private static PlayerRoleManager instance;

    private final PlayerRoleDatabase database;

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
        var roles = ((PlayerWithRoles) player).loadPlayerRoles(config);
        this.database.tryLoadInto(player.getUuid(), roles);
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        var roles = ((PlayerWithRoles) player).getPlayerRoleSet();
        if (roles.isDirty()) {
            this.database.trySave(player.getUuid(), roles);
            roles.setDirty(false);
        }
    }

    public void onRoleReload(MinecraftServer server, PlayerRolesConfig config) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            ((PlayerWithRoles) player).loadPlayerRoles(config);
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

    public void addLegacyRoles(PlayerWithRoles player, NbtList nbt) {
        var roles = player.getPlayerRoleSet();
        roles.deserialize(PlayerRolesConfig.get(), nbt);
        roles.setDirty(true);
    }

    public <R> R updateRoles(MinecraftServer server, UUID uuid, Function<PlayerRoleSet, R> update) {
        var roles = getOnlinePlayerRoles(server, uuid);
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
        var roles = getOnlinePlayerRoles(server, uuid);
        return roles != null ? roles : this.loadOfflinePlayerRoles(uuid);
    }

    private PlayerRoleSet loadOfflinePlayerRoles(UUID uuid) {
        var config = PlayerRolesConfig.get();

        PlayerRoleSet roles = new PlayerRoleSet(config.everyone(), null);
        this.database.tryLoadInto(uuid, roles);

        return roles;
    }

    @Nullable
    private static PlayerRoleSet getOnlinePlayerRoles(MinecraftServer server, UUID uuid) {
        var player = server.getPlayerManager().getPlayer(uuid);
        if (player != null) {
            return ((PlayerWithRoles) player).getPlayerRoleSet();
        } else {
            return null;
        }
    }
}
