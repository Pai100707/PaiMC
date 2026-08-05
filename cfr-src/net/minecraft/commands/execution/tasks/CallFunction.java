/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.ContinuationTask;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T extends ExecutionCommandSource<T>>
implements UnboundEntryAction<T> {
    private final InstantiatedFunction<T> function;
    private final CommandResultCallback resultCallback;
    private final boolean returnParentFrame;

    public CallFunction(InstantiatedFunction<T> $$0, CommandResultCallback $$1, boolean $$2) {
        this.function = $$0;
        this.resultCallback = $$1;
        this.returnParentFrame = $$2;
    }

    @Override
    public void execute(T $$0, ExecutionContext<T> $$12, Frame $$22) {
        $$12.incrementCost();
        List<UnboundEntryAction<T>> $$3 = this.function.entries();
        TraceCallbacks $$4 = $$12.tracer();
        if ($$4 != null) {
            $$4.onCall($$22.depth(), this.function.id(), this.function.entries().size());
        }
        int $$5 = $$22.depth() + 1;
        Frame.FrameControl $$6 = this.returnParentFrame ? $$22.frameControl() : $$12.frameControlForDepth($$5);
        Frame $$7 = new Frame($$5, this.resultCallback, $$6);
        ContinuationTask.schedule($$12, $$7, $$3, ($$1, $$2) -> new CommandQueueEntry<ExecutionCommandSource>($$1, $$2.bind($$0)));
    }

    @Override
    public /* synthetic */ void execute(Object object, ExecutionContext executionContext, Frame frame) {
        this.execute((ExecutionCommandSource)object, executionContext, frame);
    }
}

