package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;

public class SwizzleArgument implements ArgumentType<EnumSet<Axis>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("xyz", "x");
   private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("arguments.swizzle.invalid"));

   public static SwizzleArgument swizzle() {
      return new SwizzleArgument();
   }

   public static EnumSet<Axis> getSwizzle(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (EnumSet<Axis>)$$0.getArgument($$1, EnumSet.class);
   }

   public EnumSet<Axis> parse(StringReader $$0) throws CommandSyntaxException {
      EnumSet<Axis> $$1 = EnumSet.noneOf(Axis.class);

      while ($$0.canRead() && $$0.peek() != ' ') {
         char $$2 = $$0.read();

         Axis $$6 = switch ($$2) {
            case 'x' -> Axis.X;
            case 'y' -> Axis.Y;
            case 'z' -> Axis.Z;
            default -> throw ERROR_INVALID.createWithContext($$0);
         };
         if ($$1.contains($$6)) {
            throw ERROR_INVALID.createWithContext($$0);
         }

         $$1.add($$6);
      }

      return $$1;
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
