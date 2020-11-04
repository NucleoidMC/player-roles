package dev.gegy.roles.override.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import dev.gegy.roles.RolesInitializer;
import net.minecraft.util.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
    public void hookAll(CommandNode<S> root) {
        List<Pair<CommandNode<S>, Predicate<S>>> queue = new ArrayList<>();
        this.collectRecursive(new CommandNode[] { root }, queue);

        for (Pair<CommandNode<S>, Predicate<S>> pair : queue) {
            CommandNode<S> node = pair.getLeft();
            Predicate<S> requirement = pair.getRight();
            this.setRequirement(node, requirement);
        }
    }

    private void collectRecursive(CommandNode<S>[] nodes, List<Pair<CommandNode<S>, Predicate<S>>> queue) {
        CommandNode<S> tail = nodes[nodes.length - 1];
        this.collectCommand(nodes, queue);

        for (CommandNode<S> child : tail.getChildren()) {
            CommandNode<S>[] childNodes = Arrays.copyOf(nodes, nodes.length + 1);
            childNodes[childNodes.length - 1] = child;
            this.collectRecursive(childNodes, queue);
        }
    }

    private void collectCommand(CommandNode<S>[] nodes, List<Pair<CommandNode<S>, Predicate<S>>> queue) {
        CommandNode<S> tail = nodes[nodes.length - 1];
        Predicate<S> requirement = this.effectiveRequirement(nodes);

        queue.add(new Pair<>(tail, this.override.apply(nodes, requirement)));
    }

    @SuppressWarnings("unchecked")
    private Predicate<S> effectiveRequirement(CommandNode<S>[] nodes) {
        Predicate<S>[] requirementTree = new Predicate[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            CommandNode<S> node = nodes[i];
            requirementTree[i] = node.getRequirement();
        }

        return value -> {
            for (Predicate<S> requirement : requirementTree) {
                if (!requirement.test(value)) {
                    return false;
                }
            }
            return true;
        };
    }

    private void setRequirement(CommandNode<S> node, Predicate<S> requirement) {
        try {
            this.requirementField.set(node, requirement);
        } catch (IllegalAccessException e) {
            RolesInitializer.LOGGER.error("Failed to hook command node {}", node, e);
        }
    }

    public interface RequirementOverride<S> {
        Predicate<S> apply(CommandNode<S>[] nodes, Predicate<S> existing);
    }
}
