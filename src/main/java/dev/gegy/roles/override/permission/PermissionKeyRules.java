package dev.gegy.roles.override.permission;

import com.mojang.serialization.Codec;
import dev.gegy.roles.api.override.RoleOverrideResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PermissionKeyRules {
    public static final Codec<PermissionKeyRules> CODEC = Codec.unboundedMap(Codec.STRING, RoleOverrideResult.CODEC).xmap(
            map -> {
                PermissionKeyRules.Builder rules = PermissionKeyRules.builder();
                map.forEach(rules::add);
                return rules.build();
            },
            override -> {
                var map = new HashMap<>(override.exactPermissions);
                for (var keyMatcher : override.keyMatchers) {
                    map.put(keyMatcher.asPattern(), keyMatcher.result);
                }
                return map;
            }
    );

    private final Map<String, RoleOverrideResult> exactPermissions;
    private final KeyMatcher[] keyMatchers;

    private PermissionKeyRules(Map<String, RoleOverrideResult> exactPermissions, KeyMatcher[] keyMatchers) {
        this.exactPermissions = exactPermissions;
        this.keyMatchers = keyMatchers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public RoleOverrideResult test(String permission) {
        var result = this.exactPermissions.get(permission);
        if (result != null) {
            return result;
        }

        var tokens = permission.split("\\.");
        for (var matcher : this.keyMatchers) {
            result = matcher.test(tokens);
            if (result != null) {
                return result;
            }
        }

        return RoleOverrideResult.PASS;
    }

    public static class Builder {
        private final Map<String, RoleOverrideResult> exactPermissions = new Object2ObjectOpenHashMap<>();
        private final List<KeyMatcher> keyMatchers = new ArrayList<>();

        Builder() {
        }

        public Builder add(String key, RoleOverrideResult result) {
            if (key.contains("*")) {
                this.keyMatchers.add(new KeyMatcher(key, result));
            } else {
                this.exactPermissions.putIfAbsent(key, result);
            }
            return this;
        }

        public PermissionKeyRules build() {
            return new PermissionKeyRules(this.exactPermissions, this.keyMatchers.toArray(new KeyMatcher[0]));
        }
    }

    static final class KeyMatcher {
        final String[] pattern;
        final RoleOverrideResult result;

        KeyMatcher(String permission, RoleOverrideResult result) {
            this.pattern = permission.split("\\.");
            this.result = result;
        }

        @Nullable
        RoleOverrideResult test(String[] tokens) {
            var pattern = this.pattern;
            int patternIdx = 0;
            String endWildcard = null;

            for (var token : tokens) {
                if (endWildcard == null) {
                    var match = pattern[patternIdx];
                    if (match.equals("*")) {
                        if (++patternIdx < pattern.length) {
                            endWildcard = pattern[patternIdx];
                            continue;
                        } else {
                            return this.result;
                        }
                    }

                    if (token.equals(match)) {
                        if (++patternIdx >= pattern.length) {
                            return this.result;
                        }
                    } else {
                        return null;
                    }
                } else {
                    if (token.equals(endWildcard)) {
                        endWildcard = null;
                        if (++patternIdx >= pattern.length) {
                            return this.result;
                        }
                    }
                }
            }

            if (endWildcard != null) {
                return null;
            }

            return this.result;
        }

        String asPattern() {
            return String.join(".", this.pattern);
        }
    }
}
