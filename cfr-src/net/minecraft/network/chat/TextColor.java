/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

public final class TextColor {
    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap(TextColor::parseColor, TextColor::serialize);
    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = (Map)Stream.of(ChatFormatting.values()).filter(ChatFormatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), $$0 -> new TextColor($$0.getColor(), $$0.getName())));
    private static final Map<String, TextColor> NAMED_COLORS = (Map)LEGACY_FORMAT_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap($$0 -> $$0.name, Function.identity()));
    private final int value;
    private final @Nullable String name;

    private TextColor(int $$0, String $$1) {
        this.value = $$0 & 0xFFFFFF;
        this.name = $$1;
    }

    private TextColor(int $$0) {
        this.value = $$0 & 0xFFFFFF;
        this.name = null;
    }

    public int getValue() {
        return this.value;
    }

    public String serialize() {
        return this.name != null ? this.name : this.formatValue();
    }

    private String formatValue() {
        return String.format(Locale.ROOT, "#%06X", this.value);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        TextColor $$1 = (TextColor)$$0;
        return this.value == $$1.value;
    }

    public int hashCode() {
        return Objects.hash(this.value, this.name);
    }

    public String toString() {
        return this.serialize();
    }

    public static @Nullable TextColor fromLegacyFormat(ChatFormatting $$0) {
        return LEGACY_FORMAT_TO_COLOR.get($$0);
    }

    public static TextColor fromRgb(int $$0) {
        return new TextColor($$0);
    }

    public static DataResult<TextColor> parseColor(String $$0) {
        if ($$0.startsWith(CUSTOM_COLOR_PREFIX)) {
            try {
                int $$1 = Integer.parseInt($$0.substring(1), 16);
                if ($$1 < 0 || $$1 > 0xFFFFFF) {
                    return DataResult.error(() -> "Color value out of range: " + $$0);
                }
                return DataResult.success((Object)TextColor.fromRgb($$1), (Lifecycle)Lifecycle.stable());
            }
            catch (NumberFormatException $$2) {
                return DataResult.error(() -> "Invalid color value: " + $$0);
            }
        }
        TextColor $$3 = NAMED_COLORS.get($$0);
        if ($$3 == null) {
            return DataResult.error(() -> "Invalid color name: " + $$0);
        }
        return DataResult.success((Object)$$3, (Lifecycle)Lifecycle.stable());
    }
}

