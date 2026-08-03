package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.resources.Identifier;

public class IdentifierArgument implements ArgumentType<Identifier> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

   public static IdentifierArgument id() {
      return new IdentifierArgument();
   }

   public static Identifier getId(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Identifier)$$0.getArgument($$1, Identifier.class);
   }

   public Identifier parse(StringReader $$0) throws CommandSyntaxException {
      return Identifier.read($$0);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
