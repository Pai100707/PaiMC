package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.CustomCommandExecutor.CommandAdapter;
import net.minecraft.commands.execution.CustomCommandExecutor.WithErrorHandling;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import org.jspecify.annotations.Nullable;

public class FunctionCommand {
   private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.function.error.argument_not_compound", new Object[]{$$0})
   );
   static final DynamicCommandExceptionType ERROR_NO_FUNCTIONS = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.function.scheduled.no_functions", new Object[]{$$0})
   );
   @VisibleForTesting
   public static final Dynamic2CommandExceptionType ERROR_FUNCTION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.function.instantiationFailure", new Object[]{$$0, $$1})
   );
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = ($$0, $$1) -> {
      net.minecraft.server.ServerFunctionManager $$2 = ((CommandSourceStack)$$0.getSource()).getServer().getFunctions();
      SharedSuggestionProvider.suggestResource($$2.getTagNames(), $$1, "#");
      return SharedSuggestionProvider.suggestResource($$2.getFunctionNames(), $$1);
   };
   static final FunctionCommand.Callbacks<CommandSourceStack> FULL_CONTEXT_CALLBACKS = new FunctionCommand.Callbacks<CommandSourceStack>() {
      public void signalResult(CommandSourceStack $$0, Identifier $$1, int $$2) {
         $$0.sendSuccess(() -> Component.translatable("commands.function.result", new Object[]{Component.translationArg($$1), $$2}), true);
      }
   };

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      LiteralArgumentBuilder<CommandSourceStack> $$1 = Commands.literal("with");

      for (DataCommands.DataProvider $$2 : DataCommands.SOURCE_PROVIDERS) {
         $$2.wrap($$1, $$1x -> $$1x.executes(new FunctionCommand.FunctionCustomExecutor() {
            @Override
            protected CompoundTag arguments(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
               return $$2.access($$0).getData();
            }
         }).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(new FunctionCommand.FunctionCustomExecutor() {
            @Override
            protected CompoundTag arguments(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
               return FunctionCommand.getArgumentTag(NbtPathArgument.getPath($$0, "path"), $$2.access($$0));
            }
         })));
      }

      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("function").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", FunctionArgument.functions())
                        .suggests(SUGGEST_FUNCTION)
                        .executes(new FunctionCommand.FunctionCustomExecutor() {
                           @Nullable
                           @Override
                           protected CompoundTag arguments(CommandContext<CommandSourceStack> $$0) {
                              return null;
                           }
                        }))
                     .then(Commands.argument("arguments", CompoundTagArgument.compoundTag()).executes(new FunctionCommand.FunctionCustomExecutor() {
                        @Override
                        protected CompoundTag arguments(CommandContext<CommandSourceStack> $$0) {
                           return CompoundTagArgument.getCompoundTag($$0, "arguments");
                        }
                     })))
                  .then($$1)
            )
      );
   }

   static CompoundTag getArgumentTag(NbtPath $$0, DataAccessor $$1) throws CommandSyntaxException {
      Tag $$2 = DataCommands.getSingleTag($$0, $$1);
      if ($$2 instanceof CompoundTag $$3) {
         return $$3;
      } else {
         throw ERROR_ARGUMENT_NOT_COMPOUND.create($$2.getType().getName());
      }
   }

   public static CommandSourceStack modifySenderForExecution(CommandSourceStack $$0) {
      return $$0.withSuppressedOutput().withMaximumPermission(LevelBasedPermissionSet.GAMEMASTER);
   }

   public static <T extends ExecutionCommandSource<T>> void queueFunctions(
      Collection<CommandFunction<T>> $$0,
      @Nullable CompoundTag $$1,
      T $$2,
      T $$3,
      ExecutionControl<T> $$4,
      FunctionCommand.Callbacks<T> $$5,
      ChainModifiers $$6
   ) throws CommandSyntaxException {
      if ($$6.isReturn()) {
         queueFunctionsAsReturn($$0, $$1, $$2, $$3, $$4, $$5);
      } else {
         queueFunctionsNoReturn($$0, $$1, $$2, $$3, $$4, $$5);
      }
   }

   private static <T extends ExecutionCommandSource<T>> void instantiateAndQueueFunctions(
      @Nullable CompoundTag $$0,
      ExecutionControl<T> $$1,
      CommandDispatcher<T> $$2,
      T $$3,
      CommandFunction<T> $$4,
      Identifier $$5,
      CommandResultCallback $$6,
      boolean $$7
   ) throws CommandSyntaxException {
      try {
         InstantiatedFunction<T> $$8 = $$4.instantiate($$0, $$2);
         $$1.queueNext(new CallFunction($$8, $$6, $$7).bind($$3));
      } catch (FunctionInstantiationException var9) {
         throw ERROR_FUNCTION_INSTANTATION_FAILURE.create($$5, var9.messageComponent());
      }
   }

   private static <T extends ExecutionCommandSource<T>> CommandResultCallback decorateOutputIfNeeded(
      T $$0, FunctionCommand.Callbacks<T> $$1, Identifier $$2, CommandResultCallback $$3
   ) {
      return $$0.isSilent() ? $$3 : ($$4, $$5) -> {
         $$1.signalResult($$0, $$2, $$5);
         $$3.onResult($$4, $$5);
      };
   }

   private static <T extends ExecutionCommandSource<T>> void queueFunctionsAsReturn(
      Collection<CommandFunction<T>> $$0, @Nullable CompoundTag $$1, T $$2, T $$3, ExecutionControl<T> $$4, FunctionCommand.Callbacks<T> $$5
   ) throws CommandSyntaxException {
      CommandDispatcher<T> $$6 = $$2.dispatcher();
      T $$7 = (T)$$3.clearCallbacks();
      CommandResultCallback $$8 = CommandResultCallback.chain($$2.callback(), $$4.currentFrame().returnValueConsumer());

      for (CommandFunction<T> $$9 : $$0) {
         Identifier $$10 = $$9.id();
         CommandResultCallback $$11 = decorateOutputIfNeeded($$2, $$5, $$10, $$8);
         instantiateAndQueueFunctions($$1, $$4, $$6, $$7, $$9, $$10, $$11, true);
      }

      $$4.queueNext(FallthroughTask.instance());
   }

   private static <T extends ExecutionCommandSource<T>> void queueFunctionsNoReturn(
      Collection<CommandFunction<T>> $$0, @Nullable CompoundTag $$1, T $$2, T $$3, ExecutionControl<T> $$4, FunctionCommand.Callbacks<T> $$5
   ) throws CommandSyntaxException {
      CommandDispatcher<T> $$6 = $$2.dispatcher();
      T $$7 = (T)$$3.clearCallbacks();
      CommandResultCallback $$8 = $$2.callback();
      if (!$$0.isEmpty()) {
         if ($$0.size() == 1) {
            CommandFunction<T> $$9 = $$0.iterator().next();
            Identifier $$10 = $$9.id();
            CommandResultCallback $$11 = decorateOutputIfNeeded($$2, $$5, $$10, $$8);
            instantiateAndQueueFunctions($$1, $$4, $$6, $$7, $$9, $$10, $$11, false);
         } else if ($$8 == CommandResultCallback.EMPTY) {
            for (CommandFunction<T> $$12 : $$0) {
               Identifier $$13 = $$12.id();
               CommandResultCallback $$14 = decorateOutputIfNeeded($$2, $$5, $$13, $$8);
               instantiateAndQueueFunctions($$1, $$4, $$6, $$7, $$12, $$13, $$14, false);
            }
         } else {
            class Accumulator {
               boolean anyResult;
               int sum;

               public void add(int $$0) {
                  this.anyResult = true;
                  this.sum += $$0;
               }
            }

            Accumulator $$15 = new Accumulator();
            CommandResultCallback $$16 = ($$1x, $$2x) -> $$15.add($$2x);

            for (CommandFunction<T> $$17 : $$0) {
               Identifier $$18 = $$17.id();
               CommandResultCallback $$19 = decorateOutputIfNeeded($$2, $$5, $$18, $$16);
               instantiateAndQueueFunctions($$1, $$4, $$6, $$7, $$17, $$18, $$19, false);
            }

            $$4.queueNext(($$2x, $$3x) -> {
               if ($$15.anyResult) {
                  $$8.onSuccess($$15.sum);
               }
            });
         }
      }
   }

   public interface Callbacks<T> {
      void signalResult(T var1, Identifier var2, int var3);
   }

   abstract static class FunctionCustomExecutor extends WithErrorHandling<CommandSourceStack> implements CommandAdapter<CommandSourceStack> {
      @Nullable
      protected abstract CompoundTag arguments(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

      public void runGuarded(CommandSourceStack $$0, ContextChain<CommandSourceStack> $$1, ChainModifiers $$2, ExecutionControl<CommandSourceStack> $$3) throws CommandSyntaxException {
         CommandContext<CommandSourceStack> $$4 = $$1.getTopContext().copyFor($$0);
         Pair<Identifier, Collection<CommandFunction<CommandSourceStack>>> $$5 = FunctionArgument.getFunctionCollection($$4, "name");
         Collection<CommandFunction<CommandSourceStack>> $$6 = (Collection<CommandFunction<CommandSourceStack>>)$$5.getSecond();
         if ($$6.isEmpty()) {
            throw FunctionCommand.ERROR_NO_FUNCTIONS.create(Component.translationArg((Identifier)$$5.getFirst()));
         } else {
            CompoundTag $$7 = this.arguments($$4);
            CommandSourceStack $$8 = FunctionCommand.modifySenderForExecution($$0);
            if ($$6.size() == 1) {
               $$0.sendSuccess(
                  () -> Component.translatable("commands.function.scheduled.single", new Object[]{Component.translationArg($$6.iterator().next().id())}), true
               );
            } else {
               $$0.sendSuccess(
                  () -> Component.translatable(
                     "commands.function.scheduled.multiple",
                     new Object[]{ComponentUtils.formatList($$6.stream().map(CommandFunction::id).toList(), Component::translationArg)}
                  ),
                  true
               );
            }

            FunctionCommand.queueFunctions($$6, $$7, $$0, $$8, $$3, FunctionCommand.FULL_CONTEXT_CALLBACKS, $$2);
         }
      }
   }
}
