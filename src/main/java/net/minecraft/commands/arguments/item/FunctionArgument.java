package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FunctionArgument implements ArgumentType<FunctionArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("arguments.function.tag.unknown", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("arguments.function.unknown", new Object[]{$$0})
   );

   public static FunctionArgument functions() {
      return new FunctionArgument();
   }

   public FunctionArgument.Result parse(StringReader $$0) throws CommandSyntaxException {
      if ($$0.canRead() && $$0.peek() == '#') {
         $$0.skip();
         final Identifier $$1 = Identifier.read($$0);
         return new FunctionArgument.Result() {
            @Override
            public Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>> create(CommandContext<net.minecraft.commands.CommandSourceStack> $$0) throws CommandSyntaxException {
               return FunctionArgument.getFunctionTag($$0, $$1);
            }

            @Override
            public Pair<Identifier, Either<CommandFunction<net.minecraft.commands.CommandSourceStack>, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>>> unwrap(
               CommandContext<net.minecraft.commands.CommandSourceStack> $$0
            ) throws CommandSyntaxException {
               return Pair.of($$1, Either.right(FunctionArgument.getFunctionTag($$0, $$1)));
            }

            @Override
            public Pair<Identifier, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>> unwrapToCollection(
               CommandContext<net.minecraft.commands.CommandSourceStack> $$0
            ) throws CommandSyntaxException {
               return Pair.of($$1, FunctionArgument.getFunctionTag($$0, $$1));
            }
         };
      } else {
         final Identifier $$2 = Identifier.read($$0);
         return new FunctionArgument.Result() {
            @Override
            public Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>> create(CommandContext<net.minecraft.commands.CommandSourceStack> $$0) throws CommandSyntaxException {
               return Collections.singleton(FunctionArgument.getFunction($$0, $$2));
            }

            @Override
            public Pair<Identifier, Either<CommandFunction<net.minecraft.commands.CommandSourceStack>, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>>> unwrap(
               CommandContext<net.minecraft.commands.CommandSourceStack> $$0
            ) throws CommandSyntaxException {
               return Pair.of($$2, Either.left(FunctionArgument.getFunction($$0, $$2)));
            }

            @Override
            public Pair<Identifier, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>> unwrapToCollection(
               CommandContext<net.minecraft.commands.CommandSourceStack> $$0
            ) throws CommandSyntaxException {
               return Pair.of($$2, Collections.singleton(FunctionArgument.getFunction($$0, $$2)));
            }
         };
      }
   }

   static CommandFunction<net.minecraft.commands.CommandSourceStack> getFunction(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, Identifier $$1) throws CommandSyntaxException {
      return (CommandFunction<net.minecraft.commands.CommandSourceStack>)((net.minecraft.commands.CommandSourceStack)$$0.getSource())
         .getServer()
         .getFunctions()
         .get($$1)
         .orElseThrow(() -> ERROR_UNKNOWN_FUNCTION.create($$1.toString()));
   }

   static Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>> getFunctionTag(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, Identifier $$1
   ) throws CommandSyntaxException {
      Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>> $$2 = ((net.minecraft.commands.CommandSourceStack)$$0.getSource())
         .getServer()
         .getFunctions()
         .getTag($$1);
      if ($$2 == null) {
         throw ERROR_UNKNOWN_TAG.create($$1.toString());
      } else {
         return $$2;
      }
   }

   public static Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>> getFunctions(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1
   ) throws CommandSyntaxException {
      return ((FunctionArgument.Result)$$0.getArgument($$1, FunctionArgument.Result.class)).create($$0);
   }

   public static Pair<Identifier, Either<CommandFunction<net.minecraft.commands.CommandSourceStack>, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>>> getFunctionOrTag(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1
   ) throws CommandSyntaxException {
      return ((FunctionArgument.Result)$$0.getArgument($$1, FunctionArgument.Result.class)).unwrap($$0);
   }

   public static Pair<Identifier, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>> getFunctionCollection(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1
   ) throws CommandSyntaxException {
      return ((FunctionArgument.Result)$$0.getArgument($$1, FunctionArgument.Result.class)).unwrapToCollection($$0);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public interface Result {
      Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>> create(CommandContext<net.minecraft.commands.CommandSourceStack> var1) throws CommandSyntaxException;

      Pair<Identifier, Either<CommandFunction<net.minecraft.commands.CommandSourceStack>, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>>> unwrap(
         CommandContext<net.minecraft.commands.CommandSourceStack> var1
      ) throws CommandSyntaxException;

      Pair<Identifier, Collection<CommandFunction<net.minecraft.commands.CommandSourceStack>>> unwrapToCollection(
         CommandContext<net.minecraft.commands.CommandSourceStack> var1
      ) throws CommandSyntaxException;
   }
}
