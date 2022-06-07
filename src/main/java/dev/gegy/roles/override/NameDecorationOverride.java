package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record NameDecorationOverride(
		Optional<AddPrefix> prefix,
		Optional<AddSuffix> suffix,
		Optional<ApplyStyle> applyStyle
) {
	public static final Codec<NameDecorationOverride> CODEC = RecordCodecBuilder.create(i -> i.group(
			AddPrefix.CODEC.optionalFieldOf("prefix").forGetter(NameDecorationOverride::prefix),
			AddSuffix.CODEC.optionalFieldOf("suffix").forGetter(NameDecorationOverride::suffix),
			ApplyStyle.CODEC.optionalFieldOf("style").forGetter(NameDecorationOverride::applyStyle)
	).apply(i, NameDecorationOverride::new));

	public MutableText apply(MutableText name) {
		if (this.applyStyle.isPresent()) {
			name = this.applyStyle.get().apply(name);
		}
		if (this.prefix.isPresent()) {
			name = this.prefix.get().apply(name);
		}
		if (this.suffix.isPresent()) {
			name = this.suffix.get().apply(name);
		}
		return name;
	}

	public record AddPrefix(Text prefix) {
		public static final Codec<AddPrefix> CODEC = MoreCodecs.TEXT.xmap(AddPrefix::new, AddPrefix::prefix);

		public MutableText apply(final MutableText name) {
			return new LiteralText("").append(this.prefix).append(name);
		}
	}

	public record AddSuffix(Text suffix) {
		public static final Codec<AddSuffix> CODEC = MoreCodecs.TEXT.xmap(AddSuffix::new, AddSuffix::suffix);

		public MutableText apply(final MutableText name) {
			return name.append(this.suffix);
		}
	}

	public record ApplyStyle(Formatting[] formats, @Nullable TextColor color) {
		public static final Codec<ApplyStyle> CODEC = MoreCodecs.listOrUnit(Codec.STRING).xmap(
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

					return new ApplyStyle(formats.toArray(new Formatting[0]), color);
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
}
