package dev.gegy.roles.override;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;

public record NameDecorationOverride(
		Optional<AddPrefix> prefix,
		Optional<AddSuffix> suffix,
		Optional<ApplyStyle> applyStyle,
		Optional<OnHover> onHover,
		EnumSet<Context> contexts
) {
	private static final EnumSet<Context> DEFAULT_CONTEXTS = EnumSet.allOf(Context.class);

	public static final Codec<NameDecorationOverride> CODEC = RecordCodecBuilder.create(i -> i.group(
			AddPrefix.CODEC.optionalFieldOf("prefix").forGetter(NameDecorationOverride::prefix),
			AddSuffix.CODEC.optionalFieldOf("suffix").forGetter(NameDecorationOverride::suffix),
			ApplyStyle.CODEC.optionalFieldOf("style").forGetter(NameDecorationOverride::applyStyle),
			OnHover.CODEC.optionalFieldOf("hover").forGetter(NameDecorationOverride::onHover),
			Context.SET_CODEC.optionalFieldOf("contexts", DEFAULT_CONTEXTS).forGetter(NameDecorationOverride::contexts)
	).apply(i, NameDecorationOverride::new));

	public MutableComponent apply(MutableComponent name, Context context) {
		if (!this.contexts.contains(context)) {
			return name;
		}
		if (this.applyStyle.isPresent()) {
			name = this.applyStyle.get().apply(name);
		}
		if (this.onHover.isPresent()) {
			name = this.onHover.get().apply(name);
		}
		if (this.prefix.isPresent()) {
			name = this.prefix.get().apply(name);
		}
		if (this.suffix.isPresent()) {
			name = this.suffix.get().apply(name);
		}
		return name;
	}

	public record AddPrefix(Component prefix) {
		public static final Codec<AddPrefix> CODEC = ComponentSerialization.CODEC.xmap(AddPrefix::new, AddPrefix::prefix);

		public MutableComponent apply(final MutableComponent name) {
			return Component.empty().append(this.prefix).append(name);
		}
	}

	public record AddSuffix(Component suffix) {
		public static final Codec<AddSuffix> CODEC = ComponentSerialization.CODEC.xmap(AddSuffix::new, AddSuffix::suffix);

		public MutableComponent apply(final MutableComponent name) {
			return name.append(this.suffix);
		}
	}

	public record ApplyStyle(ChatFormatting[] formats, @Nullable TextColor color) {
		public static final Codec<ApplyStyle> CODEC = MoreCodecs.listOrUnit(Codec.STRING).xmap(
				formatKeys -> {
					List<ChatFormatting> formats = new ArrayList<>();
					TextColor color = null;

					for (String formatKey : formatKeys) {
						var format = ChatFormatting.getByName(formatKey);
						if (format != null) {
							formats.add(format);
						} else {
							var parsedColor = TextColor.parseColor(formatKey).result();
							if (parsedColor.isPresent()) {
								color = parsedColor.get();
							}
						}
					}

					return new ApplyStyle(formats.toArray(new ChatFormatting[0]), color);
				},
				override -> {
					List<String> formatKeys = new ArrayList<>();
					if (override.color != null) {
						formatKeys.add(override.color.serialize());
					}

					for (var format : override.formats) {
						formatKeys.add(format.getName());
					}

					return formatKeys;
				}
		);

		public MutableComponent apply(MutableComponent text) {
			return text.setStyle(this.applyStyle(text.getStyle()));
		}

		private Style applyStyle(Style style) {
			style = style.applyFormats(this.formats);
			if (this.color != null) {
				style = style.withColor(this.color);
			}
			return style;
		}
	}

	public record OnHover(HoverEvent event) {
		private static final Codec<OnHover> CODEC = HoverEvent.CODEC.xmap(OnHover::new, OnHover::event);
		public MutableComponent apply(MutableComponent text) {
			return text.setStyle(text.getStyle().withHoverEvent(this.event));
		}
	}

	public enum Context implements StringRepresentable {
		CHAT("chat"),
		TAB_LIST("tab_list"),
		;

		public static final com.mojang.serialization.Codec<Context> CODEC = StringRepresentable.fromEnum(Context::values);

		public static final com.mojang.serialization.Codec<EnumSet<Context>> SET_CODEC = Context.CODEC.listOf().comapFlatMap(list -> {
			var set = EnumSet.noneOf(Context.class);
			for (var context : list) {
				if (!set.add(context)) {
					return DataResult.error(() -> "Duplicate entry in set: " + context.name());
				}
			}
			return DataResult.success(set);
		}, ArrayList::new);

		private final String name;

		Context(final String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
