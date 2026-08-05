/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkLoadCounter {
    private final List<ChunkHolder> pendingChunks = new ArrayList<ChunkHolder>();
    private int totalChunks;

    public void track(ServerLevel $$0, Runnable $$1) {
        ServerChunkCache $$2 = $$0.getChunkSource();
        LongOpenHashSet $$3 = new LongOpenHashSet();
        $$2.runDistanceManagerUpdates();
        $$2.chunkMap.allChunksWithAtLeastStatus(ChunkStatus.FULL).forEach(arg_0 -> ChunkLoadCounter.lambda$track$0((LongSet)$$3, arg_0));
        $$1.run();
        $$2.runDistanceManagerUpdates();
        $$2.chunkMap.allChunksWithAtLeastStatus(ChunkStatus.FULL).forEach(arg_0 -> this.lambda$track$1((LongSet)$$3, arg_0));
    }

    public int readyChunks() {
        return this.totalChunks - this.pendingChunks();
    }

    public int pendingChunks() {
        this.pendingChunks.removeIf($$0 -> $$0.getLatestStatus() == ChunkStatus.FULL);
        return this.pendingChunks.size();
    }

    public int totalChunks() {
        return this.totalChunks;
    }

    private /* synthetic */ void lambda$track$1(LongSet $$0, ChunkHolder $$1) {
        if (!$$0.contains($$1.getPos().toLong())) {
            this.pendingChunks.add($$1);
            ++this.totalChunks;
        }
    }

    private static /* synthetic */ void lambda$track$0(LongSet $$0, ChunkHolder $$1) {
        $$0.add($$1.getPos().toLong());
    }
}

