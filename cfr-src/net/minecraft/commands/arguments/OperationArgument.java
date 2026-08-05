/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.ScoreAccess;

public class OperationArgument
implements ArgumentType<Operation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
    private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType((Message)Component.translatable("arguments.operation.invalid"));
    private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType((Message)Component.translatable("arguments.operation.div0"));

    public static OperationArgument operation() {
        return new OperationArgument();
    }

    public static Operation getOperation(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (Operation)$$0.getArgument($$1, Operation.class);
    }

    public Operation parse(StringReader $$0) throws CommandSyntaxException {
        if ($$0.canRead()) {
            int $$1 = $$0.getCursor();
            while ($$0.canRead() && $$0.peek() != ' ') {
                $$0.skip();
            }
            return OperationArgument.getOperation($$0.getString().substring($$1, $$0.getCursor()));
        }
        throw ERROR_INVALID_OPERATION.createWithContext((ImmutableStringReader)$$0);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        return SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, $$1);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static Operation getOperation(String $$02) throws CommandSyntaxException {
        if ($$02.equals("><")) {
            return ($$0, $$1) -> {
                int $$2 = $$0.get();
                $$0.set($$1.get());
                $$1.set($$2);
            };
        }
        return OperationArgument.getSimpleOperation($$02);
    }

    private static SimpleOperation getSimpleOperation(String $$02) throws CommandSyntaxException {
        return switch ($$02) {
            case "=" -> ($$0, $$1) -> $$1;
            case "+=" -> Integer::sum;
            case "-=" -> ($$0, $$1) -> $$0 - $$1;
            case "*=" -> ($$0, $$1) -> $$0 * $$1;
            case "/=" -> ($$0, $$1) -> {
                if ($$1 == 0) {
                    throw ERROR_DIVIDE_BY_ZERO.create();
                }
                return Mth.floorDiv($$0, $$1);
            };
            case "%=" -> ($$0, $$1) -> {
                if ($$1 == 0) {
                    throw ERROR_DIVIDE_BY_ZERO.create();
                }
                return Mth.positiveModulo($$0, $$1);
            };
            case "<" -> Math::min;
            case ">" -> Math::max;
            default -> throw ERROR_INVALID_OPERATION.create();
        };
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    @FunctionalInterface
    public static interface Operation {
        public void apply(ScoreAccess var1, ScoreAccess var2) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface SimpleOperation
    extends Operation {
        public int apply(int var1, int var2) throws CommandSyntaxException;

        @Override
        default public void apply(ScoreAccess $$0, ScoreAccess $$1) throws CommandSyntaxException {
            $$0.set(this.apply($$0.get(), $$1.get()));
        }
    }
}

