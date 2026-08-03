package net.minecraft.commands;

import com.mojang.brigadier.StringReader;
import net.minecraft.CharPredicate;

public class ParserUtils {
   public static String readWhile(StringReader $$0, CharPredicate $$1) {
      int $$2 = $$0.getCursor();

      while ($$0.canRead() && $$1.test($$0.peek())) {
         $$0.skip();
      }

      return $$0.getString().substring($$2, $$0.getCursor());
   }
}
