package dev.gegy.roles.store;

import dev.gegy.roles.api.RoleOwner;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

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

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PlayerRoleManager instance = PlayerRoleManager.instance;
            if (instance != null) {
                instance.close(server);
            }
            PlayerRoleManager.instance = null;
        });
    }

    private static PlayerRoleManager open(MinecraftServer server) {
        try {
            Path path = server.getSavePath(WorldSavePath.PLAYERDATA).resolve("player_roles");
            PlayerIndexedDatabase database = PlayerIndexedDatabase.open(path);
            return new PlayerRoleManager(database);
        } catch (IOException e) {
            throw new RuntimeException("failed to open player roles database");
        }
    }

    public static PlayerRoleManager get() {
        return Objects.requireNonNull(instance, "player role manager not initialized");
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        RoleOwner roleOwner = (RoleOwner) player;
        this.loadRolesInto(player.getUuid(), roleOwner.getRoles());
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        RoleOwner roleOwner = (RoleOwner) player;
        PlayerRoleSet roles = roleOwner.getRoles();
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
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                this.onPlayerLeave(player);
            }
        } finally {
            IOUtils.closeQuietly(this.database);
        }
    }

    private void loadRolesInto(UUID uuid, PlayerRoleSet roles) {
        try {
            ByteBuffer bytes = this.database.get(uuid);
            if (bytes != null) {
                deserializeRoles(roles, bytes);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load roles for {}", uuid, e);
        }
    }

    private void saveRoles(UUID uuid, PlayerRoleSet roles) throws IOException {
        if (!roles.isEmpty()) {
            ByteBuffer bytes = serializeRoles(roles);
            this.database.put(uuid, bytes);
        } else {
            this.database.remove(uuid);
        }
    }

    private static ByteBuffer serializeRoles(PlayerRoleSet roles) throws IOException {
        CompoundTag nbt = new CompoundTag();
        nbt.put("roles", roles.serialize());

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(nbt, output);
            return ByteBuffer.wrap(output.toByteArray());
        }
    }

    private static void deserializeRoles(PlayerRoleSet roles, ByteBuffer bytes) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes.array())) {
            CompoundTag nbt = NbtIo.readCompressed(input);
            roles.deserialize(nbt.getList("roles", NbtType.STRING));
        }
    }

    public void addLegacyRoles(RoleOwner owner, ListTag nbt) {
        PlayerRoleSet roles = owner.getRoles();
        roles.deserialize(nbt);
        roles.setDirty(true);
    }
}
