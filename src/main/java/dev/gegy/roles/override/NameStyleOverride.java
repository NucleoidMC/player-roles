package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.ArrayList;
import java.util.List;

public record NameStyleOverride(Formatting[] formats, @Nullable TextColor color) {
    public static final Codec<NameStyleOverride> CODEC = MoreCodecs.listOrUnit(Codec.STRING).xmap(
            formatKeys -> {
                List<Formatting> formats = new ArrayList<>();
                TextColor color = null;

                for (String formatKey : formatKeys) {
                    var format = Formatting.byName(formatKey);
                    if (format != null) {
                        formats.add(format);
                    } else {
                        var parsedColor = TextColor.parse(formatKey);
                        if (parsedColor != null) {
                            color = parsedColor;
                        }
                    }
                }

                return new NameStyleOverride(formats.toArray(new Formatting[0]), color);
            },
            override -> {
                List<String> formatKeys = new ArrayList<>();
                if (override.color != null) {
                    formatKeys.add(override.color.getName());
                }

                for (var format : override.formats) {
                    formatKeys.add(format.getName());
                }

                return formatKeys;
            }
    );

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
