package dev.gegy.roles;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RoleConfiguration {
    private static final JsonParser JSON = new JsonParser();

    private static RoleConfiguration instance = new RoleConfiguration(Collections.emptyList(), Role.empty(Role.EVERYONE));

    private final ImmutableMap<String, Role> roles;
    private final Role everyone;

    private RoleConfiguration(List<Role> roles, Role everyone) {
        ImmutableMap.Builder<String, Role> roleMap = ImmutableMap.builder();
        for (Role role : roles) {
            roleMap.put(role.getName(), role);
        }
        this.roles = roleMap.build();

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
            PlayerRolesInitializer.LOGGER.warn("Failed to load roles.json configuration", e);
        } catch (JsonSyntaxException e) {
            PlayerRolesInitializer.LOGGER.warn("Malformed syntax in roles.json configuration", e);
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

            try (InputStream input = PlayerRolesInitializer.class.getResourceAsStream("/data/player-roles/default_roles.json")) {
                Files.copy(input, path);
                return true;
            }
        } catch (IOException e) {
            PlayerRolesInitializer.LOGGER.warn("Failed to load default roles.json configuration", e);
            return false;
        }
    }

    private static <T> RoleConfiguration parse(Dynamic<T> root) {
        Role everyone = Role.empty(Role.EVERYONE);

        List<Pair<Dynamic<T>, Dynamic<T>>> roleEntries = root.asMapOpt().result().orElse(Stream.empty())
                .collect(Collectors.toList());

        List<Role> roles = new ArrayList<>(roleEntries.size());

        for (Pair<Dynamic<T>, Dynamic<T>> entry : roleEntries) {
            String name = entry.getFirst().asString("everyone").toLowerCase(Locale.ROOT);
            Dynamic<T> roleRoot = entry.getSecond();

            Role role = Role.parse(name, roleRoot);

            if (!name.equalsIgnoreCase(Role.EVERYONE)) {
                roles.add(role);
            } else {
                if (everyone.getLevel() != 0) {
                    throw new JsonSyntaxException("'everyone' role level must = 0");
                }
                everyone = role;
            }
        }

        // ensure all roles are assigned unique levels with higher levels assigned to earlier declared entries
        Collections.reverse(roles);
        roles.sort(Comparator.comparingInt(Role::getLevel));

        for (int idx = 0; idx < roles.size(); idx++) {
            Role role = roles.get(idx);
            role.setLevel(idx + 1);
        }

        return new RoleConfiguration(roles, everyone);
    }

    @Nullable
    public Role get(String name) {
        return this.roles.get(name);
    }

    @NotNull
    public Role everyone() {
        return this.everyone;
    }

    public Stream<Role> stream() {
        return this.roles.values().stream();
    }
}
