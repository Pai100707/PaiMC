package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class GreedyPredicateParseRule implements Rule<StringReader, String> {
   private final int minSize;
   private final int maxSize;
   private final DelayedException<CommandSyntaxException> error;

   public GreedyPredicateParseRule(int $$0, DelayedException<CommandSyntaxException> $$1) {
      this($$0, Integer.MAX_VALUE, $$1);
   }

   public GreedyPredicateParseRule(int $$0, int $$1, DelayedException<CommandSyntaxException> $$2) {
      this.minSize = $$0;
      this.maxSize = $$1;
      this.error = $$2;
   }

   
   public String parse(ParseState<StringReader> $$0) {
      StringReader $$1 = $$0.input();
      String $$2 = $$1.getString();
      int $$3 = $$1.getCursor();
      int $$4 = $$3;

      while ($$4 < $$2.length() && this.isAccepted($$2.charAt($$4)) && $$4 - $$3 < this.maxSize) {
         $$4++;
      }

      int $$5 = $$4 - $$3;
      if ($$5 < this.minSize) {
         $$0.errorCollector().store($$0.mark(), this.error);
         return null;
      } else {
         $$1.setCursor($$4);
         return $$2.substring($$3, $$4);
      }
   }

   protected abstract boolean isAccepted(char var1);
}
