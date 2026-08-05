package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class TagParseRule<T> implements Rule<StringReader, Dynamic<?>> {
   private final TagParser<T> parser;

   public TagParseRule(DynamicOps<T> $$0) {
      this.parser = TagParser.create($$0);
   }

   
   public Dynamic<T> parse(ParseState<StringReader> $$0) {
      $$0.input().skipWhitespace();
      int $$1 = $$0.mark();

      try {
         return new Dynamic(this.parser.getOps(), this.parser.parseAsArgument($$0.input()));
      } catch (Exception var4) {
         $$0.errorCollector().store($$1, var4);
         return null;
      }
   }
}
