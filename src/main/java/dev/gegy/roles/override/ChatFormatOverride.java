package dev.gegy.roles.override;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class ChatFormatOverride implements RoleOverride {
    private final String format;

    public ChatFormatOverride(String format) {
        this.format = format;
    }

    public Text make(Text name, String message) {
        return new TranslatableText(this.format, name, message);
    }
}
