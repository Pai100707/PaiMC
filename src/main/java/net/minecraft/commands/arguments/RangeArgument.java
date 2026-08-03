package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;

public interface RangeArgument<T extends MinMaxBounds<?>> extends ArgumentType<T> {
   static RangeArgument.Ints intRange() {
      return new RangeArgument.Ints();
   }

   static RangeArgument.Floats floatRange() {
      return new RangeArgument.Floats();
   }

   public static class Floats implements RangeArgument<Doubles> {
      private static final Collection<String> EXAMPLES = Arrays.asList("0..5.2", "0", "-5.4", "-100.76..", "..100");

      public static Doubles getRange(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
         return (Doubles)$$0.getArgument($$1, Doubles.class);
      }

      public Doubles parse(StringReader $$0) throws CommandSyntaxException {
         return Doubles.fromReader($$0);
      }

      public Collection<String> getExamples() {
         return EXAMPLES;
      }
   }

   public static class Ints implements RangeArgument<net.minecraft.advancements.criterion.MinMaxBounds.Ints> {
      private static final Collection<String> EXAMPLES = Arrays.asList("0..5", "0", "-5", "-100..", "..100");

      public static net.minecraft.advancements.criterion.MinMaxBounds.Ints getRange(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
         return (net.minecraft.advancements.criterion.MinMaxBounds.Ints)$$0.getArgument($$1, net.minecraft.advancements.criterion.MinMaxBounds.Ints.class);
      }

      public net.minecraft.advancements.criterion.MinMaxBounds.Ints parse(StringReader $$0) throws CommandSyntaxException {
         return net.minecraft.advancements.criterion.MinMaxBounds.Ints.fromReader($$0);
      }

      public Collection<String> getExamples() {
         return EXAMPLES;
      }
   }
}
