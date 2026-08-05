/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.GeneratingChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;

public class ChunkGenerationTask {
    private final GeneratingChunkMap chunkMap;
    private final ChunkPos pos;
    private @Nullable ChunkStatus scheduledStatus = null;
    public final ChunkStatus targetStatus;
    private volatile boolean markedForCancellation;
    private final List<CompletableFuture<ChunkResult<ChunkAccess>>> scheduledLayer = new ArrayList<CompletableFuture<ChunkResult<ChunkAccess>>>();
    private final StaticCache2D<GenerationChunkHolder> cache;
    private boolean needsGeneration;

    private ChunkGenerationTask(GeneratingChunkMap $$0, ChunkStatus $$1, ChunkPos $$2, StaticCache2D<GenerationChunkHolder> $$3) {
        this.chunkMap = $$0;
        this.targetStatus = $$1;
        this.pos = $$2;
        this.cache = $$3;
    }

    public static ChunkGenerationTask create(GeneratingChunkMap $$0, ChunkStatus $$12, ChunkPos $$22) {
        int $$3 = ChunkPyramid.GENERATION_PYRAMID.getStepTo($$12).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
        StaticCache2D<GenerationChunkHolder> $$4 = StaticCache2D.create($$22.x, $$22.z, $$3, ($$1, $$2) -> $$0.acquireGeneration(ChunkPos.asLong($$1, $$2)));
        return new ChunkGenerationTask($$0, $$12, $$22, $$4);
    }

    public @Nullable CompletableFuture<?> runUntilWait() {
        CompletableFuture<?> $$0;
        while (($$0 = this.waitForScheduledLayer()) == null) {
            if (this.markedForCancellation || this.scheduledStatus == this.targetStatus) {
                this.releaseClaim();
                return null;
            }
            this.scheduleNextLayer();
        }
        return $$0;
    }

    private void scheduleNextLayer() {
        ChunkStatus $$2;
        if (this.scheduledStatus == null) {
            ChunkStatus $$0 = ChunkStatus.EMPTY;
        } else if (!this.needsGeneration && this.scheduledStatus == ChunkStatus.EMPTY && !this.canLoadWithoutGeneration()) {
            this.needsGeneration = true;
            ChunkStatus $$1 = ChunkStatus.EMPTY;
        } else {
            $$2 = ChunkStatus.getStatusList().get(this.scheduledStatus.getIndex() + 1);
        }
        this.scheduleLayer($$2, this.needsGeneration);
        this.scheduledStatus = $$2;
    }

    public void markForCancellation() {
        this.markedForCancellation = true;
    }

    private void releaseClaim() {
        GenerationChunkHolder $$0 = this.cache.get(this.pos.x, this.pos.z);
        $$0.removeTask(this);
        this.cache.forEach(this.chunkMap::releaseGeneration);
    }

    private boolean canLoadWithoutGeneration() {
        if (this.targetStatus == ChunkStatus.EMPTY) {
            return true;
        }
        ChunkStatus $$0 = this.cache.get(this.pos.x, this.pos.z).getPersistedStatus();
        if ($$0 == null || $$0.isBefore(this.targetStatus)) {
            return false;
        }
        ChunkDependencies $$1 = ChunkPyramid.LOADING_PYRAMID.getStepTo(this.targetStatus).accumulatedDependencies();
        int $$2 = $$1.getRadius();
        for (int $$3 = this.pos.x - $$2; $$3 <= this.pos.x + $$2; ++$$3) {
            for (int $$4 = this.pos.z - $$2; $$4 <= this.pos.z + $$2; ++$$4) {
                int $$5 = this.pos.getChessboardDistance($$3, $$4);
                ChunkStatus $$6 = $$1.get($$5);
                ChunkStatus $$7 = this.cache.get($$3, $$4).getPersistedStatus();
                if ($$7 != null && !$$7.isBefore($$6)) continue;
                return false;
            }
        }
        return true;
    }

    public GenerationChunkHolder getCenter() {
        return this.cache.get(this.pos.x, this.pos.z);
    }

    private void scheduleLayer(ChunkStatus $$0, boolean $$1) {
        try (Zone $$2 = Profiler.get().zone("scheduleLayer");){
            $$2.addText($$0::getName);
            int $$3 = this.getRadiusForLayer($$0, $$1);
            for (int $$4 = this.pos.x - $$3; $$4 <= this.pos.x + $$3; ++$$4) {
                for (int $$5 = this.pos.z - $$3; $$5 <= this.pos.z + $$3; ++$$5) {
                    GenerationChunkHolder $$6 = this.cache.get($$4, $$5);
                    if (!this.markedForCancellation && this.scheduleChunkInLayer($$0, $$1, $$6)) continue;
                    return;
                }
            }
        }
    }

    private int getRadiusForLayer(ChunkStatus $$0, boolean $$1) {
        ChunkPyramid $$2 = $$1 ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
        return $$2.getStepTo(this.targetStatus).getAccumulatedRadiusOf($$0);
    }

    private boolean scheduleChunkInLayer(ChunkStatus $$0, boolean $$1, GenerationChunkHolder $$2) {
        ChunkPyramid $$5;
        ChunkStatus $$3 = $$2.getPersistedStatus();
        boolean $$4 = $$3 != null && $$0.isAfter($$3);
        ChunkPyramid chunkPyramid = $$5 = $$4 ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
        if ($$4 && !$$1) {
            throw new IllegalStateException("Can't load chunk, but didn't expect to need to generate");
        }
        CompletableFuture<ChunkResult<ChunkAccess>> $$6 = $$2.applyStep($$5.getStepTo($$0), this.chunkMap, this.cache);
        ChunkResult $$7 = $$6.getNow(null);
        if ($$7 == null) {
            this.scheduledLayer.add($$6);
            return true;
        }
        if ($$7.isSuccess()) {
            return true;
        }
        this.markForCancellation();
        return false;
    }

    private @Nullable CompletableFuture<?> waitForScheduledLayer() {
        while (!this.scheduledLayer.isEmpty()) {
            CompletableFuture<ChunkResult<ChunkAccess>> $$0 = this.scheduledLayer.getLast();
            ChunkResult $$1 = $$0.getNow(null);
            if ($$1 == null) {
                return $$0;
            }
            this.scheduledLayer.removeLast();
            if ($$1.isSuccess()) continue;
            this.markForCancellation();
        }
        return null;
    }
}

