package dev.gegy.roles.config;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import dev.gegy.roles.Role;
import dev.gegy.roles.override.RoleOverrideMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RoleConfigMap implements Iterable<Pair<String, RoleConfig>> {
    private final Map<String, RoleConfig> roles;
    private final List<String> roleOrder;

    RoleConfigMap(Map<String, RoleConfig> roles, List<String> roleOrder) {
        this.roles = roles;
        this.roleOrder = roleOrder;
    }

    public static <T> RoleConfigMap parse(Dynamic<T> root, ConfigErrorConsumer error) {
        var roleEntries = root.asMapOpt().result().orElse(Stream.empty())
                .collect(Collectors.toList());

        var roleBuilder = new Builder();

        for (var entry : roleEntries) {
            var name = entry.getFirst().asString(Role.EVERYONE).toLowerCase(Locale.ROOT);
            var roleRoot = entry.getSecond();

            var roleConfigResult = RoleConfig.CODEC.parse(roleRoot);
            if (roleConfigResult.error().isPresent()) {
                error.report("Failed to parse role config for '" + name + "'", roleConfigResult.error().get());
                continue;
            }

            var role = roleConfigResult.result().get();
            roleBuilder.add(name, role);
        }

        return roleBuilder.build(error);
    }

    @Nullable
    public RoleConfig get(String name) {
        return this.roles.get(name);
    }

    @NotNull
    @Override
    public Iterator<Pair<String, RoleConfig>> iterator() {
        return Iterators.transform(this.roleOrder.iterator(), name -> Pair.of(name, this.roles.get(name)));
    }

    static final class Builder {
        private final Map<String, RoleConfig> roles = new Object2ObjectOpenHashMap<>();
        private final List<String> roleOrder = new ArrayList<>();

        Builder() {
        }

        public Builder add(String name, RoleConfig role) {
            this.roles.put(name, role);
            this.roleOrder.add(name);
            return this;
        }

        public RoleConfigMap build(ConfigErrorConsumer error) {
            var roles = this.roles;
            var order = this.sortRoles();

            roles = this.resolveIncludes(roles, order, error);

            return new RoleConfigMap(roles, order);
        }

        private Map<String, RoleConfig> resolveIncludes(Map<String, RoleConfig> roles, List<String> order, ConfigErrorConsumer error) {
            Map<String, RoleConfig> result = new Object2ObjectOpenHashMap<>(roles.size());

            for (String name : order) {
                var role = roles.get(name);

                var resolvedOverrides = new RoleOverrideMap();
                resolvedOverrides.addAll(role.overrides);

                // add includes to our resolved overrides with lower priority than our own
                for (var include : role.includes) {
                    var includeRole = result.get(include);
                    if (includeRole != null) {
                        resolvedOverrides.addAll(includeRole.overrides);
                    } else {
                        if (roles.containsKey(include)) {
                            error.report("'" + name + "' tried to include '" + include + "' but it is of a higher level");
                        } else {
                            error.report("'" + name + "' tried to include '" + include + "' but it does not exist");
                        }
                    }
                }

                result.put(name, new RoleConfig(role.level, resolvedOverrides, new String[0], role.apply));
            }

            return result;
        }

        private List<String> sortRoles() {
            List<String> roleOrder = new ArrayList<>(this.roleOrder);

            Collections.reverse(roleOrder);
            roleOrder.sort(Comparator.comparingInt(name -> {
                var role = this.roles.get(name);
                return role.level;
            }));

            return roleOrder;
        }
    }
}
