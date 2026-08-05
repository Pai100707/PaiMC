/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.functions.FunctionBuilder;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface CommandFunction<T> {
    public Identifier id();

    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag var1, CommandDispatcher<T> var2) throws FunctionInstantiationException;

    private static boolean shouldConcatenateNextLine(CharSequence $$0) {
        int $$1 = $$0.length();
        return $$1 > 0 && $$0.charAt($$1 - 1) == '\\';
    }

    public static <T extends ExecutionCommandSource<T>> CommandFunction<T> fromLines(Identifier $$0, CommandDispatcher<T> $$1, T $$2, List<String> $$3) {
        FunctionBuilder<T> $$4 = new FunctionBuilder<T>();
        for (int $$5 = 0; $$5 < $$3.size(); ++$$5) {
            String $$11;
            int $$6 = $$5 + 1;
            String $$7 = $$3.get($$5).trim();
            if (CommandFunction.shouldConcatenateNextLine($$7)) {
                StringBuilder $$8 = new StringBuilder($$7);
                do {
                    if (++$$5 == $$3.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }
                    $$8.deleteCharAt($$8.length() - 1);
                    String $$9 = $$3.get($$5).trim();
                    $$8.append($$9);
                    CommandFunction.checkCommandLineLength($$8);
                } while (CommandFunction.shouldConcatenateNextLine($$8));
                String $$10 = $$8.toString();
            } else {
                $$11 = $$7;
            }
            CommandFunction.checkCommandLineLength($$11);
            StringReader $$12 = new StringReader($$11);
            if (!$$12.canRead() || $$12.peek() == '#') continue;
            if ($$12.peek() == '/') {
                $$12.skip();
                if ($$12.peek() == '/') {
                    throw new IllegalArgumentException("Unknown or invalid command '" + $$11 + "' on line " + $$6 + " (if you intended to make a comment, use '#' not '//')");
                }
                String $$13 = $$12.readUnquotedString();
                throw new IllegalArgumentException("Unknown or invalid command '" + $$11 + "' on line " + $$6 + " (did you mean '" + $$13 + "'? Do not use a preceding forwards slash.)");
            }
            if ($$12.peek() == '$') {
                $$4.addMacro($$11.substring(1), $$6, $$2);
                continue;
            }
            try {
                $$4.addCommand(CommandFunction.parseCommand($$1, $$2, $$12));
                continue;
            }
            catch (CommandSyntaxException $$14) {
                throw new IllegalArgumentException("Whilst parsing command on line " + $$6 + ": " + $$14.getMessage());
            }
        }
        return $$4.build($$0);
    }

    public static void checkCommandLineLength(CharSequence $$0) {
        if ($$0.length() > 2000000) {
            CharSequence $$1 = $$0.subSequence(0, Math.min(512, 2000000));
            throw new IllegalStateException("Command too long: " + $$0.length() + " characters, contents: " + String.valueOf($$1) + "...");
        }
    }

    public static <T extends ExecutionCommandSource<T>> UnboundEntryAction<T> parseCommand(CommandDispatcher<T> $$0, T $$1, StringReader $$2) throws CommandSyntaxException {
        ParseResults $$3 = $$0.parse($$2, $$1);
        Commands.validateParseResults($$3);
        Optional $$4 = ContextChain.tryFlatten((CommandContext)$$3.getContext().build($$2.getString()));
        if ($$4.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext($$3.getReader());
        }
        return new BuildContexts.Unbound($$2.getString(), (ContextChain)$$4.get());
    }
}

