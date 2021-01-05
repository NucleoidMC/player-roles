package dev.gegy.roles.override;

import com.mojang.serialization.Dynamic;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NameStyleOverride {
    private final Formatting[] formats;
    private final TextColor color;

    public NameStyleOverride(Formatting[] formats, @Nullable TextColor color) {
        this.formats = formats;
        this.color = color;
    }

    public static <T> NameStyleOverride parse(Dynamic<T> root) {
        List<String> formatKeys = root.asString().result().map(Collections::singletonList)
                .orElseGet(() -> root.asList(element -> element.asString("reset")));

        List<Formatting> formats = new ArrayList<>();
        TextColor color = null;

        for (String formatKey : formatKeys) {
            Formatting format = Formatting.byName(formatKey);
            if (format != null) {
                formats.add(format);
            } else {
                TextColor parsedColor = TextColor.parse(formatKey);
                if (parsedColor != null) {
                    color = parsedColor;
                }
            }
        }

        return new NameStyleOverride(formats.toArray(new Formatting[0]), color);
    }

    public MutableText apply(MutableText text) {
        return text.setStyle(this.applyStyle(text.getStyle()));
    }

    private Style applyStyle(Style style) {
        style = style.withFormatting(this.formats);
        if (this.color != null) {
            style = style.withColor(this.color);
        }
        return style;
    }
}
