package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;

public enum ChatFormatting implements StringRepresentable {
   BLACK("BLACK", '0', 0, 0),
   DARK_BLUE("DARK_BLUE", '1', 1, 170),
   DARK_GREEN("DARK_GREEN", '2', 2, 43520),
   DARK_AQUA("DARK_AQUA", '3', 3, 43690),
   DARK_RED("DARK_RED", '4', 4, 11141120),
   DARK_PURPLE("DARK_PURPLE", '5', 5, 11141290),
   GOLD("GOLD", '6', 6, 16755200),
   GRAY("GRAY", '7', 7, 11184810),
   DARK_GRAY("DARK_GRAY", '8', 8, 5592405),
   BLUE("BLUE", '9', 9, 5592575),
   GREEN("GREEN", 'a', 10, 5635925),
   AQUA("AQUA", 'b', 11, 5636095),
   RED("RED", 'c', 12, 16733525),
   LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, 16733695),
   YELLOW("YELLOW", 'e', 14, 16777045),
   WHITE("WHITE", 'f', 15, 16777215),
   OBFUSCATED("OBFUSCATED", 'k', true),
   BOLD("BOLD", 'l', true),
   STRIKETHROUGH("STRIKETHROUGH", 'm', true),
   UNDERLINE("UNDERLINE", 'n', true),
   ITALIC("ITALIC", 'o', true),
   RESET("RESET", 'r', -1, null);

   public static final Codec<ChatFormatting> CODEC = StringRepresentable.fromEnum(ChatFormatting::values);
   public static final Codec<ChatFormatting> COLOR_CODEC = CODEC.validate(
      $$0 -> $$0.isFormat() ? DataResult.error(() -> "Formatting was not a valid color: " + $$0) : DataResult.success($$0)
   );
   public static final char PREFIX_CODE = '§';
   private static final Map<String, ChatFormatting> FORMATTING_BY_NAME = Arrays.stream(values())
      .collect(Collectors.toMap($$0 -> cleanName($$0.name), $$0 -> (ChatFormatting)$$0));
   private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
   private final String name;
   private final char code;
   private final boolean isFormat;
   private final String toString;
   private final int id;
   
   private final Integer color;

   private static String cleanName(String $$0) {
      return $$0.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
   }

   private ChatFormatting(final String $$0, final char $$1, final int $$2, final Integer $$3) {
      this($$0, $$1, false, $$2, $$3);
   }

   private ChatFormatting(final String $$0, final char $$1, final boolean $$2) {
      this($$0, $$1, $$2, -1, null);
   }

   private ChatFormatting(final String $$0, final char $$1, final boolean $$2, final int $$3, final Integer $$4) {
      this.name = $$0;
      this.code = $$1;
      this.isFormat = $$2;
      this.id = $$3;
      this.color = $$4;
      this.toString = "§" + $$1;
   }

   public char getChar() {
      return this.code;
   }

   public int getId() {
      return this.id;
   }

   public boolean isFormat() {
      return this.isFormat;
   }

   public boolean isColor() {
      return !this.isFormat && this != RESET;
   }

   
   public Integer getColor() {
      return this.color;
   }

   public String getName() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   @Override
   public String toString() {
      return this.toString;
   }

   @Contract("!null->!null;_->_")
   
   public static String stripFormatting(String $$0) {
      return $$0 == null ? null : STRIP_FORMATTING_PATTERN.matcher($$0).replaceAll("");
   }

   
   public static ChatFormatting getByName(String $$0) {
      return $$0 == null ? null : FORMATTING_BY_NAME.get(cleanName($$0));
   }

   
   public static ChatFormatting getById(int $$0) {
      if ($$0 < 0) {
         return RESET;
      } else {
         for (ChatFormatting $$1 : values()) {
            if ($$1.getId() == $$0) {
               return $$1;
            }
         }

         return null;
      }
   }

   
   public static ChatFormatting getByCode(char $$0) {
      char $$1 = Character.toLowerCase($$0);

      for (ChatFormatting $$2 : values()) {
         if ($$2.code == $$1) {
            return $$2;
         }
      }

      return null;
   }

   public static Collection<String> getNames(boolean $$0, boolean $$1) {
      List<String> $$2 = Lists.newArrayList();

      for (ChatFormatting $$3 : values()) {
         if ((!$$3.isColor() || $$0) && (!$$3.isFormat() || $$1)) {
            $$2.add($$3.getName());
         }
      }

      return $$2;
   }

   public String getSerializedName() {
      return this.getName();
   }
}
