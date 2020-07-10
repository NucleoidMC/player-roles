package net.gegy1000.roles.override.command;

import com.mojang.brigadier.tree.CommandNode;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class MatchableCommand {
    private final String[] nodes;

    private MatchableCommand(String[] nodes) {
        this.nodes = nodes;
    }

    public static <S> MatchableCommand compile(CommandNode<S>[] nodes) {
        return new MatchableCommand(Arrays.stream(nodes)
                .map(CommandNode::getName)
                .toArray(String[]::new)
        );
    }

    public boolean matches(Pattern[] patterns) {
        int length = Math.min(this.nodes.length, patterns.length);
        for (int i = 0; i < length; i++) {
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
