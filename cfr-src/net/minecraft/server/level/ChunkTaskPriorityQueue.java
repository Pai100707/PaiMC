/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.level;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public class ChunkTaskPriorityQueue {
    public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
    private final List<Long2ObjectLinkedOpenHashMap<List<Runnable>>> queuesPerPriority = IntStream.range(0, PRIORITY_LEVEL_COUNT).mapToObj($$0 -> new Long2ObjectLinkedOpenHashMap()).toList();
    private volatile int topPriorityQueueIndex = PRIORITY_LEVEL_COUNT;
    private final String name;

    public ChunkTaskPriorityQueue(String $$02) {
        this.name = $$02;
    }

    protected void resortChunkTasks(int $$02, ChunkPos $$1, int $$2) {
        if ($$02 >= PRIORITY_LEVEL_COUNT) {
            return;
        }
        Long2ObjectLinkedOpenHashMap<List<Runnable>> $$3 = this.queuesPerPriority.get($$02);
        List $$4 = (List)$$3.remove($$1.toLong());
        if ($$02 == this.topPriorityQueueIndex) {
            while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
                ++this.topPriorityQueueIndex;
            }
        }
        if ($$4 != null && !$$4.isEmpty()) {
            ((List)this.queuesPerPriority.get($$2).computeIfAbsent($$1.toLong(), $$0 -> Lists.newArrayList())).addAll($$4);
            this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, $$2);
        }
    }

    protected void submit(Runnable $$02, long $$1, int $$2) {
        ((List)this.queuesPerPriority.get($$2).computeIfAbsent($$1, $$0 -> Lists.newArrayList())).add($$02);
        this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, $$2);
    }

    protected void release(long $$0, boolean $$1) {
        for (Long2ObjectLinkedOpenHashMap<List<Runnable>> $$2 : this.queuesPerPriority) {
            List $$3 = (List)$$2.get($$0);
            if ($$3 == null) continue;
            if ($$1) {
                $$3.clear();
            }
            if (!$$3.isEmpty()) continue;
            $$2.remove($$0);
        }
        while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
            ++this.topPriorityQueueIndex;
        }
    }

    public @Nullable TasksForChunk pop() {
        if (!this.hasWork()) {
            return null;
        }
        int $$0 = this.topPriorityQueueIndex;
        Long2ObjectLinkedOpenHashMap<List<Runnable>> $$1 = this.queuesPerPriority.get($$0);
        long $$2 = $$1.firstLongKey();
        List $$3 = (List)$$1.removeFirst();
        while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
            ++this.topPriorityQueueIndex;
        }
        return new TasksForChunk($$2, $$3);
    }

    public boolean hasWork() {
        return this.topPriorityQueueIndex < PRIORITY_LEVEL_COUNT;
    }

    public String toString() {
        return this.name + " " + this.topPriorityQueueIndex + "...";
    }

    public record TasksForChunk(long chunkPos, List<Runnable> tasks) {
    }
}

