package dev.gegy.roles.store;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private static final int MAX_VALUE_SIZE = 4 * 1024 * 1024;

    private static final int UUID_BYTES = 16;
    private static final int SIZE_BYTES = 4;
    private static final int HEADER_BYTES = UUID_BYTES + SIZE_BYTES;

    private static final long NULL_POINTER = -1;

    private static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    final FileChannel file;
    final Object2LongMap<UUID> pointers;

    final ByteBuffer uuidBytes = ByteBuffer.allocate(16).order(BYTE_ORDER);
    final ByteBuffer sizeBytes = ByteBuffer.allocate(4).order(BYTE_ORDER);
    final LongBuffer uuidBuffer = this.uuidBytes.asLongBuffer();
    final IntBuffer sizeBuffer = this.sizeBytes.asIntBuffer();

    final ByteBuffer[] headerBytes = new ByteBuffer[] { this.uuidBytes, this.sizeBytes };

    private PlayerIndexedDatabase(FileChannel file, Object2LongMap<UUID> pointers) {
        this.file = file;
        this.pointers = pointers;
        this.pointers.defaultReturnValue(NULL_POINTER);
    }

    public static PlayerIndexedDatabase open(Path path) throws IOException {
        var channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        var pointers = buildPointerIndex(channel);
        return new PlayerIndexedDatabase(channel, pointers);
    }

    private static Object2LongMap<UUID> buildPointerIndex(FileChannel channel) throws IOException {
        Object2LongMap<UUID> pointers = new Object2LongOpenHashMap<>();

        var uuidBytes = ByteBuffer.allocate(16).order(BYTE_ORDER);
        var sizeBytes = ByteBuffer.allocate(4).order(BYTE_ORDER);
        var uuidBuffer = uuidBytes.asLongBuffer();
        var sizeBuffer = sizeBytes.asIntBuffer();

        int pointer = 0;

        long fileSize = channel.size();
        while (pointer < fileSize) {
            channel.position(pointer);

            clear(uuidBytes);
            clear(sizeBytes);
            channel.read(uuidBytes);
            channel.read(sizeBytes);

            var uuid = new UUID(uuidBuffer.get(0), uuidBuffer.get(1));
            int size = validateSize(sizeBuffer.get(0));

            pointers.put(uuid, pointer);

            pointer += HEADER_BYTES + size;
        }

        return pointers;
    }

    @Nullable
    public synchronized ByteBuffer get(UUID key) throws IOException {
        long pointer = this.pointers.getLong(key);
        if (pointer == NULL_POINTER) {
            return null;
        }

        this.file.position(pointer + UUID_BYTES);

        clear(this.sizeBytes);
        this.readToEnd(this.sizeBytes);

        int size = this.sizeBuffer.get(0);
        var buffer = ByteBuffer.allocate(size).order(BYTE_ORDER);
        this.readToEnd(buffer);

        return buffer;
    }

    public synchronized void put(UUID key, ByteBuffer bytes) throws IOException {
        validateSize(bytes.capacity());

        long pointer = this.pointers.getLong(key);
        if (pointer == NULL_POINTER) {
            this.push(key, bytes);
        } else {
            this.update(pointer, bytes);
        }
    }

    public synchronized boolean remove(UUID key) throws IOException {
        long pointer = this.pointers.removeLong(key);
        if (pointer == NULL_POINTER) {
            return false;
        }

        this.file.position(pointer + UUID_BYTES);

        clear(this.sizeBytes);
        this.readToEnd(this.sizeBytes);

        int size = validateSize(this.sizeBuffer.get(0));

        long endPointer = pointer + HEADER_BYTES + size;
        this.shiftAfter(endPointer, -(size + HEADER_BYTES));

        return true;
    }

    private void push(UUID key, ByteBuffer bytes) throws IOException {
        long pointer = this.file.size();
        this.file.position(pointer);

        this.writeToEnd(this.writeHeader(key, bytes.capacity()));
        this.writeToEnd(bytes);

        this.pointers.put(key, pointer);
    }

    private ByteBuffer[] writeHeader(UUID key, int size) {
        clear(this.uuidBytes);
        clear(this.uuidBuffer);
        this.uuidBuffer.put(key.getMostSignificantBits()).put(key.getLeastSignificantBits());

        clear(this.sizeBytes);
        clear(this.sizeBuffer);
        this.sizeBuffer.put(size);

        return this.headerBytes;
    }

    private void update(long pointer, ByteBuffer bytes) throws IOException {
        this.file.position(pointer + UUID_BYTES);

        clear(this.sizeBytes);
        this.readToEnd(this.sizeBytes);

        int lastSize = validateSize(this.sizeBuffer.get(0));
        int newSize = validateSize(bytes.capacity());
        if (lastSize != newSize) {
            long endPointer = pointer + HEADER_BYTES + lastSize;
            this.shiftAfter(endPointer, newSize - lastSize);
        }

        clear(this.sizeBytes);
        clear(this.sizeBuffer);
        this.sizeBuffer.put(newSize);

        this.file.position(pointer + UUID_BYTES);
        this.writeToEnd(this.sizeBytes);
        this.writeToEnd(bytes);
    }

    private void shiftAfter(long source, int amount) throws IOException {
        long destination = source + amount;
        long length = this.file.size() - source;

        if (amount > 0) {
            // make space for the shifted data
            this.file.position(this.file.size());
            this.writeToEnd(ByteBuffer.allocate(amount).order(BYTE_ORDER));
        }

        if (length > 0) {
            moveBytes(this.file, source, destination, length);
        }

        if (amount < 0) {
            // shrink the file if it got smaller
            this.file.truncate(this.file.size() + amount);
        }

        // shift all pointers
        for (var entry : Object2LongMaps.fastIterable(this.pointers)) {
            long pointer = entry.getLongValue();
            if (pointer >= source) {
                entry.setValue(pointer + amount);
            }
        }
    }

    private void writeToEnd(ByteBuffer... buffers) throws IOException {
        for (var buffer : buffers) {
            this.writeToEnd(buffer);
        }
    }

    private void writeToEnd(ByteBuffer buffer) throws IOException {
        long remaining = buffer.remaining();
        while (remaining > 0) {
            remaining -= this.file.write(buffer);
        }
    }

    private void readToEnd(ByteBuffer buffer) throws IOException {
        long remaining = buffer.remaining();
        while (remaining > 0) {
            remaining -= this.file.read(buffer);
        }
    }

    private static void moveBytes(FileChannel file, long source, long destination, long length) throws IOException {
        if (source < destination) {
            moveBytesForwards(file, source, destination, length);
        } else {
            moveBytesBackwards(file, source, destination, length);
        }
    }

    private static void moveBytesForwards(FileChannel file, long source, long destination, long length) throws IOException {
        int bufferSize = Math.min(1024, (int) length);
        var buffer = ByteBuffer.allocate(bufferSize).order(BYTE_ORDER);

        long backPointer = source + length;
        long offset = destination - source;
        long remaining = length;

        while (remaining > 0) {
            int copySize = bufferSize;
            if (remaining < bufferSize) {
                copySize = (int) remaining;
                buffer = ByteBuffer.allocate(copySize).order(BYTE_ORDER);
            }

            long frontPointer = backPointer - copySize;
            int read = copyBytes(file, buffer, frontPointer, frontPointer + offset);

            remaining -= read;
            backPointer -= read;
        }
    }

    private static void moveBytesBackwards(FileChannel file, long source, long destination, long length) throws IOException {
        int bufferSize = Math.min(1024, (int) length);
        var buffer = ByteBuffer.allocate(bufferSize).order(BYTE_ORDER);

        long frontPointer = source;
        long offset = destination - source;
        long remaining = length;

        while (remaining > 0) {
            int read = copyBytes(file, buffer, frontPointer, frontPointer + offset);
            remaining -= read;
            frontPointer += read;
        }
    }

    private static int copyBytes(FileChannel file, ByteBuffer buffer, long source, long destination) throws IOException {
        file.position(source);

        clear(buffer);
        int read = file.read(buffer);
        buffer.flip();

        file.position(destination);
        file.write(buffer);

        return read;
    }

    private static int validateSize(int size) throws IOException {
        if (size > MAX_VALUE_SIZE) {
            throw new IOException("size greater than maximum (" + size + ">" + MAX_VALUE_SIZE + ")");
        } else if (size < 0) {
            throw new IOException("size is negative (" + size + "<0)");
        }
        return size;
    }

    @Override
    public synchronized void close() throws IOException {
        this.file.close();
    }

    // in newer Java versions, ByteBuffer overrides the clear method in order to return the right type.
    // here we upcast to force compatibility when compiling with newer Java versions and running on older.
    private static void clear(Buffer buffer) {
        buffer.clear();
    }
}
