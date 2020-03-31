package net.gegy1000.roles.override.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.gegy1000.roles.RolesInitializer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

// TODO: This can probably be optimized to only hook the required commands and cleverly handle when commands are
//  removed from the config.. but for now this is good enough!
public final class CommandRequirementHooks<S> {
    private final RequirementOverride<S> override;
    private final Field requirementField;

    private CommandRequirementHooks(RequirementOverride<S> override, Field requirementField) {
        this.override = override;
        this.requirementField = requirementField;
    }

    public static <S> CommandRequirementHooks<S> tryCreate(RequirementOverride<S> override) throws ReflectiveOperationException {
        Field requirementField = CommandNode.class.getDeclaredField("requirement");
        requirementField.setAccessible(true);
        return new CommandRequirementHooks<>(override, requirementField);
    }

    public void hookAll(CommandDispatcher<S> dispatcher) {
        Collection<CommandNode<S>> nodes = dispatcher.getRoot().getChildren();
        nodes.forEach(this::hookAll);
    }

    @SuppressWarnings("unchecked")
    public void hookAll(CommandNode<S> node) {
        this.hookRecursive(new CommandNode[] { node });
    }

    private void hookRecursive(CommandNode<S>[] nodes) {
        CommandNode<S> tail = nodes[nodes.length - 1];
        this.hookCommand(nodes);

        for (CommandNode<S> child : tail.getChildren()) {
            CommandNode<S>[] childNodes = Arrays.copyOf(nodes, nodes.length + 1);
            childNodes[childNodes.length - 1] = child;
            this.hookRecursive(childNodes);
        }
    }

    private void hookCommand(CommandNode<S>[] nodes) {
        CommandNode<S> tail = nodes[nodes.length - 1];
        try {
            Predicate<S> requirement = tail.getRequirement();
            this.requirementField.set(tail, this.override.apply(nodes, requirement));
        } catch (IllegalAccessException e) {
            RolesInitializer.LOGGER.error("Failed to hook command node {}", tail, e);
        }
    }

    public interface RequirementOverride<S> {
        Predicate<S> apply(CommandNode<S>[] nodes, Predicate<S> existing);
    }
}
