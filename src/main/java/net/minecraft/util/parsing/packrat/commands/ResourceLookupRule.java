package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class ResourceLookupRule<C, V> implements Rule<StringReader, V>, ResourceSuggestion {
   private final NamedRule<StringReader, Identifier> idParser;
   protected final C context;
   private final DelayedException<CommandSyntaxException> error;

   protected ResourceLookupRule(NamedRule<StringReader, Identifier> $$0, C $$1) {
      this.idParser = $$0;
      this.context = $$1;
      this.error = DelayedException.create(Identifier.ERROR_INVALID);
   }

   
   @Override
   public V parse(ParseState<StringReader> $$0) {
      $$0.input().skipWhitespace();
      int $$1 = $$0.mark();
      Identifier $$2 = $$0.parse(this.idParser);
      if ($$2 != null) {
         try {
            return this.validateElement((ImmutableStringReader)$$0.input(), $$2);
         } catch (Exception var5) {
            $$0.errorCollector().store($$1, this, var5);
            return null;
         }
      } else {
         $$0.errorCollector().store($$1, this, this.error);
         return null;
      }
   }

   protected abstract V validateElement(ImmutableStringReader var1, Identifier var2) throws Exception;
}
