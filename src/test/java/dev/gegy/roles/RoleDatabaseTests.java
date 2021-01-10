package dev.gegy.roles;

import dev.gegy.roles.store.PlayerIndexedDatabase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class RoleDatabaseTests {
    private static final UUID FOO = UUID.fromString("61fd2fee-c62a-393f-b126-a9885f9b4361");
    private static final UUID BAR = UUID.fromString("303505c1-798d-3df3-ab8d-6c701f3fe36a");
    private static final UUID BAZ = UUID.fromString("fc74fe37-e9f9-3198-8e46-1eee97cacfa6");

    private static final Path DATABASE_PATH = Paths.get("test_database");

    @Test
    void testAddOne() throws IOException {
        PlayerIndexedDatabase database = createEmptyDatabase();
        database.put(FOO, encode("foo"));

        assertEquals(decode(database.get(FOO)), "foo");
        assertNull(database.get(BAR));
    }

    @Test
    void testAddAndUpdateOne() throws IOException {
        PlayerIndexedDatabase database = createEmptyDatabase();
        database.put(FOO, encode("foo"));
        assertEquals(decode(database.get(FOO)), "foo");

        database.put(FOO, encode("not foo"));
        assertEquals(decode(database.get(FOO)), "not foo");
    }

    @Test
    void testAddAndRemoveOne() throws IOException {
        PlayerIndexedDatabase database = createEmptyDatabase();
        database.put(FOO, encode("foo"));
        assertEquals(decode(database.get(FOO)), "foo");

        assertTrue(database.remove(FOO));
        assertNull(database.get(FOO));
    }

    @Test
    void testAddAndShrinkInMiddle() throws IOException {
        PlayerIndexedDatabase database = createEmptyDatabase();
        database.put(FOO, encode("foo"));
        database.put(BAR, encode("bar"));
        database.put(BAZ, encode("baz"));

        assertEquals(decode(database.get(FOO)), "foo");
        assertEquals(decode(database.get(BAR)), "bar");
        assertEquals(decode(database.get(BAZ)), "baz");

        database.put(BAR, encode("b"));

        assertEquals(decode(database.get(FOO)), "foo");
        assertEquals(decode(database.get(BAR)), "b");
        assertEquals(decode(database.get(BAZ)), "baz");
    }

    @Test
    void testAddAndGrowInMiddle() throws IOException {
        PlayerIndexedDatabase database = createEmptyDatabase();
        database.put(FOO, encode("foo"));
        database.put(BAR, encode("bar"));
        database.put(BAZ, encode("baz"));

        assertEquals(decode(database.get(FOO)), "foo");
        assertEquals(decode(database.get(BAR)), "bar");
        assertEquals(decode(database.get(BAZ)), "baz");

        database.put(BAR, encode("baaar"));

        assertEquals(decode(database.get(FOO)), "foo");
        assertEquals(decode(database.get(BAR)), "baaar");
        assertEquals(decode(database.get(BAZ)), "baz");
    }

    @Test
    void testAddAndRemoveInMiddle() throws IOException {
        PlayerIndexedDatabase database = createEmptyDatabase();
        database.put(FOO, encode("foo"));
        database.put(BAR, encode("bar"));
        database.put(BAZ, encode("baz"));

        assertEquals(decode(database.get(FOO)), "foo");
        assertEquals(decode(database.get(BAR)), "bar");
        assertEquals(decode(database.get(BAZ)), "baz");

        assertTrue(database.remove(BAR));

        assertEquals(decode(database.get(FOO)), "foo");
        assertNull(database.get(BAR));
        assertEquals(decode(database.get(BAZ)), "baz");
    }

    @Test
    void testPersistent() throws IOException {
        PlayerIndexedDatabase database = createEmptyDatabase();
        database.put(FOO, encode("foo"));
        database.put(BAZ, encode("baz"));

        assertEquals(decode(database.get(FOO)), "foo");
        assertEquals(decode(database.get(BAZ)), "baz");

        database = reopenDatabase();
        assertEquals(decode(database.get(FOO)), "foo");
        assertEquals(decode(database.get(BAZ)), "baz");
    }

    private static PlayerIndexedDatabase createEmptyDatabase() throws IOException {
        Files.deleteIfExists(DATABASE_PATH);
        return PlayerIndexedDatabase.open(DATABASE_PATH);
    }

    private static PlayerIndexedDatabase reopenDatabase() throws IOException {
        return PlayerIndexedDatabase.open(DATABASE_PATH);
    }

    private static ByteBuffer encode(String text) {
        return ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(ByteBuffer bytes) {
        if (bytes != null) {
            return new String(bytes.array(), StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
}
