package dev.gegy.roles;

import dev.gegy.roles.override.command.MatchableCommand;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CommandMatchingTests {
    @Test
    void testExecuteMatchesForAllow() {
        assertTrue(command("execute as").matchesAllow(matcher("execute")));
        assertTrue(command("execute at").matchesAllow(matcher("execute")));

        assertTrue(command("execute").matchesAllow(matcher("execute as")));
        assertTrue(command("execute").matchesAllow(matcher("execute at")));
    }

    @Test
    void testUnrelatedNoMatchesForAllow() {
        assertFalse(command("execute").matchesAllow(matcher("time")));
        assertFalse(command("execute as").matchesAllow(matcher("time")));

        assertFalse(command("time").matchesAllow(matcher("execute")));
        assertFalse(command("time").matchesAllow(matcher("execute as")));
    }

    @Test
    void testExecuteMatchesForDeny() {
        assertTrue(command("execute as").matchesDeny(matcher("execute")));
        assertTrue(command("execute at").matchesDeny(matcher("execute")));

        assertFalse(command("execute").matchesDeny(matcher("execute as")));
        assertFalse(command("execute").matchesDeny(matcher("execute at")));
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
