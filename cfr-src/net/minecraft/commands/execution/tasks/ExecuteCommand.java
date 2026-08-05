/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;

public class ExecuteCommand<T extends ExecutionCommandSource<T>>
implements UnboundEntryAction<T> {
    private final String commandInput;
    private final ChainModifiers modifiers;
    private final CommandContext<T> executionContext;

    public ExecuteCommand(String $$0, ChainModifiers $$1, CommandContext<T> $$2) {
        this.commandInput = $$0;
        this.modifiers = $$1;
        this.executionContext = $$2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute(T $$0, ExecutionContext<T> $$1, Frame $$2) {
        $$1.profiler().push(() -> "execute " + this.commandInput);
        try {
            $$1.incrementCost();
            int $$3 = ContextChain.runExecutable(this.executionContext, $$0, ExecutionCommandSource.resultConsumer(), (boolean)this.modifiers.isForked());
            TraceCallbacks $$4 = $$1.tracer();
            if ($$4 != null) {
                $$4.onReturn($$2.depth(), this.commandInput, $$3);
            }
        }
        catch (CommandSyntaxException $$5) {
            $$0.handleError($$5, this.modifiers.isForked(), $$1.tracer());
        }
        finally {
            $$1.profiler().pop();
        }
    }

    @Override
    public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
        this.execute((ExecutionCommandSource)object, executionContext, frame);
    }
}

