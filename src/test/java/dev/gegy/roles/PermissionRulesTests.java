package dev.gegy.roles;

import dev.gegy.roles.api.override.RoleOverrideResult;
import dev.gegy.roles.override.legacypermission.LegacyPermissionKeyRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PermissionRulesTests {
    @Test
    void testMatchExact() {
        LegacyPermissionKeyRules rules = LegacyPermissionKeyRules.builder()
                .add("a.b.c", RoleOverrideResult.ALLOW)
                .add("a.b", RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.b.c"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.b"), RoleOverrideResult.DENY);
        assertEquals(rules.test("a.b.c.d"), RoleOverrideResult.PASS);
    }

    @Test
    void testMatchSuffixWildcards() {
        LegacyPermissionKeyRules rules = LegacyPermissionKeyRules.builder()
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
        LegacyPermissionKeyRules rules = LegacyPermissionKeyRules.builder()
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
        LegacyPermissionKeyRules rules = LegacyPermissionKeyRules.builder()
                .add("a.*.c", RoleOverrideResult.ALLOW)
                .add("a.b.c", RoleOverrideResult.DENY)
                .build();

        assertEquals(rules.test("a.a.c"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.a.a.a.a.c"), RoleOverrideResult.ALLOW);
        assertEquals(rules.test("a.b.c"), RoleOverrideResult.DENY);
        assertEquals(rules.test("b.a.c"), RoleOverrideResult.PASS);
    }
}
