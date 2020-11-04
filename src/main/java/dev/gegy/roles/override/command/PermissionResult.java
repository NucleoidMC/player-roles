package dev.gegy.roles.override.command;

import java.util.Locale;

public enum PermissionResult {
    PASS,
    ALLOW,
    DENY,
    HIDDEN;

    public boolean isDefinitive() {
        return this != PASS;
    }

    public static PermissionResult byName(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "allow":
            case "yes":
            case "true":
                return PermissionResult.ALLOW;
            case "deny":
            case "no":
            case "false":
                return PermissionResult.DENY;
            case "hidden":
            case "hide":
                return PermissionResult.HIDDEN;
            case "pass":
            default:
                return PermissionResult.PASS;
        }
    }
}
