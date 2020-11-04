package dev.gegy.roles;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public final class RoleConfiguration {
    private static final JsonParser JSON = new JsonParser();

    private static RoleConfiguration instance = new RoleConfiguration(ImmutableMap.of(), Role.empty(Role.EVERYONE));

    private final ImmutableMap<String, Role> roles;
    private final Role everyone;

    private RoleConfiguration(ImmutableMap<String, Role> roles, Role everyone) {
        this.roles = roles;
        this.everyone = everyone;
    }

    public static RoleConfiguration get() {
        return instance;
    }

    public static void setup() {
        Path path = Paths.get("config/roles.json");
        if (!Files.exists(path)) {
            if (!createDefaultConfig(path)) {
                return;
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            JsonElement root = JSON.parse(reader);
            instance = parse(new Dynamic<>(JsonOps.INSTANCE, root));
        } catch (IOException e) {
            RolesInitializer.LOGGER.warn("Failed to load roles.json configuration", e);
        }
    }

    private static boolean createDefaultConfig(Path path) {
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Path legacyPath = Paths.get("roles.json");
            if (Files.exists(legacyPath)) {
                Files.move(legacyPath, path);
                return true;
            }

            try (InputStream input = RolesInitializer.class.getResourceAsStream("/data/player-roles/default_roles.json")) {
                Files.copy(input, path);
                return true;
            }
        } catch (IOException e) {
            RolesInitializer.LOGGER.warn("Failed to load default roles.json configuration", e);
            return false;
        }
    }

    private static <T> RoleConfiguration parse(Dynamic<T> root) {
        ImmutableMap.Builder<String, Role> roles = ImmutableMap.builder();
        Role everyone = Role.empty(Role.EVERYONE);

        Map<Dynamic<T>, Dynamic<T>> map = root.getMapValues().result().orElse(ImmutableMap.of());
        for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : map.entrySet()) {
            String name = entry.getKey().asString("everyone").toLowerCase(Locale.ROOT);
            Dynamic<T> roleRoot = entry.getValue();

            Role role = Role.parse(name, roleRoot);
            if (!name.equalsIgnoreCase(Role.EVERYONE)) {
                roles.put(name, role);
            } else {
                everyone = role;
            }
        }

        if (everyone.getLevel() != 0) throw new JsonSyntaxException("'everyone' role level must = 0");

        return new RoleConfiguration(roles.build(), everyone);
    }

    @Nullable
    public Role get(String name) {
        return this.roles.get(name);
    }

    @Nonnull
    public Role everyone() {
        return this.everyone;
    }

    public Stream<Role> stream() {
        return this.roles.values().stream();
    }
}
