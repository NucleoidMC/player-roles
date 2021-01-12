package dev.gegy.roles.override.command;

import dev.gegy.roles.api.PermissionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public final class CommandPermissionRules {
    private final Rule[] rules;

    CommandPermissionRules(Rule[] commands) {
        this.rules = commands;
    }

    public static Builder builder() {
        return new Builder();
    }

    public PermissionResult test(MatchableCommand command) {
        for (Rule rule : this.rules) {
            PermissionResult result = rule.test(command);
            if (result.isDefinitive()) {
                return result;
            }
        }
        return PermissionResult.PASS;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.rules);
    }

    public static class Builder {
        private final List<Rule> rules = new ArrayList<>();

        Builder() {
        }

        public Builder add(Pattern[] patterns, PermissionResult result) {
            this.rules.add(new Rule(patterns, result));
            return this;
        }

        public CommandPermissionRules build() {
            this.rules.sort(Comparator.comparingInt(Rule::size).reversed());
            Rule[] rules = this.rules.toArray(new Rule[0]);
            return new CommandPermissionRules(rules);
        }
    }

    private static class Rule {
        final Pattern[] patterns;
        final PermissionResult result;

        Rule(Pattern[] patterns, PermissionResult result) {
            this.patterns = patterns;
            this.result = result;
        }

        PermissionResult test(MatchableCommand command) {
            if (this.result.isAllowed()) {
                return command.matchesAllow(this.patterns) ? this.result : PermissionResult.PASS;
            } else if (this.result.isDenied()) {
                return command.matchesDeny(this.patterns) ? this.result : PermissionResult.PASS;
            }
            return PermissionResult.PASS;
        }

        int size() {
            return this.patterns.length;
        }

        @Override
        public String toString() {
            return "\"" + Arrays.toString(this.patterns) + "\"=" + this.result;
        }
    }
}
