/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.Queues
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Deque;
import org.jspecify.annotations.Nullable;

public final class SequencedPriorityIterator<T>
extends AbstractIterator<T> {
    private static final int MIN_PRIO = Integer.MIN_VALUE;
    private @Nullable Deque<T> highestPrioQueue = null;
    private int highestPrio = Integer.MIN_VALUE;
    private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap();

    public void add(T $$02, int $$1) {
        if ($$1 == this.highestPrio && this.highestPrioQueue != null) {
            this.highestPrioQueue.addLast($$02);
            return;
        }
        Deque $$2 = (Deque)this.queuesByPriority.computeIfAbsent($$1, $$0 -> Queues.newArrayDeque());
        $$2.addLast($$02);
        if ($$1 >= this.highestPrio) {
            this.highestPrioQueue = $$2;
            this.highestPrio = $$1;
        }
    }

    protected @Nullable T computeNext() {
        if (this.highestPrioQueue == null) {
            return (T)this.endOfData();
        }
        T $$0 = this.highestPrioQueue.removeFirst();
        if ($$0 == null) {
            return (T)this.endOfData();
        }
        if (this.highestPrioQueue.isEmpty()) {
            this.switchCacheToNextHighestPrioQueue();
        }
        return $$0;
    }

    private void switchCacheToNextHighestPrioQueue() {
        int $$0 = Integer.MIN_VALUE;
        Deque $$1 = null;
        for (Int2ObjectMap.Entry $$2 : Int2ObjectMaps.fastIterable(this.queuesByPriority)) {
            Deque $$3 = (Deque)$$2.getValue();
            int $$4 = $$2.getIntKey();
            if ($$4 <= $$0 || $$3.isEmpty()) continue;
            $$0 = $$4;
            $$1 = $$3;
            if ($$4 != this.highestPrio - 1) continue;
            break;
        }
        this.highestPrio = $$0;
        this.highestPrioQueue = $$1;
    }
}

