/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

public class ContinuationTask<T, P>
implements EntryAction<T> {
    private final TaskProvider<T, P> taskFactory;
    private final List<P> arguments;
    private final CommandQueueEntry<T> selfEntry;
    private int index;

    private ContinuationTask(TaskProvider<T, P> $$0, List<P> $$1, Frame $$2) {
        this.taskFactory = $$0;
        this.arguments = $$1;
        this.selfEntry = new CommandQueueEntry($$2, this);
    }

    @Override
    public void execute(ExecutionContext<T> $$0, Frame $$1) {
        P $$2 = this.arguments.get(this.index);
        $$0.queueNext(this.taskFactory.create($$1, $$2));
        if (++this.index < this.arguments.size()) {
            $$0.queueNext(this.selfEntry);
        }
    }

    public static <T, P> void schedule(ExecutionContext<T> $$0, Frame $$1, List<P> $$2, TaskProvider<T, P> $$3) {
        int $$4 = $$2.size();
        switch ($$4) {
            case 0: {
                break;
            }
            case 1: {
                $$0.queueNext($$3.create($$1, $$2.get(0)));
                break;
            }
            case 2: {
                $$0.queueNext($$3.create($$1, $$2.get(0)));
                $$0.queueNext($$3.create($$1, $$2.get(1)));
                break;
            }
            default: {
                $$0.queueNext(new ContinuationTask<T, P>($$3, $$2, (Frame)$$1).selfEntry);
            }
        }
    }

    @FunctionalInterface
    public static interface TaskProvider<T, P> {
        public CommandQueueEntry<T> create(Frame var1, P var2);
    }
}

