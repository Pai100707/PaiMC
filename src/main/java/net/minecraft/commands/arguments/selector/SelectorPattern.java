package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record SelectorPattern(String pattern, EntitySelector resolved) {
   public static final Codec<SelectorPattern> CODEC = Codec.STRING.comapFlatMap(SelectorPattern::parse, SelectorPattern::pattern);

   public static DataResult<SelectorPattern> parse(String $$0) {
      try {
         EntitySelectorParser $$1 = new EntitySelectorParser(new StringReader($$0), true);
         return DataResult.success(new SelectorPattern($$0, $$1.parse()));
      } catch (CommandSyntaxException var2) {
         return DataResult.error(() -> "Invalid selector component: " + $$0 + ": " + var2.getMessage());
      }
   }

   @Override
   public boolean equals(Object $$0) {
      return $$0 instanceof SelectorPattern $$1 && this.pattern.equals($$1.pattern);
   }

   @Override
   public int hashCode() {
      return this.pattern.hashCode();
   }

   @Override
   public String toString() {
      return this.pattern;
   }
}
