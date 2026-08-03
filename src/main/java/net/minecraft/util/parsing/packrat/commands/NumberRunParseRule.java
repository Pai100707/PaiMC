package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;
import org.jspecify.annotations.Nullable;

public abstract class NumberRunParseRule implements Rule<StringReader, String> {
   private final DelayedException<CommandSyntaxException> noValueError;
   private final DelayedException<CommandSyntaxException> underscoreNotAllowedError;

   public NumberRunParseRule(DelayedException<CommandSyntaxException> $$0, DelayedException<CommandSyntaxException> $$1) {
      this.noValueError = $$0;
      this.underscoreNotAllowedError = $$1;
   }

   @Nullable
   public String parse(ParseState<StringReader> $$0) {
      StringReader $$1 = $$0.input();
      $$1.skipWhitespace();
      String $$2 = $$1.getString();
      int $$3 = $$1.getCursor();
      int $$4 = $$3;

      while ($$4 < $$2.length() && this.isAccepted($$2.charAt($$4))) {
         $$4++;
      }

      int $$5 = $$4 - $$3;
      if ($$5 == 0) {
         $$0.errorCollector().store($$0.mark(), this.noValueError);
         return null;
      } else if ($$2.charAt($$3) != '_' && $$2.charAt($$4 - 1) != '_') {
         $$1.setCursor($$4);
         return $$2.substring($$3, $$4);
      } else {
         $$0.errorCollector().store($$0.mark(), this.underscoreNotAllowedError);
         return null;
      }
   }

   protected abstract boolean isAccepted(char var1);
}
