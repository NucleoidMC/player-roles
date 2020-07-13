package net.gegy1000.roles.override;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public final class ChatStyleOverride implements RoleOverride {
    private final String format;

    public ChatStyleOverride(String format) {
        this.format = format;
    }

    public Text make(String name, String message) {
        return new LiteralText(String.format(this.format, name, message));
    }
}
