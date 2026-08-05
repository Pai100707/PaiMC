/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class DebugCommand {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.alreadyRunning"));
    static final SimpleCommandExceptionType NO_RECURSIVE_TRACES = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.function.noRecursion"));
    static final SimpleCommandExceptionType NO_RETURN_RUN = new SimpleCommandExceptionType((Message)Component.translatable("commands.debug.function.noReturnRun"));

    public static void register(CommandDispatcher<CommandSourceStack> $$02) {
        $$02.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debug").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.literal("start").executes($$0 -> DebugCommand.start((CommandSourceStack)$$0.getSource())))).then(Commands.literal("stop").executes($$0 -> DebugCommand.stop((CommandSourceStack)$$0.getSource())))).then(((LiteralArgumentBuilder)Commands.literal("function").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))).then(Commands.argument("name", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).executes((Command)new TraceCustomExecutor()))));
    }

    private static int start(CommandSourceStack $$0) throws CommandSyntaxException {
        MinecraftServer $$1 = $$0.getServer();
        if ($$1.isTimeProfilerRunning()) {
            throw ERROR_ALREADY_RUNNING.create();
        }
        $$1.startTimeProfiler();
        $$0.sendSuccess(() -> Component.translatable("commands.debug.started"), true);
        return 0;
    }

    private static int stop(CommandSourceStack $$0) throws CommandSyntaxException {
        MinecraftServer $$1 = $$0.getServer();
        if (!$$1.isTimeProfilerRunning()) {
            throw ERROR_NOT_RUNNING.create();
        }
        ProfileResults $$2 = $$1.stopTimeProfiler();
        double $$3 = (double)$$2.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
        double $$4 = (double)$$2.getTickDuration() / $$3;
        $$0.sendSuccess(() -> Component.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", $$3), $$2.getTickDuration(), String.format(Locale.ROOT, "%.2f", $$4)), true);
        return (int)$$4;
    }

    static class TraceCustomExecutor
    extends CustomCommandExecutor.WithErrorHandling<CommandSourceStack>
    implements CustomCommandExecutor.CommandAdapter<CommandSourceStack> {
        TraceCustomExecutor() {
        }

        @Override
        public void runGuarded(CommandSourceStack $$0, ContextChain<CommandSourceStack> $$1, ChainModifiers $$2, ExecutionControl<CommandSourceStack> $$3) throws CommandSyntaxException {
            if ($$2.isReturn()) {
                throw NO_RETURN_RUN.create();
            }
            if ($$3.tracer() != null) {
                throw NO_RECURSIVE_TRACES.create();
            }
            CommandContext $$42 = $$1.getTopContext();
            Collection<CommandFunction<CommandSourceStack>> $$52 = FunctionArgument.getFunctions((CommandContext<CommandSourceStack>)$$42, "name");
            MinecraftServer $$6 = $$0.getServer();
            String $$7 = "debug-trace-" + Util.getFilenameFormattedDateTime() + ".txt";
            CommandDispatcher<CommandSourceStack> $$8 = $$0.getServer().getFunctions().getDispatcher();
            int $$9 = 0;
            try {
                Path $$10 = $$6.getFile("debug");
                Files.createDirectories($$10, new FileAttribute[0]);
                final PrintWriter $$11 = new PrintWriter(Files.newBufferedWriter($$10.resolve($$7), StandardCharsets.UTF_8, new OpenOption[0]));
                Tracer $$12 = new Tracer($$11);
                $$3.tracer($$12);
                for (final CommandFunction<CommandSourceStack> $$13 : $$52) {
                    try {
                        CommandSourceStack $$14 = $$0.withSource($$12).withMaximumPermission(LevelBasedPermissionSet.GAMEMASTER);
                        InstantiatedFunction<CommandSourceStack> $$15 = $$13.instantiate(null, $$8);
                        $$3.queueNext(new CallFunction<CommandSourceStack>(this, $$15, CommandResultCallback.EMPTY, false){

                            @Override
                            public void execute(CommandSourceStack $$0, ExecutionContext<CommandSourceStack> $$1, Frame $$2) {
                                $$11.println($$13.id());
                                super.execute($$0, $$1, $$2);
                            }

                            @Override
                            public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
                                this.execute((CommandSourceStack)object, (ExecutionContext<CommandSourceStack>)executionContext, frame);
                            }
                        }.bind($$14));
                        $$9 += $$15.entries().size();
                    }
                    catch (FunctionInstantiationException $$16) {
                        $$0.sendFailure($$16.messageComponent());
                    }
                }
            }
            catch (IOException | UncheckedIOException $$17) {
                LOGGER.warn("Tracing failed", (Throwable)$$17);
                $$0.sendFailure(Component.translatable("commands.debug.function.traceFailed"));
            }
            int $$18 = $$9;
            $$3.queueNext(($$4, $$5) -> {
                if ($$52.size() == 1) {
                    $$0.sendSuccess(() -> Component.translatable("commands.debug.function.success.single", $$18, Component.translationArg(((CommandFunction)$$52.iterator().next()).id()), $$7), true);
                } else {
                    $$0.sendSuccess(() -> Component.translatable("commands.debug.function.success.multiple", $$18, $$52.size(), $$7), true);
                }
            });
        }

        @Override
        public /* synthetic */ void runGuarded(ExecutionCommandSource executionCommandSource, ContextChain contextChain, ChainModifiers chainModifiers, ExecutionControl executionControl) throws CommandSyntaxException {
            this.runGuarded((CommandSourceStack)executionCommandSource, (ContextChain<CommandSourceStack>)contextChain, chainModifiers, (ExecutionControl<CommandSourceStack>)executionControl);
        }
    }

    static class Tracer
    implements CommandSource,
    TraceCallbacks {
        public static final int INDENT_OFFSET = 1;
        private final PrintWriter output;
        private int lastIndent;
        private boolean waitingForResult;

        Tracer(PrintWriter $$0) {
            this.output = $$0;
        }

        private void indentAndSave(int $$0) {
            this.printIndent($$0);
            this.lastIndent = $$0;
        }

        private void printIndent(int $$0) {
            for (int $$1 = 0; $$1 < $$0 + 1; ++$$1) {
                this.output.write("    ");
            }
        }

        private void newLine() {
            if (this.waitingForResult) {
                this.output.println();
                this.waitingForResult = false;
            }
        }

        @Override
        public void onCommand(int $$0, String $$1) {
            this.newLine();
            this.indentAndSave($$0);
            this.output.print("[C] ");
            this.output.print($$1);
            this.waitingForResult = true;
        }

        @Override
        public void onReturn(int $$0, String $$1, int $$2) {
            if (this.waitingForResult) {
                this.output.print(" -> ");
                this.output.println($$2);
                this.waitingForResult = false;
            } else {
                this.indentAndSave($$0);
                this.output.print("[R = ");
                this.output.print($$2);
                this.output.print("] ");
                this.output.println($$1);
            }
        }

        @Override
        public void onCall(int $$0, Identifier $$1, int $$2) {
            this.newLine();
            this.indentAndSave($$0);
            this.output.print("[F] ");
            this.output.print($$1);
            this.output.print(" size=");
            this.output.println($$2);
        }

        @Override
        public void onError(String $$0) {
            this.newLine();
            this.indentAndSave(this.lastIndent + 1);
            this.output.print("[E] ");
            this.output.print($$0);
        }

        @Override
        public void sendSystemMessage(Component $$0) {
            this.newLine();
            this.printIndent(this.lastIndent + 1);
            this.output.print("[M] ");
            this.output.println($$0.getString());
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        @Override
        public boolean alwaysAccepts() {
            return true;
        }

        @Override
        public void close() {
            IOUtils.closeQuietly((Writer)this.output);
        }
    }
}

