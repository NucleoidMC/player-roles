package dev.gegy.roles.api.override;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum RoleOverrideResult implements StringIdentifiable {
    PASS,
    ALLOW,
    DENY,
    HIDDEN;

    public static final Codec<RoleOverrideResult> CODEC = StringIdentifiable.createCodec(RoleOverrideResult::values);

    public boolean isDefinitive() {
        return this != PASS;
    }

    public boolean isAllowed() {
        return this == ALLOW || this == HIDDEN;
    }

    public boolean isDenied() {
        return this == DENY;
    }

    public static RoleOverrideResult byName(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "allow", "yes", "true" -> RoleOverrideResult.ALLOW;
            case "deny", "no", "false" -> RoleOverrideResult.DENY;
            case "hidden", "hide" -> RoleOverrideResult.HIDDEN;
            default -> RoleOverrideResult.PASS;
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
