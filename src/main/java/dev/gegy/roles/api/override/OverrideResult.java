package dev.gegy.roles.api.override;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum OverrideResult implements StringIdentifiable {
    PASS,
    ALLOW,
    DENY,
    HIDDEN;

    public static final Codec<OverrideResult> CODEC = StringIdentifiable.createCodec(OverrideResult::values, OverrideResult::byName);

    public boolean isDefinitive() {
        return this != PASS;
    }

    public boolean isAllowed() {
        return this == ALLOW || this == HIDDEN;
    }

    public boolean isDenied() {
        return this == DENY;
    }

    public static OverrideResult byName(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "allow", "yes", "true" -> OverrideResult.ALLOW;
            case "deny", "no", "false" -> OverrideResult.DENY;
            case "hidden", "hide" -> OverrideResult.HIDDEN;
            default -> OverrideResult.PASS;
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
