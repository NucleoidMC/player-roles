package dev.gegy.roles;

import dev.gegy.roles.api.PermissionResult;
import dev.gegy.roles.override.permission.PermissionKeyRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PermissionRulesTests {
    @Test
    void testMatchExact() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.b.c", PermissionResult.ALLOW)
                .add("a.b", PermissionResult.DENY)
                .build();

        assertEquals(rules.test("a.b.c"), PermissionResult.ALLOW);
        assertEquals(rules.test("a.b"), PermissionResult.DENY);
        assertEquals(rules.test("a.b.c.d"), PermissionResult.PASS);
    }

    @Test
    void testMatchSuffixWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.b.c", PermissionResult.ALLOW)
                .add("a.b.*", PermissionResult.DENY)
                .build();

        assertEquals(rules.test("a.b.c"), PermissionResult.ALLOW);
        assertEquals(rules.test("a.b.c.d"), PermissionResult.DENY);
        assertEquals(rules.test("a.b"), PermissionResult.DENY);
        assertEquals(rules.test("a.b.f"), PermissionResult.DENY);
    }

    @Test
    void testMatchPrefixWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("*.b", PermissionResult.ALLOW)
                .add("a.b", PermissionResult.DENY)
                .build();

        assertEquals(rules.test("f.b"), PermissionResult.ALLOW);
        assertEquals(rules.test("g.b"), PermissionResult.ALLOW);
        assertEquals(rules.test("a.b"), PermissionResult.DENY);
        assertEquals(rules.test("a.c"), PermissionResult.PASS);
    }

    @Test
    void testMatchInlineWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.*.c", PermissionResult.ALLOW)
                .add("a.b.c", PermissionResult.DENY)
                .build();

        assertEquals(rules.test("a.a.c"), PermissionResult.ALLOW);
        assertEquals(rules.test("a.a.a.a.a.c"), PermissionResult.ALLOW);
        assertEquals(rules.test("a.b.c"), PermissionResult.DENY);
        assertEquals(rules.test("b.a.c"), PermissionResult.PASS);
    }
}
