package net.minecraft.util;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class StringDecomposer {
   private static final char REPLACEMENT_CHAR = '�';
   private static final Optional<Object> STOP_ITERATION = Optional.of(net.minecraft.util.Unit.INSTANCE);

   private static boolean feedChar(Style $$0, net.minecraft.util.FormattedCharSink $$1, int $$2, char $$3) {
      return Character.isSurrogate($$3) ? $$1.accept($$2, $$0, 65533) : $$1.accept($$2, $$0, $$3);
   }

   public static boolean iterate(String $$0, Style $$1, net.minecraft.util.FormattedCharSink $$2) {
      int $$3 = $$0.length();

      for (int $$4 = 0; $$4 < $$3; $$4++) {
         char $$5 = $$0.charAt($$4);
         if (Character.isHighSurrogate($$5)) {
            if ($$4 + 1 >= $$3) {
               if (!$$2.accept($$4, $$1, 65533)) {
                  return false;
               }
               break;
            }

            char $$6 = $$0.charAt($$4 + 1);
            if (Character.isLowSurrogate($$6)) {
               if (!$$2.accept($$4, $$1, Character.toCodePoint($$5, $$6))) {
                  return false;
               }

               $$4++;
            } else if (!$$2.accept($$4, $$1, 65533)) {
               return false;
            }
         } else if (!feedChar($$1, $$2, $$4, $$5)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateBackwards(String $$0, Style $$1, net.minecraft.util.FormattedCharSink $$2) {
      int $$3 = $$0.length();

      for (int $$4 = $$3 - 1; $$4 >= 0; $$4--) {
         char $$5 = $$0.charAt($$4);
         if (Character.isLowSurrogate($$5)) {
            if ($$4 - 1 < 0) {
               if (!$$2.accept(0, $$1, 65533)) {
                  return false;
               }
               break;
            }

            char $$6 = $$0.charAt($$4 - 1);
            if (Character.isHighSurrogate($$6)) {
               if (!$$2.accept(--$$4, $$1, Character.toCodePoint($$6, $$5))) {
                  return false;
               }
            } else if (!$$2.accept($$4, $$1, 65533)) {
               return false;
            }
         } else if (!feedChar($$1, $$2, $$4, $$5)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateFormatted(String $$0, Style $$1, net.minecraft.util.FormattedCharSink $$2) {
      return iterateFormatted($$0, 0, $$1, $$2);
   }

   public static boolean iterateFormatted(String $$0, int $$1, Style $$2, net.minecraft.util.FormattedCharSink $$3) {
      return iterateFormatted($$0, $$1, $$2, $$2, $$3);
   }

   public static boolean iterateFormatted(String $$0, int $$1, Style $$2, Style $$3, net.minecraft.util.FormattedCharSink $$4) {
      int $$5 = $$0.length();
      Style $$6 = $$2;

      for (int $$7 = $$1; $$7 < $$5; $$7++) {
         char $$8 = $$0.charAt($$7);
         if ($$8 == 167) {
            if ($$7 + 1 >= $$5) {
               break;
            }

            char $$9 = $$0.charAt($$7 + 1);
            ChatFormatting $$10 = ChatFormatting.getByCode($$9);
            if ($$10 != null) {
               $$6 = $$10 == ChatFormatting.RESET ? $$3 : $$6.applyLegacyFormat($$10);
            }

            $$7++;
         } else if (Character.isHighSurrogate($$8)) {
            if ($$7 + 1 >= $$5) {
               if (!$$4.accept($$7, $$6, 65533)) {
                  return false;
               }
               break;
            }

            char $$11 = $$0.charAt($$7 + 1);
            if (Character.isLowSurrogate($$11)) {
               if (!$$4.accept($$7, $$6, Character.toCodePoint($$8, $$11))) {
                  return false;
               }

               $$7++;
            } else if (!$$4.accept($$7, $$6, 65533)) {
               return false;
            }
         } else if (!feedChar($$6, $$4, $$7, $$8)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateFormatted(FormattedText $$0, Style $$1, net.minecraft.util.FormattedCharSink $$2) {
      return $$0.visit(($$1x, $$2x) -> iterateFormatted($$2x, 0, $$1x, $$2) ? Optional.empty() : STOP_ITERATION, $$1).isEmpty();
   }

   public static String filterBrokenSurrogates(String $$0) {
      StringBuilder $$1 = new StringBuilder();
      iterate($$0, Style.EMPTY, ($$1x, $$2, $$3) -> {
         $$1.appendCodePoint($$3);
         return true;
      });
      return $$1.toString();
   }

   public static String getPlainText(FormattedText $$0) {
      StringBuilder $$1 = new StringBuilder();
      iterateFormatted($$0, Style.EMPTY, ($$1x, $$2, $$3) -> {
         $$1.appendCodePoint($$3);
         return true;
      });
      return $$1.toString();
   }
}
