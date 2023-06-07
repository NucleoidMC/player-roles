package dev.gegy.roles;

import dev.gegy.roles.api.override.RoleOverrideResult;
import dev.gegy.roles.override.command.CommandOverrideRules;
import dev.gegy.roles.override.command.MatchableCommand;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CommandRulesTests {
    @Test
    void testAllowExecuteAsDenyExecute() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("execute as"), RoleOverrideResult.ALLOW)
                .add(matcher("execute"), RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test(command("execute as")), RoleOverrideResult.ALLOW);
        assertEquals(rules.test(command("execute at")), RoleOverrideResult.DENY);
        assertEquals(rules.test(command("execute")), RoleOverrideResult.ALLOW);
    }

    @Test
    void testAllowExecuteDenyExecuteAs() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("execute as"), RoleOverrideResult.DENY)
                .add(matcher("execute"), RoleOverrideResult.ALLOW)
                .build();

        assertEquals(rules.test(command("execute as")), RoleOverrideResult.DENY);
        assertEquals(rules.test(command("execute at")), RoleOverrideResult.ALLOW);
        assertEquals(rules.test(command("execute")), RoleOverrideResult.ALLOW);
    }

    @Test
    void testOverrideAllowWildcard() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("gamemode"), RoleOverrideResult.DENY)
                .add(matcher(".*"), RoleOverrideResult.ALLOW)
                .build();

        assertEquals(rules.test(command("gamemode")), RoleOverrideResult.DENY);
        assertEquals(rules.test(command("gamemode creative")), RoleOverrideResult.DENY);
        assertEquals(rules.test(command("foo")), RoleOverrideResult.ALLOW);
        assertEquals(rules.test(command("bar")), RoleOverrideResult.ALLOW);
    }

    @Test
    void testOverrideAllowSpecificGameMode() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("gamemode (spectator|survival)"), RoleOverrideResult.ALLOW)
                .add(matcher("gamemode"), RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test(command("gamemode creative")), RoleOverrideResult.DENY);
        assertEquals(rules.test(command("gamemode survival")), RoleOverrideResult.ALLOW);
        assertEquals(rules.test(command("gamemode spectator")), RoleOverrideResult.ALLOW);
    }

    @Test
    void testOverrideDenyWildcard() {
        CommandOverrideRules rules = CommandOverrideRules.builder()
                .add(matcher("gamemode"), RoleOverrideResult.ALLOW)
                .add(matcher(".*"), RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test(command("gamemode")), RoleOverrideResult.ALLOW);
        assertEquals(rules.test(command("gamemode creative")), RoleOverrideResult.ALLOW);
        assertEquals(rules.test(command("foo")), RoleOverrideResult.DENY);
        assertEquals(rules.test(command("bar")), RoleOverrideResult.DENY);
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
