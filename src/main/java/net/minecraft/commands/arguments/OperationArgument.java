package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.ScoreAccess;

public class OperationArgument implements ArgumentType<OperationArgument.Operation> {
   private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
   private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType(
      Component.translatable("arguments.operation.invalid")
   );
   private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType(Component.translatable("arguments.operation.div0"));

   public static OperationArgument operation() {
      return new OperationArgument();
   }

   public static OperationArgument.Operation getOperation(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (OperationArgument.Operation)$$0.getArgument($$1, OperationArgument.Operation.class);
   }

   public OperationArgument.Operation parse(StringReader $$0) throws CommandSyntaxException {
      if (!$$0.canRead()) {
         throw ERROR_INVALID_OPERATION.createWithContext($$0);
      } else {
         int $$1 = $$0.getCursor();

         while ($$0.canRead() && $$0.peek() != ' ') {
            $$0.skip();
         }

         return getOperation($$0.getString().substring($$1, $$0.getCursor()));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, $$1);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static OperationArgument.Operation getOperation(String $$0) throws CommandSyntaxException {
      return (OperationArgument.Operation)($$0.equals("><") ? ($$0x, $$1) -> {
         int $$2 = $$0x.get();
         $$0x.set($$1.get());
         $$1.set($$2);
      } : getSimpleOperation($$0));
   }

   private static OperationArgument.SimpleOperation getSimpleOperation(String $$0) throws CommandSyntaxException {
      return switch ($$0) {
         case "=" -> ($$0x, $$1) -> $$1;
         case "+=" -> Integer::sum;
         case "-=" -> ($$0x, $$1) -> $$0x - $$1;
         case "*=" -> ($$0x, $$1) -> $$0x * $$1;
         case "/=" -> ($$0x, $$1) -> {
            if ($$1 == 0) {
               throw ERROR_DIVIDE_BY_ZERO.create();
            } else {
               return Mth.floorDiv($$0x, $$1);
            }
         };
         case "%=" -> ($$0x, $$1) -> {
            if ($$1 == 0) {
               throw ERROR_DIVIDE_BY_ZERO.create();
            } else {
               return Mth.positiveModulo($$0x, $$1);
            }
         };
         case "<" -> Math::min;
         case ">" -> Math::max;
         default -> throw ERROR_INVALID_OPERATION.create();
      };
   }

   @FunctionalInterface
   public interface Operation {
      void apply(ScoreAccess var1, ScoreAccess var2) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface SimpleOperation extends OperationArgument.Operation {
      int apply(int var1, int var2) throws CommandSyntaxException;

      @Override
      default void apply(ScoreAccess $$0, ScoreAccess $$1) throws CommandSyntaxException {
         $$0.set(this.apply($$0.get(), $$1.get()));
      }
   }
}
