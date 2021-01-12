package dev.gegy.roles.store;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Very simple persistent database indexed by UUID. This implementation is not optimized for performance, but rather
 * for simplicity.
 */
public final class PlayerIndexedDatabase implements Closeable {
    private static final int UUID_BYTES = 16;
    private static final int SIZE_BYTES = 4;
    private static final int HEADER_BYTES = UUID_BYTES + SIZE_BYTES;

    private static final long NULL_POINTER = -1;

    final FileChannel file;
    final Object2LongMap<UUID> pointers;

    final ByteBuffer uuidBytes = ByteBuffer.allocate(16);
    final ByteBuffer sizeBytes = ByteBuffer.allocate(4);
    final LongBuffer uuidBuffer = this.uuidBytes.asLongBuffer();
    final IntBuffer sizeBuffer = this.sizeBytes.asIntBuffer();

    final ByteBuffer[] headerBytes = new ByteBuffer[] { this.uuidBytes, this.sizeBytes };

    private PlayerIndexedDatabase(FileChannel file, Object2LongMap<UUID> pointers) {
        this.file = file;
        this.pointers = pointers;
        this.pointers.defaultReturnValue(NULL_POINTER);
    }

    public static PlayerIndexedDatabase open(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        Object2LongMap<UUID> pointers = buildPointerIndex(channel);
        return new PlayerIndexedDatabase(channel, pointers);
    }

    private static Object2LongMap<UUID> buildPointerIndex(FileChannel channel) throws IOException {
        Object2LongMap<UUID> pointers = new Object2LongOpenHashMap<>();

        ByteBuffer uuidBytes = ByteBuffer.allocate(16);
        ByteBuffer sizeBytes = ByteBuffer.allocate(4);
        LongBuffer uuidBuffer = uuidBytes.asLongBuffer();
        IntBuffer sizeBuffer = sizeBytes.asIntBuffer();

        int pointer = 0;

        long fileSize = channel.size();
        while (pointer < fileSize) {
            channel.position(pointer);

            uuidBytes.position(0);
            sizeBytes.position(0);
            channel.read(uuidBytes);
            channel.read(sizeBytes);

            UUID uuid = new UUID(uuidBuffer.get(0), uuidBuffer.get(1));
            int size = sizeBuffer.get(0);

            pointers.put(uuid, pointer);

            pointer += HEADER_BYTES + size;
        }

        return pointers;
    }

    @Nullable
    public ByteBuffer get(UUID key) throws IOException {
        long pointer = this.pointers.getLong(key);
        if (pointer == NULL_POINTER) {
            return null;
        }

        this.file.position(pointer + UUID_BYTES);

        this.sizeBytes.position(0);
        this.file.read(this.sizeBytes);

        int size = this.sizeBuffer.get(0);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        this.file.read(buffer);

        return buffer;
    }

    public void put(UUID key, ByteBuffer bytes) throws IOException {
        long pointer = this.pointers.getLong(key);
        if (pointer == NULL_POINTER) {
            this.push(key, bytes);
        } else {
            this.update(pointer, bytes);
        }
    }

    public boolean remove(UUID key) throws IOException {
        long pointer = this.pointers.removeLong(key);
        if (pointer == NULL_POINTER) {
            return false;
        }

        this.file.position(pointer + UUID_BYTES);

        this.sizeBytes.position(0);
        this.file.read(this.sizeBytes);

        int size = this.sizeBuffer.get(0);

        long endPointer = pointer + HEADER_BYTES + size;
        this.shiftAfter(endPointer, -(size + HEADER_BYTES));

        return true;
    }

    private void push(UUID key, ByteBuffer bytes) throws IOException {
        long pointer = this.file.size();
        this.file.position(pointer);

        this.file.write(this.writeHeader(key, bytes.capacity()));
        this.file.write(bytes);

        this.pointers.put(key, pointer);
    }

    private ByteBuffer[] writeHeader(UUID key, int size) {
        this.uuidBytes.position(0);
        this.uuidBuffer.position(0);
        this.uuidBuffer.put(key.getMostSignificantBits()).put(key.getLeastSignificantBits());

        this.sizeBytes.position(0);
        this.sizeBuffer.position(0);
        this.sizeBuffer.put(size);

        return this.headerBytes;
    }

    private void update(long pointer, ByteBuffer bytes) throws IOException {
        this.file.position(pointer + UUID_BYTES);

        this.sizeBytes.position(0);
        this.file.read(this.sizeBytes);

        int lastSize = this.sizeBuffer.get(0);
        int newSize = bytes.capacity();
        if (lastSize != newSize) {
            long endPointer = pointer + HEADER_BYTES + lastSize;
            this.shiftAfter(endPointer, newSize - lastSize);
        }

        this.sizeBytes.position(0);
        this.sizeBuffer.position(0);
        this.sizeBuffer.put(newSize);

        this.file.position(pointer + UUID_BYTES);
        this.file.write(this.sizeBytes);
        this.file.write(bytes);
    }

    private void shiftAfter(long source, int amount) throws IOException {
        long destination = source + amount;
        long length = this.file.size() - source;

        if (amount > 0) {
            // make space for the shifted data
            this.file.position(this.file.size());
            this.file.write(ByteBuffer.allocate(amount));
        }

        // shift the data along
        this.file.position(destination);
        this.file.transferTo(source, length, this.file);

        if (amount < 0) {
            // shrink the file if it got smaller
            this.file.truncate(this.file.size() + amount);
        }

        // shift all pointers
        for (Object2LongMap.Entry<UUID> entry : Object2LongMaps.fastIterable(this.pointers)) {
            long pointer = entry.getLongValue();
            if (pointer >= source) {
                entry.setValue(pointer + amount);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.file.close();
    }
}
