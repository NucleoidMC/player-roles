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
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "allow", "yes", "true" -> PermissionResult.ALLOW;
            case "deny", "no", "false" -> PermissionResult.DENY;
            case "hidden", "hide" -> PermissionResult.HIDDEN;
            default -> PermissionResult.PASS;
        };
    }

    public TriState asTriState() {
        return switch (this) {
            case ALLOW, HIDDEN -> TriState.TRUE;
            case DENY -> TriState.FALSE;
            default -> TriState.DEFAULT;
        };
    }

    @Override
    public String asString() {
        return switch (this) {
            case ALLOW -> "allow";
            case DENY -> "deny";
            case HIDDEN -> "hidden";
            default -> "pass";
        };
    }
}
