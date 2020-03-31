package net.gegy1000.roles.override.command;

import java.util.Locale;

public enum PermissionResult {
    PASS,
    ALLOW,
    DENY;

    public boolean isDefinitive() {
        return this == ALLOW || this == DENY;
    }

    public static PermissionResult byName(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "allow":
            case "yes":
                return PermissionResult.ALLOW;
            case "deny":
            case "no":
                return PermissionResult.DENY;
            case "pass":
            default:
                return PermissionResult.PASS;
        }
    }
}
