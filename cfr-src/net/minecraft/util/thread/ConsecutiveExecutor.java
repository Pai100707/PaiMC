/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import net.minecraft.util.thread.AbstractConsecutiveExecutor;
import net.minecraft.util.thread.StrictQueue;

public class ConsecutiveExecutor
extends AbstractConsecutiveExecutor<Runnable> {
    public ConsecutiveExecutor(Executor $$0, String $$1) {
        super(new StrictQueue.QueueStrictQueue(new ConcurrentLinkedQueue<Runnable>()), $$0, $$1);
    }

    @Override
    public Runnable wrapRunnable(Runnable $$0) {
        return $$0;
    }
}

