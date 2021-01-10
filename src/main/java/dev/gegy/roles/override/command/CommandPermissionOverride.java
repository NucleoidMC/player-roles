package dev.gegy.roles.override.command;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import dev.gegy.roles.api.RoleOwner;
import dev.gegy.roles.override.RoleChangeListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

public final class CommandPermissionOverride implements RoleChangeListener {
    private final CommandPermissionRules rules;

    public CommandPermissionOverride(CommandPermissionRules rules) {
        this.rules = rules;
    }

    public PermissionResult test(MatchableCommand command) {
        return this.rules.test(command);
    }

    public static <T> CommandPermissionOverride parse(Dynamic<T> root) {
        CommandPermissionRules.Builder rules = CommandPermissionRules.builder();

        Map<Dynamic<T>, Dynamic<T>> map = root.getMapValues().result().orElse(ImmutableMap.of());
        for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : map.entrySet()) {
            String[] patternStrings = entry.getKey().asString("").split(" ");
            String ruleName = entry.getValue().asString("pass");

            Pattern[] patterns = Arrays.stream(patternStrings).map(Pattern::compile).toArray(Pattern[]::new);
            PermissionResult result = PermissionResult.byName(ruleName);

            rules.add(patterns, result);
        }

        return new CommandPermissionOverride(rules.build());
    }

    @Override
    public void notifyChange(RoleOwner owner) {
        if (owner instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) owner;
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getCommandManager().sendCommandTree(player);
            }
        }
    }

    @Override
    public String toString() {
        return "CommandPermissionOverride[" + this.rules.toString() + "]";
    }
}
