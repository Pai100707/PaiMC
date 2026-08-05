/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jspecify.annotations.Nullable;

public final class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null, null);
    public static final int NO_SHADOW = 0;
    final @Nullable TextColor color;
    final @Nullable Integer shadowColor;
    final @Nullable Boolean bold;
    final @Nullable Boolean italic;
    final @Nullable Boolean underlined;
    final @Nullable Boolean strikethrough;
    final @Nullable Boolean obfuscated;
    final @Nullable ClickEvent clickEvent;
    final @Nullable HoverEvent hoverEvent;
    final @Nullable String insertion;
    final @Nullable FontDescription font;

    private static Style create(Optional<TextColor> $$0, Optional<Integer> $$1, Optional<Boolean> $$2, Optional<Boolean> $$3, Optional<Boolean> $$4, Optional<Boolean> $$5, Optional<Boolean> $$6, Optional<ClickEvent> $$7, Optional<HoverEvent> $$8, Optional<String> $$9, Optional<FontDescription> $$10) {
        Style $$11 = new Style($$0.orElse(null), $$1.orElse(null), $$2.orElse(null), $$3.orElse(null), $$4.orElse(null), $$5.orElse(null), $$6.orElse(null), $$7.orElse(null), $$8.orElse(null), $$9.orElse(null), $$10.orElse(null));
        if ($$11.equals(EMPTY)) {
            return EMPTY;
        }
        return $$11;
    }

    private Style(@Nullable TextColor $$0, @Nullable Integer $$1, @Nullable Boolean $$2, @Nullable Boolean $$3, @Nullable Boolean $$4, @Nullable Boolean $$5, @Nullable Boolean $$6, @Nullable ClickEvent $$7, @Nullable HoverEvent $$8, @Nullable String $$9, @Nullable FontDescription $$10) {
        this.color = $$0;
        this.shadowColor = $$1;
        this.bold = $$2;
        this.italic = $$3;
        this.underlined = $$4;
        this.strikethrough = $$5;
        this.obfuscated = $$6;
        this.clickEvent = $$7;
        this.hoverEvent = $$8;
        this.insertion = $$9;
        this.font = $$10;
    }

    public @Nullable TextColor getColor() {
        return this.color;
    }

    public @Nullable Integer getShadowColor() {
        return this.shadowColor;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public @Nullable ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    public @Nullable HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    public @Nullable String getInsertion() {
        return this.insertion;
    }

    public FontDescription getFont() {
        return this.font != null ? this.font : FontDescription.DEFAULT;
    }

    private static <T> Style checkEmptyAfterChange(Style $$0, @Nullable T $$1, @Nullable T $$2) {
        if ($$1 != null && $$2 == null && $$0.equals(EMPTY)) {
            return EMPTY;
        }
        return $$0;
    }

    public Style withColor(@Nullable TextColor $$0) {
        if (Objects.equals(this.color, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style($$0, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.color, $$0);
    }

    public Style withColor(@Nullable ChatFormatting $$0) {
        return this.withColor($$0 != null ? TextColor.fromLegacyFormat($$0) : null);
    }

    public Style withColor(int $$0) {
        return this.withColor(TextColor.fromRgb($$0));
    }

    public Style withShadowColor(int $$0) {
        if (Objects.equals(this.shadowColor, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, $$0, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.shadowColor, $$0);
    }

    public Style withoutShadow() {
        return this.withShadowColor(0);
    }

    public Style withBold(@Nullable Boolean $$0) {
        if (Objects.equals(this.bold, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, $$0, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.bold, $$0);
    }

    public Style withItalic(@Nullable Boolean $$0) {
        if (Objects.equals(this.italic, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, $$0, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.italic, $$0);
    }

    public Style withUnderlined(@Nullable Boolean $$0) {
        if (Objects.equals(this.underlined, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, this.italic, $$0, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.underlined, $$0);
    }

    public Style withStrikethrough(@Nullable Boolean $$0) {
        if (Objects.equals(this.strikethrough, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, $$0, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.strikethrough, $$0);
    }

    public Style withObfuscated(@Nullable Boolean $$0) {
        if (Objects.equals(this.obfuscated, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, $$0, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.obfuscated, $$0);
    }

    public Style withClickEvent(@Nullable ClickEvent $$0) {
        if (Objects.equals(this.clickEvent, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, $$0, this.hoverEvent, this.insertion, this.font), this.clickEvent, $$0);
    }

    public Style withHoverEvent(@Nullable HoverEvent $$0) {
        if (Objects.equals(this.hoverEvent, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, $$0, this.insertion, this.font), this.hoverEvent, $$0);
    }

    public Style withInsertion(@Nullable String $$0) {
        if (Objects.equals(this.insertion, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, $$0, this.font), this.insertion, $$0);
    }

    public Style withFont(@Nullable FontDescription $$0) {
        if (Objects.equals(this.font, $$0)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, $$0), this.font, $$0);
    }

    public Style applyFormat(ChatFormatting $$0) {
        TextColor $$1 = this.color;
        Boolean $$2 = this.bold;
        Boolean $$3 = this.italic;
        Boolean $$4 = this.strikethrough;
        Boolean $$5 = this.underlined;
        Boolean $$6 = this.obfuscated;
        switch ($$0) {
            case OBFUSCATED: {
                $$6 = true;
                break;
            }
            case BOLD: {
                $$2 = true;
                break;
            }
            case STRIKETHROUGH: {
                $$4 = true;
                break;
            }
            case UNDERLINE: {
                $$5 = true;
                break;
            }
            case ITALIC: {
                $$3 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                $$1 = TextColor.fromLegacyFormat($$0);
            }
        }
        return new Style($$1, this.shadowColor, $$2, $$3, $$5, $$4, $$6, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyLegacyFormat(ChatFormatting $$0) {
        TextColor $$1 = this.color;
        Boolean $$2 = this.bold;
        Boolean $$3 = this.italic;
        Boolean $$4 = this.strikethrough;
        Boolean $$5 = this.underlined;
        Boolean $$6 = this.obfuscated;
        switch ($$0) {
            case OBFUSCATED: {
                $$6 = true;
                break;
            }
            case BOLD: {
                $$2 = true;
                break;
            }
            case STRIKETHROUGH: {
                $$4 = true;
                break;
            }
            case UNDERLINE: {
                $$5 = true;
                break;
            }
            case ITALIC: {
                $$3 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                $$6 = false;
                $$2 = false;
                $$4 = false;
                $$5 = false;
                $$3 = false;
                $$1 = TextColor.fromLegacyFormat($$0);
            }
        }
        return new Style($$1, this.shadowColor, $$2, $$3, $$5, $$4, $$6, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyFormats(ChatFormatting ... $$0) {
        TextColor $$1 = this.color;
        Boolean $$2 = this.bold;
        Boolean $$3 = this.italic;
        Boolean $$4 = this.strikethrough;
        Boolean $$5 = this.underlined;
        Boolean $$6 = this.obfuscated;
        block8: for (ChatFormatting $$7 : $$0) {
            switch ($$7) {
                case OBFUSCATED: {
                    $$6 = true;
                    continue block8;
                }
                case BOLD: {
                    $$2 = true;
                    continue block8;
                }
                case STRIKETHROUGH: {
                    $$4 = true;
                    continue block8;
                }
                case UNDERLINE: {
                    $$5 = true;
                    continue block8;
                }
                case ITALIC: {
                    $$3 = true;
                    continue block8;
                }
                case RESET: {
                    return EMPTY;
                }
                default: {
                    $$1 = TextColor.fromLegacyFormat($$7);
                }
            }
        }
        return new Style($$1, this.shadowColor, $$2, $$3, $$5, $$4, $$6, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyTo(Style $$0) {
        if (this == EMPTY) {
            return $$0;
        }
        if ($$0 == EMPTY) {
            return this;
        }
        return new Style(this.color != null ? this.color : $$0.color, this.shadowColor != null ? this.shadowColor : $$0.shadowColor, this.bold != null ? this.bold : $$0.bold, this.italic != null ? this.italic : $$0.italic, this.underlined != null ? this.underlined : $$0.underlined, this.strikethrough != null ? this.strikethrough : $$0.strikethrough, this.obfuscated != null ? this.obfuscated : $$0.obfuscated, this.clickEvent != null ? this.clickEvent : $$0.clickEvent, this.hoverEvent != null ? this.hoverEvent : $$0.hoverEvent, this.insertion != null ? this.insertion : $$0.insertion, this.font != null ? this.font : $$0.font);
    }

    public String toString() {
        final StringBuilder $$0 = new StringBuilder("{");
        class Collector {
            private boolean isNotFirst;

            Collector() {
            }

            private void prependSeparator() {
                if (this.isNotFirst) {
                    $$0.append(',');
                }
                this.isNotFirst = true;
            }

            void addFlagString(String $$02, @Nullable Boolean $$1) {
                if ($$1 != null) {
                    this.prependSeparator();
                    if (!$$1.booleanValue()) {
                        $$0.append('!');
                    }
                    $$0.append($$02);
                }
            }

            void addValueString(String $$02, @Nullable Object $$1) {
                if ($$1 != null) {
                    this.prependSeparator();
                    $$0.append($$02);
                    $$0.append('=');
                    $$0.append($$1);
                }
            }
        }
        Collector $$1 = new Collector();
        $$1.addValueString("color", this.color);
        $$1.addValueString("shadowColor", this.shadowColor);
        $$1.addFlagString("bold", this.bold);
        $$1.addFlagString("italic", this.italic);
        $$1.addFlagString("underlined", this.underlined);
        $$1.addFlagString("strikethrough", this.strikethrough);
        $$1.addFlagString("obfuscated", this.obfuscated);
        $$1.addValueString("clickEvent", this.clickEvent);
        $$1.addValueString("hoverEvent", this.hoverEvent);
        $$1.addValueString("insertion", this.insertion);
        $$1.addValueString("font", this.font);
        $$0.append("}");
        return $$0.toString();
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 instanceof Style) {
            Style $$1 = (Style)$$0;
            return this.bold == $$1.bold && Objects.equals(this.getColor(), $$1.getColor()) && Objects.equals(this.getShadowColor(), $$1.getShadowColor()) && this.italic == $$1.italic && this.obfuscated == $$1.obfuscated && this.strikethrough == $$1.strikethrough && this.underlined == $$1.underlined && Objects.equals(this.clickEvent, $$1.clickEvent) && Objects.equals(this.hoverEvent, $$1.hoverEvent) && Objects.equals(this.insertion, $$1.insertion) && Objects.equals(this.font, $$1.font);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
    }

    public static class Serializer {
        public static final MapCodec<Style> MAP_CODEC = RecordCodecBuilder.mapCodec($$02 -> $$02.group((App)TextColor.CODEC.optionalFieldOf("color").forGetter($$0 -> Optional.ofNullable($$0.color)), (App)ExtraCodecs.ARGB_COLOR_CODEC.optionalFieldOf("shadow_color").forGetter($$0 -> Optional.ofNullable($$0.shadowColor)), (App)Codec.BOOL.optionalFieldOf("bold").forGetter($$0 -> Optional.ofNullable($$0.bold)), (App)Codec.BOOL.optionalFieldOf("italic").forGetter($$0 -> Optional.ofNullable($$0.italic)), (App)Codec.BOOL.optionalFieldOf("underlined").forGetter($$0 -> Optional.ofNullable($$0.underlined)), (App)Codec.BOOL.optionalFieldOf("strikethrough").forGetter($$0 -> Optional.ofNullable($$0.strikethrough)), (App)Codec.BOOL.optionalFieldOf("obfuscated").forGetter($$0 -> Optional.ofNullable($$0.obfuscated)), (App)ClickEvent.CODEC.optionalFieldOf("click_event").forGetter($$0 -> Optional.ofNullable($$0.clickEvent)), (App)HoverEvent.CODEC.optionalFieldOf("hover_event").forGetter($$0 -> Optional.ofNullable($$0.hoverEvent)), (App)Codec.STRING.optionalFieldOf("insertion").forGetter($$0 -> Optional.ofNullable($$0.insertion)), (App)FontDescription.CODEC.optionalFieldOf("font").forGetter($$0 -> Optional.ofNullable($$0.font))).apply((Applicative)$$02, Style::create));
        public static final Codec<Style> CODEC = MAP_CODEC.codec();
        public static final StreamCodec<RegistryFriendlyByteBuf, Style> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    }
}

