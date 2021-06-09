package dev.gegy.roles;

import dev.gegy.roles.override.command.CommandOverrideRules;
import dev.gegy.roles.override.command.MatchableCommand;
import dev.gegy.roles.api.override.OverrideResult;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CommandRulesTests {
    @Test
    void testAllowExecuteAsDenyExecute() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("execute as"), OverrideResult.ALLOW)
                .add(matcher("execute"), OverrideResult.DENY)
                .build();

        assertEquals(rules.test(command("execute as")), OverrideResult.ALLOW);
        assertEquals(rules.test(command("execute at")), OverrideResult.DENY);
        assertEquals(rules.test(command("execute")), OverrideResult.ALLOW);
    }

    @Test
    void testAllowExecuteDenyExecuteAs() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("execute as"), OverrideResult.DENY)
                .add(matcher("execute"), OverrideResult.ALLOW)
                .build();

        assertEquals(rules.test(command("execute as")), OverrideResult.DENY);
        assertEquals(rules.test(command("execute at")), OverrideResult.ALLOW);
        assertEquals(rules.test(command("execute")), OverrideResult.ALLOW);
    }

    @Test
    void testOverrideAllowWildcard() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("gamemode"), OverrideResult.DENY)
                .add(matcher(".*"), OverrideResult.ALLOW)
                .build();

        assertEquals(rules.test(command("gamemode")), OverrideResult.DENY);
        assertEquals(rules.test(command("gamemode creative")), OverrideResult.DENY);
        assertEquals(rules.test(command("foo")), OverrideResult.ALLOW);
        assertEquals(rules.test(command("bar")), OverrideResult.ALLOW);
    }

    @Test
    void testOverrideDenyWildcard() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("gamemode"), OverrideResult.ALLOW)
                .add(matcher(".*"), OverrideResult.DENY)
                .build();

        assertEquals(rules.test(command("gamemode")), OverrideResult.ALLOW);
        assertEquals(rules.test(command("gamemode creative")), OverrideResult.ALLOW);
        assertEquals(rules.test(command("foo")), OverrideResult.DENY);
        assertEquals(rules.test(command("bar")), OverrideResult.DENY);
    }

    private static Pattern[] matcher(String matcher) {
        String[] patternStrings = matcher.split(" ");
        Pattern[] patterns = new Pattern[patternStrings.length];
        for (int i = 0; i < patternStrings.length; i++) {
            patterns[i] = Pattern.compile(patternStrings[i]);
        }
        return patterns;
    }

    private static MatchableCommand command(String command) {
        return MatchableCommand.parse(command);
    }
}
