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
   private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = Stream.of(ChatFormatting.values())
      .filter(ChatFormatting::isColor)
      .collect(ImmutableMap.toImmutableMap(Function.identity(), $$0 -> new TextColor($$0.getColor(), $$0.getName())));
   private static final Map<String, TextColor> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values()
      .stream()
      .collect(ImmutableMap.toImmutableMap($$0 -> $$0.name, Function.identity()));
   private final int value;
   @Nullable
   private final String name;

   private TextColor(int $$0, String $$1) {
      this.value = $$0 & 16777215;
      this.name = $$1;
   }

   private TextColor(int $$0) {
      this.value = $$0 & 16777215;
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

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if ($$0 != null && this.getClass() == $$0.getClass()) {
         TextColor $$1 = (TextColor)$$0;
         return this.value == $$1.value;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value, this.name);
   }

   @Override
   public String toString() {
      return this.serialize();
   }

   @Nullable
   public static TextColor fromLegacyFormat(ChatFormatting $$0) {
      return LEGACY_FORMAT_TO_COLOR.get($$0);
   }

   public static TextColor fromRgb(int $$0) {
      return new TextColor($$0);
   }

   public static DataResult<TextColor> parseColor(String $$0) {
      if ($$0.startsWith("#")) {
         try {
            int $$1 = Integer.parseInt($$0.substring(1), 16);
            return $$1 >= 0 && $$1 <= 16777215
               ? DataResult.success(fromRgb($$1), Lifecycle.stable())
               : DataResult.error(() -> "Color value out of range: " + $$0);
         } catch (NumberFormatException var2) {
            return DataResult.error(() -> "Invalid color value: " + $$0);
         }
      } else {
         TextColor $$3 = NAMED_COLORS.get($$0);
         return $$3 == null ? DataResult.error(() -> "Invalid color name: " + $$0) : DataResult.success($$3, Lifecycle.stable());
      }
   }
}
