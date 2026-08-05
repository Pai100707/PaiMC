package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class IdentifierParseRule implements Rule<StringReader, Identifier> {
   public static final Rule<StringReader, Identifier> INSTANCE = new IdentifierParseRule();

   private IdentifierParseRule() {
   }

   
   public Identifier parse(ParseState<StringReader> $$0) {
      $$0.input().skipWhitespace();

      try {
         return Identifier.readNonEmpty($$0.input());
      } catch (CommandSyntaxException var3) {
         return null;
      }
   }
}
