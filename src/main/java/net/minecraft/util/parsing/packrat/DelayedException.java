package net.minecraft.util.parsing.packrat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;

public interface DelayedException<T extends Exception> {
   T create(String var1, int var2);

   static DelayedException<CommandSyntaxException> create(SimpleCommandExceptionType $$0) {
      return ($$1, $$2) -> $$0.createWithContext(StringReaderTerms.createReader($$1, $$2));
   }

   static DelayedException<CommandSyntaxException> create(DynamicCommandExceptionType $$0, String $$1) {
      return ($$2, $$3) -> $$0.createWithContext(StringReaderTerms.createReader($$2, $$3), $$1);
   }
}
