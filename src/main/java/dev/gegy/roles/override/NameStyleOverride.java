package dev.gegy.roles.override;

import com.mojang.serialization.Dynamic;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class NameStyleOverride implements RoleOverride {
    private final Formatting[] formats;

    public NameStyleOverride(Formatting... formats) {
        this.formats = formats;
    }

    public static <T> NameStyleOverride parse(Dynamic<T> root) {
        List<String> formatKeys = root.asString().result().map(Collections::singletonList)
                .orElseGet(() -> root.asList(element -> element.asString("reset")));

        Formatting[] formats = formatKeys.stream()
                .map(Formatting::byName)
                .filter(Objects::nonNull)
                .toArray(Formatting[]::new);

        return new NameStyleOverride(formats);
    }

    public MutableText apply(MutableText text) {
        return text.formatted(this.formats);
    }
}
