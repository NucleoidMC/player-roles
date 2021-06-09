package dev.gegy.roles.store;

import dev.gegy.roles.api.PlayerRoleSource;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class PlayerRoleManager {
    private static final Logger LOGGER = LogManager.getLogger(PlayerRoleManager.class);

    private static PlayerRoleManager instance;

    private final PlayerIndexedDatabase database;

    private PlayerRoleManager(PlayerIndexedDatabase database) {
        this.database = database;
    }

    public static void setup() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            instance = PlayerRoleManager.open(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            var instance = PlayerRoleManager.instance;
            if (instance != null) {
                instance.close(server);
            }
            PlayerRoleManager.instance = null;
        });
    }

    private static PlayerRoleManager open(MinecraftServer server) {
        try {
            var path = server.getSavePath(WorldSavePath.PLAYERDATA).resolve("player_roles");
            var database = PlayerIndexedDatabase.open(path);
            return new PlayerRoleManager(database);
        } catch (IOException e) {
            throw new RuntimeException("failed to open player roles database");
        }
    }

    public static PlayerRoleManager get() {
        return Objects.requireNonNull(instance, "player role manager not initialized");
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        var roleSource = (PlayerRoleSource) player;
        this.loadRolesInto(player.getUuid(), roleSource.getPlayerRoles());
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        var roleSource = (PlayerRoleSource) player;
        var roles = roleSource.getPlayerRoles();
        if (roles.isDirty()) {
            try {
                this.saveRoles(player.getUuid(), roles);
                roles.setDirty(false);
            } catch (IOException e) {
                LOGGER.error("Failed to save roles for {}", player.getEntityName(), e);
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

    private void loadRolesInto(UUID uuid, PlayerRoleSet roles) {
        try {
            var bytes = this.database.get(uuid);
            if (bytes != null) {
                try {
                    deserializeRoles(roles, bytes);
                } catch (IOException e) {
                    LOGGER.error("Failed to deserialize roles for {}, dropping", uuid, e);
                    this.database.remove(uuid);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load roles for {}", uuid, e);
        }
    }

    private void saveRoles(UUID uuid, PlayerRoleSet roles) throws IOException {
        if (!roles.isEmpty()) {
            var bytes = serializeRoles(roles);
            this.database.put(uuid, bytes);
        } else {
            this.database.remove(uuid);
        }
    }

    private static ByteBuffer serializeRoles(PlayerRoleSet roles) throws IOException {
        var nbt = new NbtCompound();
        nbt.put("roles", roles.serialize());

        try (var output = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(nbt, output);
            return ByteBuffer.wrap(output.toByteArray());
        }
    }

    private static void deserializeRoles(PlayerRoleSet roles, ByteBuffer bytes) throws IOException {
        try (var input = new ByteArrayInputStream(bytes.array())) {
            var nbt = NbtIo.readCompressed(input);
            roles.deserialize(nbt.getList("roles", NbtType.STRING));
        }
    }

    public void addLegacyRoles(PlayerRoleSource owner, NbtList nbt) {
        var roles = owner.getPlayerRoles();
        roles.deserialize(nbt);
        roles.setDirty(true);
    }

    public <R> R updateRoles(MinecraftServer server, UUID uuid, Function<PlayerRoleSet, R> update) {
        var roleSource = getRoleSource(server, uuid);
        if (roleSource != null) {
            return update.apply(roleSource.getPlayerRoles());
        } else {
            var roles = new PlayerRoleSet(null);
            this.loadRolesInto(uuid, roles);

            try {
                return update.apply(roles);
            } finally {
                if (roles.isDirty()) {
                    try {
                        this.saveRoles(uuid, roles);
                    } catch (IOException e) {
                        LOGGER.error("Failed to save roles for {}", uuid, e);
                    }
                }
            }
        }
    }

    public PlayerRoleSet peekRoles(MinecraftServer server, UUID uuid) {
        var roleSource = getRoleSource(server, uuid);
        if (roleSource != null) {
            return roleSource.getPlayerRoles();
        } else {
            var roles = new PlayerRoleSet(null);
            this.loadRolesInto(uuid, roles);
            return roles;
        }
    }

    @Nullable
    private static PlayerRoleSource getRoleSource(MinecraftServer server, UUID uuid) {
        var player = server.getPlayerManager().getPlayer(uuid);
        return (PlayerRoleSource) player;
    }
}
