package dev.gegy.roles;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.gegy.roles.store.PlayerRoleSet;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class PlayerRolesConfig {
    private static final JsonParser JSON = new JsonParser();

    private static PlayerRolesConfig instance = new PlayerRolesConfig(Collections.emptyList(), Role.empty(Role.EVERYONE));

    private final ImmutableMap<String, Role> roles;
    private final Role everyone;

    private PlayerRoleSet commandBlockRoles;
    private PlayerRoleSet functionRoles;

    private PlayerRolesConfig(List<Role> roles, Role everyone) {
        ImmutableMap.Builder<String, Role> roleMap = ImmutableMap.builder();
        for (Role role : roles) {
            roleMap.put(role.getName(), role);
        }
        this.roles = roleMap.build();

        this.everyone = everyone;
    }

    private PlayerRoleSet buildRoles(Predicate<RoleApplyConfig> apply) {
        PlayerRoleSet roles = new PlayerRoleSet(null);

        this.roles.values().stream()
                .filter(role -> apply.test(role.getApply()))
                .forEach(roles::add);

        return roles;
    }

    public static PlayerRolesConfig get() {
        return instance;
    }

    public static List<String> setup() {
        Path path = Paths.get("config/roles.json");
        if (!Files.exists(path)) {
            if (!createDefaultConfig(path)) {
                return ImmutableList.of();
            }
        }

        List<String> errors = new ArrayList<>();
        ErrorConsumer errorConsumer = errors::add;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            JsonElement root = JSON.parse(reader);
            instance = parse(new Dynamic<>(JsonOps.INSTANCE, root), errorConsumer);
        } catch (IOException e) {
            errorConsumer.report("Failed to read roles.json configuration", e);
            PlayerRoles.LOGGER.warn("Failed to load roles.json configuration", e);
        } catch (JsonSyntaxException e) {
            errorConsumer.report("Malformed syntax in roles.json configuration", e);
            PlayerRoles.LOGGER.warn("Malformed syntax in roles.json configuration", e);
        }

        return errors;
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

            try (InputStream input = PlayerRoles.class.getResourceAsStream("/data/player-roles/default_roles.json")) {
                Files.copy(input, path);
                return true;
            }
        } catch (IOException e) {
            PlayerRoles.LOGGER.warn("Failed to load default roles.json configuration", e);
            return false;
        }
    }

    private static <T> PlayerRolesConfig parse(Dynamic<T> root, ErrorConsumer error) {
        RoleConfigMap roleConfigs = RoleConfigMap.parse(root, error);

        Role everyone = Role.empty(Role.EVERYONE);
        List<Role> roles = new ArrayList<>();

        int level = 1;
        for (Pair<String, RoleConfig> entry : roleConfigs) {
            String name = entry.getFirst();
            RoleConfig roleConfig = entry.getSecond();

            if (!name.equals(Role.EVERYONE)) {
                roles.add(roleConfig.create(name, level++));
            } else {
                everyone = roleConfig.create(name, 0);
            }
        }

        return new PlayerRolesConfig(roles, everyone);
    }

    @Nullable
    public Role get(String name) {
        return this.roles.get(name);
    }

    @NotNull
    public Role everyone() {
        return this.everyone;
    }

    public PlayerRoleSet getCommandBlockRoles() {
        PlayerRoleSet commandBlockRoles = this.commandBlockRoles;
        if (commandBlockRoles == null) {
            this.commandBlockRoles = commandBlockRoles = this.buildRoles(apply -> apply.commandBlock);
        }
        return commandBlockRoles;
    }

    public PlayerRoleSet getFunctionRoles() {
        PlayerRoleSet functionRoles = this.functionRoles;
        if (functionRoles == null) {
            this.functionRoles = functionRoles = this.buildRoles(apply -> apply.functions);
        }
        return functionRoles;
    }

    public Stream<Role> stream() {
        return this.roles.values().stream();
    }
}
