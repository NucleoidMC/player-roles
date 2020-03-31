package net.gegy1000.roles.override;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class ChatStyleOverride implements RoleOverride {
    private final String format;

    public ChatStyleOverride(String format) {
        this.format = format;
    }

    public Text make(Text name, String message) {
        return new TranslatableText(this.format, name, message);
    }
}
