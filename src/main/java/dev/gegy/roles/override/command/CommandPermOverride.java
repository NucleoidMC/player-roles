package dev.gegy.roles.override.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import dev.gegy.roles.api.HasRoles;
import dev.gegy.roles.override.RoleOverride;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class CommandPermOverride implements RoleOverride {
    private final Collection<Command> commands;

    private CommandPermOverride(List<Command> commands) {
        this.commands = commands;
    }

    public PermissionResult test(MatchableCommand command) {
        for (Command permission : this.commands) {
            PermissionResult result = permission.test(command);
            if (result.isDefinitive()) {
                return result;
            }
        }

        return PermissionResult.PASS;
    }

    public static <T> CommandPermOverride parse(Dynamic<T> root) {
        ImmutableList.Builder<Command> commands = ImmutableList.builder();

        Map<Dynamic<T>, Dynamic<T>> map = root.getMapValues().result().orElse(ImmutableMap.of());
        for (Map.Entry<Dynamic<T>, Dynamic<T>> entry : map.entrySet()) {
            String[] patternStrings = entry.getKey().asString("").split(" ");
            String ruleName = entry.getValue().asString("pass");

            Pattern[] patterns = Arrays.stream(patternStrings).map(Pattern::compile).toArray(Pattern[]::new);
            PermissionResult rule = PermissionResult.byName(ruleName);
            commands.add(new Command(patterns, rule));
        }

        return new CommandPermOverride(commands.build());
    }

    @Override
    public void notifyChange(HasRoles entity) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getCommandManager().sendCommandTree(player);
            }
        }
    }

    @Override
    public String toString() {
        return "CommandPermOverride[" + this.commands.toString() + "]";
    }

    private static class Command {
        final Pattern[] patterns;
        final PermissionResult rule;

        Command(Pattern[] patterns, PermissionResult rule) {
            this.patterns = patterns;
            this.rule = rule;
        }

        PermissionResult test(MatchableCommand command) {
            return command.matches(this.patterns) ? this.rule : PermissionResult.PASS;
        }

        @Override
        public String toString() {
            return "\"" + Arrays.toString(this.patterns) + "\"=" + this.rule;
        }
    }
}
