package dev.gegy.roles.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.gegy.roles.PlayerRoles;
import dev.gegy.roles.SimpleRole;
import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.Role;
import dev.gegy.roles.api.RoleProvider;
import dev.gegy.roles.store.ServerRoleSet;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class PlayerRolesConfig implements RoleProvider {
    private static PlayerRolesConfig instance = new PlayerRolesConfig(Collections.emptyList(), SimpleRole.empty(PlayerRoles.EVERYONE));

    private final ImmutableMap<String, SimpleRole> roles;
    private final SimpleRole everyone;

    private ServerRoleSet commandBlockRoles;
    private ServerRoleSet functionRoles;

    private PlayerRolesConfig(List<SimpleRole> roles, SimpleRole everyone) {
        ImmutableMap.Builder<String, SimpleRole> roleMap = ImmutableMap.builder();
        for (SimpleRole role : roles) {
            roleMap.put(role.getId(), role);
        }
        this.roles = roleMap.build();

        this.everyone = everyone;
    }

    private ServerRoleSet buildRoles(Predicate<RoleApplyConfig> apply) {
        var roleSet = new ObjectAVLTreeSet<Role>();
        this.roles.values().stream()
                .filter(role -> apply.test(role.getApply()))
                .forEach(roleSet::add);

        return ServerRoleSet.of(roleSet);
    }

    public static PlayerRolesConfig get() {
        return instance;
    }

    public static List<String> setup() {
        var path = Paths.get("config/roles.json");
        if (!Files.exists(path)) {
            if (!createDefaultConfig(path)) {
                return ImmutableList.of();
            }
        }

        List<String> errors = new ArrayList<>();
        ConfigErrorConsumer errorConsumer = errors::add;

        try (var reader = Files.newBufferedReader(path)) {
            var root = JsonParser.parseReader(reader);
            var config = parse(new Dynamic<>(JsonOps.INSTANCE, root), errorConsumer);
            instance = config;

            PlayerRolesApi.setRoleProvider(config);
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

            var legacyPath = Paths.get("roles.json");
            if (Files.exists(legacyPath)) {
                Files.move(legacyPath, path);
                return true;
            }

            try (var input = PlayerRoles.class.getResourceAsStream("/data/player-roles/default_roles.json")) {
                Files.copy(input, path);
                return true;
            }
        } catch (IOException e) {
            PlayerRoles.LOGGER.warn("Failed to load default roles.json configuration", e);
            return false;
        }
    }

    private static <T> PlayerRolesConfig parse(Dynamic<T> root, ConfigErrorConsumer error) {
        var roleConfigs = RoleConfigMap.parse(root, error);

        var everyone = SimpleRole.empty(PlayerRoles.EVERYONE);
        List<SimpleRole> roles = new ArrayList<>();

        int index = 1;
        for (Pair<String, RoleConfig> entry : roleConfigs) {
            String name = entry.getFirst();
            RoleConfig roleConfig = entry.getSecond();

            if (!name.equalsIgnoreCase(PlayerRoles.EVERYONE)) {
                roles.add(roleConfig.create(name, index++));
            } else {
                everyone = roleConfig.create(name, 0);
            }
        }

        return new PlayerRolesConfig(roles, everyone);
    }

    @Override
    @Nullable
    public SimpleRole get(String name) {
        return this.roles.get(name);
    }

    @NotNull
    public SimpleRole everyone() {
        return this.everyone;
    }

    public ServerRoleSet getCommandBlockRoles() {
        var commandBlockRoles = this.commandBlockRoles;
        if (commandBlockRoles == null) {
            this.commandBlockRoles = commandBlockRoles = this.buildRoles(apply -> apply.commandBlock);
        }
        return commandBlockRoles;
    }

    public ServerRoleSet getFunctionRoles() {
        var functionRoles = this.functionRoles;
        if (functionRoles == null) {
            this.functionRoles = functionRoles = this.buildRoles(apply -> apply.functions);
        }
        return functionRoles;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Role> iterator() {
        return (Iterator<Role>) (Iterator) this.roles.values().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<Role> stream() {
        return (Stream<Role>) (Stream) this.roles.values().stream();
    }
}
