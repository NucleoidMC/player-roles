package dev.gegy.roles;

import dev.gegy.roles.api.override.RoleOverrideResult;
import dev.gegy.roles.override.permission.PermissionKeyRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PermissionRulesTests {
    @Test
    void testMatchExact() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.b.c", RoleOverrideResult.ALLOW)
                .add("a.b", RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.b.c"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.b"), RoleOverrideResult.DENY);
        assertEquals(rules.test("a.b.c.d"), RoleOverrideResult.PASS);
    }

    @Test
    void testMatchSuffixWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.b.c", RoleOverrideResult.ALLOW)
                .add("a.b.*", RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.b.c"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.b.c.d"), RoleOverrideResult.DENY);
        assertEquals(rules.test("a.b"), RoleOverrideResult.DENY);
        assertEquals(rules.test("a.b.f"), RoleOverrideResult.DENY);
    }

    @Test
    void testMatchPrefixWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("*.b", RoleOverrideResult.ALLOW)
                .add("a.b", RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test("f.b"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("g.b"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.b"), RoleOverrideResult.DENY);
        assertEquals(rules.test("a.c"), RoleOverrideResult.PASS);
    }

    @Test
    void testMatchInlineWildcards() {
        PermissionKeyRules rules = PermissionKeyRules.builder()
                .add("a.*.c", RoleOverrideResult.ALLOW)
                .add("a.b.c", RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.a.c"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.a.a.a.a.c"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.b.c"), RoleOverrideResult.DENY);
        assertEquals(rules.test("b.a.c"), RoleOverrideResult.PASS);
    }
}
