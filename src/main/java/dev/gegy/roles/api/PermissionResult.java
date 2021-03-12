package dev.gegy.roles.api;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum PermissionResult implements StringIdentifiable {
    PASS,
    ALLOW,
    DENY,
    HIDDEN;

    public static final Codec<PermissionResult> CODEC = StringIdentifiable.createCodec(PermissionResult::values, PermissionResult::byName);

    public boolean isDefinitive() {
        return this != PASS;
    }

    public boolean isAllowed() {
        return this == ALLOW || this == HIDDEN;
    }

    public boolean isDenied() {
        return this == DENY;
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

    public TriState asTriState() {
        switch (this) {
            case ALLOW:
            case HIDDEN:
                return TriState.TRUE;
            case DENY:
                return TriState.FALSE;
            case PASS:
            default:
                return TriState.DEFAULT;
        }
    }

    @Override
    public String asString() {
        switch (this) {
            case ALLOW: return "allow";
            case DENY: return "deny";
            case HIDDEN: return "hidden";
            case PASS:
            default:
                return "pass";
        }
    }
}
