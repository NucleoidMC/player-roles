package dev.gegy.roles.override.command;

import com.mojang.brigadier.tree.CommandNode;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class MatchableCommand {
    private final String[] nodes;

    private MatchableCommand(String[] nodes) {
        this.nodes = nodes;
    }

    public static MatchableCommand of(String[] nodes) {
        return new MatchableCommand(nodes);
    }

    public static MatchableCommand parse(String command) {
        return new MatchableCommand(command.split(" "));
    }

    public static <S> MatchableCommand compile(CommandNode<S>[] nodes) {
        return new MatchableCommand(Arrays.stream(nodes)
                .map(CommandNode::getName)
                .toArray(String[]::new)
        );
    }

    public boolean matchesAllow(Pattern[] patterns) {
        // we match as long as the first nodes match
        int length = Math.min(this.nodes.length, patterns.length);
        for (int i = 0; i < length; i++) {
            if (!patterns[i].matcher(this.nodes[i]).matches()) {
                return false;
            }
        }

        return true;
    }

    public boolean matchesDeny(Pattern[] patterns) {
        // command must be longer than pattern
        if (this.nodes.length < patterns.length) {
            return false;
        }

        for (int i = 0; i < patterns.length; i++) {
            if (!patterns[i].matcher(this.nodes[i]).matches()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.nodes);
    }
}
