/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.RedirectModifier
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.context.ContextChain$Stage
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.ContinuationTask;
import net.minecraft.commands.execution.tasks.ExecuteCommand;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.network.chat.Component;

public class BuildContexts<T extends ExecutionCommandSource<T>> {
    @VisibleForTesting
    public static final DynamicCommandExceptionType ERROR_FORK_LIMIT_REACHED = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("command.forkLimit", $$0));
    private final String commandInput;
    private final ContextChain<T> command;

    public BuildContexts(String $$0, ContextChain<T> $$1) {
        this.commandInput = $$0;
        this.command = $$1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void execute(T $$02, List<T> $$12, ExecutionContext<T> $$22, Frame $$3, ChainModifiers $$4) {
        ContextChain $$5 = this.command;
        ChainModifiers $$6 = $$4;
        List<Object> $$7 = $$12;
        if ($$5.getStage() != ContextChain.Stage.EXECUTE) {
            $$22.profiler().push(() -> "prepare " + this.commandInput);
            try {
                int $$8 = $$22.forkLimit();
                while ($$5.getStage() != ContextChain.Stage.EXECUTE) {
                    RedirectModifier $$10;
                    CommandContext $$9 = $$5.getTopContext();
                    if ($$9.isForked()) {
                        $$6 = $$6.setForked();
                    }
                    if (($$10 = $$9.getRedirectModifier()) instanceof CustomModifierExecutor) {
                        CustomModifierExecutor $$11 = (CustomModifierExecutor)$$10;
                        $$11.apply($$02, $$7, $$5, $$6, ExecutionControl.create($$22, $$3));
                        return;
                    }
                    if ($$10 != null) {
                        $$22.incrementCost();
                        boolean $$122 = $$6.isForked();
                        ObjectArrayList $$13 = new ObjectArrayList();
                        for (ExecutionCommandSource executionCommandSource : $$7) {
                            Collection $$15;
                            block21: {
                                try {
                                    $$15 = ContextChain.runModifier((CommandContext)$$9, (Object)executionCommandSource, ($$0, $$1, $$2) -> {}, (boolean)$$122);
                                    if ($$13.size() + $$15.size() < $$8) break block21;
                                    $$02.handleError(ERROR_FORK_LIMIT_REACHED.create((Object)$$8), $$122, $$22.tracer());
                                    return;
                                }
                                catch (CommandSyntaxException $$16) {
                                    executionCommandSource.handleError($$16, $$122, $$22.tracer());
                                    if ($$122) continue;
                                    $$22.profiler().pop();
                                    return;
                                }
                            }
                            $$13.addAll($$15);
                        }
                        $$7 = $$13;
                    }
                    $$5 = $$5.nextStage();
                }
            }
            finally {
                $$22.profiler().pop();
            }
        }
        if ($$7.isEmpty()) {
            if ($$6.isReturn()) {
                $$22.queueNext(new CommandQueueEntry($$3, FallthroughTask.instance()));
            }
            return;
        }
        CommandContext $$17 = $$5.getTopContext();
        Command $$18 = $$17.getCommand();
        if ($$18 instanceof CustomCommandExecutor) {
            CustomCommandExecutor $$19 = (CustomCommandExecutor)$$18;
            ExecutionControl $$20 = ExecutionControl.create($$22, $$3);
            for (ExecutionCommandSource executionCommandSource : $$7) {
                $$19.run(executionCommandSource, $$5, $$6, $$20);
            }
        } else {
            if ($$6.isReturn()) {
                ExecutionCommandSource $$222 = (ExecutionCommandSource)$$7.get(0);
                $$222 = $$222.withCallback(CommandResultCallback.chain($$222.callback(), $$3.returnValueConsumer()));
                $$7 = List.of($$222);
            }
            ExecuteCommand $$23 = new ExecuteCommand(this.commandInput, $$6, $$17);
            ContinuationTask.schedule($$22, $$3, $$7, ($$1, $$2) -> new CommandQueueEntry<ExecutionCommandSource>($$1, $$23.bind($$2)));
        }
    }

    protected void traceCommandStart(ExecutionContext<T> $$0, Frame $$1) {
        TraceCallbacks $$2 = $$0.tracer();
        if ($$2 != null) {
            $$2.onCommand($$1.depth(), this.commandInput);
        }
    }

    public String toString() {
        return this.commandInput;
    }

    public static class TopLevel<T extends ExecutionCommandSource<T>>
    extends BuildContexts<T>
    implements EntryAction<T> {
        private final T source;

        public TopLevel(String $$0, ContextChain<T> $$1, T $$2) {
            super($$0, $$1);
            this.source = $$2;
        }

        @Override
        public void execute(ExecutionContext<T> $$0, Frame $$1) {
            this.traceCommandStart($$0, $$1);
            this.execute(this.source, List.of(this.source), $$0, $$1, ChainModifiers.DEFAULT);
        }
    }

    public static class Continuation<T extends ExecutionCommandSource<T>>
    extends BuildContexts<T>
    implements EntryAction<T> {
        private final ChainModifiers modifiers;
        private final T originalSource;
        private final List<T> sources;

        public Continuation(String $$0, ContextChain<T> $$1, ChainModifiers $$2, T $$3, List<T> $$4) {
            super($$0, $$1);
            this.originalSource = $$3;
            this.sources = $$4;
            this.modifiers = $$2;
        }

        @Override
        public void execute(ExecutionContext<T> $$0, Frame $$1) {
            this.execute(this.originalSource, this.sources, $$0, $$1, this.modifiers);
        }
    }

    public static class Unbound<T extends ExecutionCommandSource<T>>
    extends BuildContexts<T>
    implements UnboundEntryAction<T> {
        public Unbound(String $$0, ContextChain<T> $$1) {
            super($$0, $$1);
        }

        @Override
        public void execute(T $$0, ExecutionContext<T> $$1, Frame $$2) {
            this.traceCommandStart($$1, $$2);
            this.execute($$0, List.of($$0), $$1, $$2, ChainModifiers.DEFAULT);
        }

        @Override
        public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
            this.execute((ExecutionCommandSource)object, executionContext, frame);
        }
    }
}

