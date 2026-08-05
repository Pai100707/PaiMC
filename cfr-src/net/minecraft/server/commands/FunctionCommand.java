/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import org.jspecify.annotations.Nullable;

public class FunctionCommand {
    private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.function.error.argument_not_compound", $$0));
    static final DynamicCommandExceptionType ERROR_NO_FUNCTIONS = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.function.scheduled.no_functions", $$0));
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("commands.function.instantiationFailure", $$0, $$1));
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = ($$0, $$1) -> {
        ServerFunctionManager $$2 = ((CommandSourceStack)$$0.getSource()).getServer().getFunctions();
        SharedSuggestionProvider.suggestResource($$2.getTagNames(), $$1, "#");
        return SharedSuggestionProvider.suggestResource($$2.getFunctionNames(), $$1);
    };
    static final Callbacks<CommandSourceStack> FULL_CONTEXT_CALLBACKS = new Callbacks<CommandSourceStack>(){

        @Override
        public void signalResult(CommandSourceStack $$0, Identifier $$1, int $$2) {
            $$0.sendSuccess(() -> Component.translatable("commands.function.result", Component.translationArg($$1), $$2), true);
        }
    };

    public static void register(CommandDispatcher<CommandSourceStack> $$0) {
        LiteralArgumentBuilder<CommandSourceStack> $$12 = Commands.literal("with");
        for (final DataCommands.DataProvider $$2 : DataCommands.SOURCE_PROVIDERS) {
            $$2.wrap((ArgumentBuilder<CommandSourceStack, ?>)$$12, $$1 -> $$1.executes((Command)new FunctionCustomExecutor(){

                @Override
                protected CompoundTag arguments(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                    return $$2.access($$0).getData();
                }
            }).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes((Command)new FunctionCustomExecutor(){

                @Override
                protected CompoundTag arguments(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                    return FunctionCommand.getArgumentTag(NbtPathArgument.getPath($$0, "path"), $$2.access($$0));
                }
            })));
        }
        $$0.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("function").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", FunctionArgument.functions()).suggests(SUGGEST_FUNCTION).executes((Command)new FunctionCustomExecutor(){

            @Override
            protected @Nullable CompoundTag arguments(CommandContext<CommandSourceStack> $$0) {
                return null;
            }
        })).then(Commands.argument("arguments", CompoundTagArgument.compoundTag()).executes((Command)new FunctionCustomExecutor(){

            @Override
            protected CompoundTag arguments(CommandContext<CommandSourceStack> $$0) {
                return CompoundTagArgument.getCompoundTag($$0, "arguments");
            }
        }))).then($$12)));
    }

    static CompoundTag getArgumentTag(NbtPathArgument.NbtPath $$0, DataAccessor $$1) throws CommandSyntaxException {
        Tag $$2 = DataCommands.getSingleTag($$0, $$1);
        if ($$2 instanceof CompoundTag) {
            CompoundTag $$3 = (CompoundTag)$$2;
            return $$3;
        }
        throw ERROR_ARGUMENT_NOT_COMPOUND.create((Object)$$2.getType().getName());
    }

    public static CommandSourceStack modifySenderForExecution(CommandSourceStack $$0) {
        return $$0.withSuppressedOutput().withMaximumPermission(LevelBasedPermissionSet.GAMEMASTER);
    }

    public static <T extends ExecutionCommandSource<T>> void queueFunctions(Collection<CommandFunction<T>> $$0, @Nullable CompoundTag $$1, T $$2, T $$3, ExecutionControl<T> $$4, Callbacks<T> $$5, ChainModifiers $$6) throws CommandSyntaxException {
        if ($$6.isReturn()) {
            FunctionCommand.queueFunctionsAsReturn($$0, $$1, $$2, $$3, $$4, $$5);
        } else {
            FunctionCommand.queueFunctionsNoReturn($$0, $$1, $$2, $$3, $$4, $$5);
        }
    }

    private static <T extends ExecutionCommandSource<T>> void instantiateAndQueueFunctions(@Nullable CompoundTag $$0, ExecutionControl<T> $$1, CommandDispatcher<T> $$2, T $$3, CommandFunction<T> $$4, Identifier $$5, CommandResultCallback $$6, boolean $$7) throws CommandSyntaxException {
        try {
            InstantiatedFunction<T> $$8 = $$4.instantiate($$0, $$2);
            $$1.queueNext(new CallFunction<T>($$8, $$6, $$7).bind($$3));
        }
        catch (FunctionInstantiationException $$9) {
            throw ERROR_FUNCTION_INSTANTATION_FAILURE.create((Object)$$5, (Object)$$9.messageComponent());
        }
    }

    private static <T extends ExecutionCommandSource<T>> CommandResultCallback decorateOutputIfNeeded(T $$0, Callbacks<T> $$1, Identifier $$2, CommandResultCallback $$3) {
        if ($$0.isSilent()) {
            return $$3;
        }
        return ($$4, $$5) -> {
            $$1.signalResult($$0, $$2, $$5);
            $$3.onResult($$4, $$5);
        };
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsAsReturn(Collection<CommandFunction<T>> $$0, @Nullable CompoundTag $$1, T $$2, T $$3, ExecutionControl<T> $$4, Callbacks<T> $$5) throws CommandSyntaxException {
        CommandDispatcher<T> $$6 = $$2.dispatcher();
        T $$7 = $$3.clearCallbacks();
        CommandResultCallback $$8 = CommandResultCallback.chain($$2.callback(), $$4.currentFrame().returnValueConsumer());
        for (CommandFunction<T> $$9 : $$0) {
            Identifier $$10 = $$9.id();
            CommandResultCallback $$11 = FunctionCommand.decorateOutputIfNeeded($$2, $$5, $$10, $$8);
            FunctionCommand.instantiateAndQueueFunctions($$1, $$4, $$6, $$7, $$9, $$10, $$11, true);
        }
        $$4.queueNext(FallthroughTask.instance());
    }

    private static <T extends ExecutionCommandSource<T>> void queueFunctionsNoReturn(Collection<CommandFunction<T>> $$0, @Nullable CompoundTag $$12, T $$22, T $$32, ExecutionControl<T> $$4, Callbacks<T> $$5) throws CommandSyntaxException {
        CommandDispatcher<T> $$6 = $$22.dispatcher();
        T $$7 = $$32.clearCallbacks();
        CommandResultCallback $$8 = $$22.callback();
        if ($$0.isEmpty()) {
            return;
        }
        if ($$0.size() == 1) {
            CommandFunction<T> $$9 = $$0.iterator().next();
            Identifier $$10 = $$9.id();
            CommandResultCallback $$11 = FunctionCommand.decorateOutputIfNeeded($$22, $$5, $$10, $$8);
            FunctionCommand.instantiateAndQueueFunctions($$12, $$4, $$6, $$7, $$9, $$10, $$11, false);
        } else if ($$8 == CommandResultCallback.EMPTY) {
            for (CommandFunction<T> $$122 : $$0) {
                Identifier $$13 = $$122.id();
                CommandResultCallback $$14 = FunctionCommand.decorateOutputIfNeeded($$22, $$5, $$13, $$8);
                FunctionCommand.instantiateAndQueueFunctions($$12, $$4, $$6, $$7, $$122, $$13, $$14, false);
            }
        } else {
            class Accumulator {
                boolean anyResult;
                int sum;

                Accumulator() {
                }

                public void add(int $$0) {
                    this.anyResult = true;
                    this.sum += $$0;
                }
            }
            Accumulator $$15 = new Accumulator();
            CommandResultCallback $$16 = ($$1, $$2) -> $$15.add($$2);
            for (CommandFunction<T> $$17 : $$0) {
                Identifier $$18 = $$17.id();
                CommandResultCallback $$19 = FunctionCommand.decorateOutputIfNeeded($$22, $$5, $$18, $$16);
                FunctionCommand.instantiateAndQueueFunctions($$12, $$4, $$6, $$7, $$17, $$18, $$19, false);
            }
            $$4.queueNext(($$2, $$3) -> {
                if ($$0.anyResult) {
                    $$8.onSuccess($$0.sum);
                }
            });
        }
    }

    public static interface Callbacks<T> {
        public void signalResult(T var1, Identifier var2, int var3);
    }

    static abstract class FunctionCustomExecutor
    extends CustomCommandExecutor.WithErrorHandling<CommandSourceStack>
    implements CustomCommandExecutor.CommandAdapter<CommandSourceStack> {
        FunctionCustomExecutor() {
        }

        protected abstract @Nullable CompoundTag arguments(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        @Override
        public void runGuarded(CommandSourceStack $$0, ContextChain<CommandSourceStack> $$1, ChainModifiers $$2, ExecutionControl<CommandSourceStack> $$3) throws CommandSyntaxException {
            CommandContext $$4 = $$1.getTopContext().copyFor((Object)$$0);
            Pair<Identifier, Collection<CommandFunction<CommandSourceStack>>> $$5 = FunctionArgument.getFunctionCollection((CommandContext<CommandSourceStack>)$$4, "name");
            Collection $$6 = (Collection)$$5.getSecond();
            if ($$6.isEmpty()) {
                throw ERROR_NO_FUNCTIONS.create((Object)Component.translationArg((Identifier)$$5.getFirst()));
            }
            CompoundTag $$7 = this.arguments((CommandContext<CommandSourceStack>)$$4);
            CommandSourceStack $$8 = FunctionCommand.modifySenderForExecution($$0);
            if ($$6.size() == 1) {
                $$0.sendSuccess(() -> Component.translatable("commands.function.scheduled.single", Component.translationArg(((CommandFunction)$$6.iterator().next()).id())), true);
            } else {
                $$0.sendSuccess(() -> Component.translatable("commands.function.scheduled.multiple", ComponentUtils.formatList($$6.stream().map(CommandFunction::id).toList(), Component::translationArg)), true);
            }
            FunctionCommand.queueFunctions($$6, $$7, $$0, $$8, $$3, FULL_CONTEXT_CALLBACKS, $$2);
        }

        @Override
        public /* synthetic */ void runGuarded(ExecutionCommandSource executionCommandSource, ContextChain contextChain, ChainModifiers chainModifiers, ExecutionControl executionControl) throws CommandSyntaxException {
            this.runGuarded((CommandSourceStack)executionCommandSource, (ContextChain<CommandSourceStack>)contextChain, chainModifiers, (ExecutionControl<CommandSourceStack>)executionControl);
        }
    }
}

