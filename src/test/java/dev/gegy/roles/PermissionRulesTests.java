package dev.gegy.roles;

import dev.gegy.roles.api.override.OverrideResult;
import dev.gegy.roles.override.permission.PermissionKeyRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PermissionRulesTests {
    @Test
    void testMatchExact() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.b.c", OverrideResult.ALLOW)
                .add("a.b", OverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.b.c"), OverrideResult.ALLOW);
        assertEquals(rules.test("a.b"), OverrideResult.DENY);
        assertEquals(rules.test("a.b.c.d"), OverrideResult.PASS);
    }

    @Test
    void testMatchSuffixWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.b.c", OverrideResult.ALLOW)
                .add("a.b.*", OverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.b.c"), OverrideResult.ALLOW);
        assertEquals(rules.test("a.b.c.d"), OverrideResult.DENY);
        assertEquals(rules.test("a.b"), OverrideResult.DENY);
        assertEquals(rules.test("a.b.f"), OverrideResult.DENY);
    }

    @Test
    void testMatchPrefixWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("*.b", OverrideResult.ALLOW)
                .add("a.b", OverrideResult.DENY)
                .build();

        assertEquals(rules.test("f.b"), OverrideResult.ALLOW);
        assertEquals(rules.test("g.b"), OverrideResult.ALLOW);
        assertEquals(rules.test("a.b"), OverrideResult.DENY);
        assertEquals(rules.test("a.c"), OverrideResult.PASS);
    }

    @Test
    void testMatchInlineWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.*.c", OverrideResult.ALLOW)
                .add("a.b.c", OverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.a.c"), OverrideResult.ALLOW);
        assertEquals(rules.test("a.a.a.a.a.c"), OverrideResult.ALLOW);
        assertEquals(rules.test("a.b.c"), OverrideResult.DENY);
        assertEquals(rules.test("b.a.c"), OverrideResult.PASS);
    }
}
