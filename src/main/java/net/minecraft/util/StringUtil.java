package net.minecraft.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class StringUtil {
   private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
   private static final Pattern LINE_PATTERN = Pattern.compile("\\r\\n|\\v");
   private static final Pattern LINE_END_PATTERN = Pattern.compile("(?:\\r\\n|\\v)$");

   public static String formatTickDuration(int $$0, float $$1) {
      int $$2 = net.minecraft.util.Mth.floor($$0 / $$1);
      int $$3 = $$2 / 60;
      $$2 %= 60;
      int $$4 = $$3 / 60;
      $$3 %= 60;
      return $$4 > 0 ? String.format(Locale.ROOT, "%02d:%02d:%02d", $$4, $$3, $$2) : String.format(Locale.ROOT, "%02d:%02d", $$3, $$2);
   }

   public static String stripColor(String $$0) {
      return STRIP_COLOR_PATTERN.matcher($$0).replaceAll("");
   }

   public static boolean isNullOrEmpty(@Nullable String $$0) {
      return StringUtils.isEmpty($$0);
   }

   public static String truncateStringIfNecessary(String $$0, int $$1, boolean $$2) {
      if ($$0.length() <= $$1) {
         return $$0;
      } else {
         return $$2 && $$1 > 3 ? $$0.substring(0, $$1 - 3) + "..." : $$0.substring(0, $$1);
      }
   }

   public static int lineCount(String $$0) {
      if ($$0.isEmpty()) {
         return 0;
      } else {
         Matcher $$1 = LINE_PATTERN.matcher($$0);
         int $$2 = 1;

         while ($$1.find()) {
            $$2++;
         }

         return $$2;
      }
   }

   public static boolean endsWithNewLine(String $$0) {
      return LINE_END_PATTERN.matcher($$0).find();
   }

   public static String trimChatMessage(String $$0) {
      return truncateStringIfNecessary($$0, 256, false);
   }

   public static boolean isAllowedChatCharacter(int $$0) {
      return $$0 != 167 && $$0 >= 32 && $$0 != 127;
   }

   public static boolean isValidPlayerName(String $$0) {
      return $$0.length() > 16 ? false : $$0.chars().filter($$0x -> $$0x <= 32 || $$0x >= 127).findAny().isEmpty();
   }

   public static String filterText(String $$0) {
      return filterText($$0, false);
   }

   public static String filterText(String $$0, boolean $$1) {
      StringBuilder $$2 = new StringBuilder();

      for (char $$3 : $$0.toCharArray()) {
         if (isAllowedChatCharacter($$3)) {
            $$2.append($$3);
         } else if ($$1 && $$3 == '\n') {
            $$2.append($$3);
         }
      }

      return $$2.toString();
   }

   public static boolean isWhitespace(int $$0) {
      return Character.isWhitespace($$0) || Character.isSpaceChar($$0);
   }

   public static boolean isBlank(@Nullable String $$0) {
      return $$0 != null && !$$0.isEmpty() ? $$0.chars().allMatch(net.minecraft.util.StringUtil::isWhitespace) : true;
   }
}
