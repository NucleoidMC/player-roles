package dev.gegy.roles.store.db;

import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.store.PlayerRoleSet;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.UUID;

public final class PlayerRoleDatabase implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger(PlayerRoleDatabase.class);

    private final Uuid2BinaryDatabase binary;

    private PlayerRoleDatabase(Uuid2BinaryDatabase binary) {
        this.binary = binary;
    }

    public static PlayerRoleDatabase open(Path path) throws IOException {
        var binary = Uuid2BinaryDatabase.open(path);
        return new PlayerRoleDatabase(binary);
    }

    public void tryLoadInto(UUID uuid, PlayerRoleSet roles) {
        try {
            var bytes = this.binary.get(uuid);
            if (bytes != null) {
                try {
                    deserializeRoles(roles, bytes);
                } catch (IOException e) {
                    LOGGER.error("Failed to deserialize roles for {}, dropping", uuid, e);
                    this.binary.remove(uuid);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load roles for {}", uuid, e);
        }
    }

    public void trySave(UUID uuid, PlayerRoleSet roles) {
        try {
            if (!roles.isEmpty()) {
                var bytes = serializeRoles(roles);
                this.binary.put(uuid, bytes);
            } else {
                this.binary.remove(uuid);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save roles for {}", uuid, e);
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
        var config = PlayerRolesConfig.get();

        try (var input = new ByteArrayInputStream(bytes.array())) {
            var nbt = NbtIo.readCompressed(input);
            roles.deserialize(config, nbt.getList("roles", NbtType.STRING));
        }
    }

    @Override
    public void close() throws IOException {
        this.binary.close();
    }
}
