/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 */
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
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FunctionArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.function.tag.unknown", $$0));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.function.unknown", $$0));

    public static FunctionArgument functions() {
        return new FunctionArgument();
    }

    public Result parse(StringReader $$0) throws CommandSyntaxException {
        if ($$0.canRead() && $$0.peek() == '#') {
            $$0.skip();
            final Identifier $$1 = Identifier.read($$0);
            return new Result(){

                @Override
                public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                    return FunctionArgument.getFunctionTag($$0, $$1);
                }

                @Override
                public Pair<Identifier, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                    return Pair.of((Object)$$1, (Object)Either.right(FunctionArgument.getFunctionTag($$0, $$1)));
                }

                @Override
                public Pair<Identifier, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                    return Pair.of((Object)$$1, FunctionArgument.getFunctionTag($$0, $$1));
                }
            };
        }
        final Identifier $$2 = Identifier.read($$0);
        return new Result(){

            @Override
            public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                return Collections.singleton(FunctionArgument.getFunction($$0, $$2));
            }

            @Override
            public Pair<Identifier, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                return Pair.of((Object)$$2, (Object)Either.left(FunctionArgument.getFunction($$0, $$2)));
            }

            @Override
            public Pair<Identifier, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> $$0) throws CommandSyntaxException {
                return Pair.of((Object)$$2, Collections.singleton(FunctionArgument.getFunction($$0, $$2)));
            }
        };
    }

    static CommandFunction<CommandSourceStack> getFunction(CommandContext<CommandSourceStack> $$0, Identifier $$1) throws CommandSyntaxException {
        return ((CommandSourceStack)$$0.getSource()).getServer().getFunctions().get($$1).orElseThrow(() -> ERROR_UNKNOWN_FUNCTION.create((Object)$$1.toString()));
    }

    static Collection<CommandFunction<CommandSourceStack>> getFunctionTag(CommandContext<CommandSourceStack> $$0, Identifier $$1) throws CommandSyntaxException {
        List<CommandFunction<CommandSourceStack>> $$2 = ((CommandSourceStack)$$0.getSource()).getServer().getFunctions().getTag($$1);
        if ($$2 == null) {
            throw ERROR_UNKNOWN_TAG.create((Object)$$1.toString());
        }
        return $$2;
    }

    public static Collection<CommandFunction<CommandSourceStack>> getFunctions(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ((Result)$$0.getArgument($$1, Result.class)).create($$0);
    }

    public static Pair<Identifier, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> getFunctionOrTag(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ((Result)$$0.getArgument($$1, Result.class)).unwrap($$0);
    }

    public static Pair<Identifier, Collection<CommandFunction<CommandSourceStack>>> getFunctionCollection(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ((Result)$$0.getArgument($$1, Result.class)).unwrapToCollection($$0);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static interface Result {
        public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public Pair<Identifier, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public Pair<Identifier, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }
}

