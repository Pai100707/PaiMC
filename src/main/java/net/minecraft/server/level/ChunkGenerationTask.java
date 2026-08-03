package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
   @Nullable
   private ChunkStatus scheduledStatus = null;
   public final ChunkStatus targetStatus;
   private volatile boolean markedForCancellation;
   private final List<CompletableFuture<ChunkResult<ChunkAccess>>> scheduledLayer = new ArrayList<>();
   private final StaticCache2D<GenerationChunkHolder> cache;
   private boolean needsGeneration;

   private ChunkGenerationTask(GeneratingChunkMap $$0, ChunkStatus $$1, ChunkPos $$2, StaticCache2D<GenerationChunkHolder> $$3) {
      this.chunkMap = $$0;
      this.targetStatus = $$1;
      this.pos = $$2;
      this.cache = $$3;
   }

   public static ChunkGenerationTask create(GeneratingChunkMap $$0, ChunkStatus $$1, ChunkPos $$2) {
      int $$3 = ChunkPyramid.GENERATION_PYRAMID.getStepTo($$1).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
      StaticCache2D<GenerationChunkHolder> $$4 = StaticCache2D.create($$2.x, $$2.z, $$3, ($$1x, $$2x) -> $$0.acquireGeneration(ChunkPos.asLong($$1x, $$2x)));
      return new ChunkGenerationTask($$0, $$1, $$2, $$4);
   }

   @Nullable
   public CompletableFuture<?> runUntilWait() {
      while (true) {
         CompletableFuture<?> $$0 = this.waitForScheduledLayer();
         if ($$0 != null) {
            return $$0;
         }

         if (this.markedForCancellation || this.scheduledStatus == this.targetStatus) {
            this.releaseClaim();
            return null;
         }

         this.scheduleNextLayer();
      }
   }

   private void scheduleNextLayer() {
      ChunkStatus $$0;
      if (this.scheduledStatus == null) {
         $$0 = ChunkStatus.EMPTY;
      } else if (!this.needsGeneration && this.scheduledStatus == ChunkStatus.EMPTY && !this.canLoadWithoutGeneration()) {
         this.needsGeneration = true;
         $$0 = ChunkStatus.EMPTY;
      } else {
         $$0 = (ChunkStatus)ChunkStatus.getStatusList().get(this.scheduledStatus.getIndex() + 1);
      }

      this.scheduleLayer($$0, this.needsGeneration);
      this.scheduledStatus = $$0;
   }

   public void markForCancellation() {
      this.markedForCancellation = true;
   }

   private void releaseClaim() {
      GenerationChunkHolder $$0 = (GenerationChunkHolder)this.cache.get(this.pos.x, this.pos.z);
      $$0.removeTask(this);
      this.cache.forEach(this.chunkMap::releaseGeneration);
   }

   private boolean canLoadWithoutGeneration() {
      if (this.targetStatus == ChunkStatus.EMPTY) {
         return true;
      } else {
         ChunkStatus $$0 = ((GenerationChunkHolder)this.cache.get(this.pos.x, this.pos.z)).getPersistedStatus();
         if ($$0 != null && !$$0.isBefore(this.targetStatus)) {
            ChunkDependencies $$1 = ChunkPyramid.LOADING_PYRAMID.getStepTo(this.targetStatus).accumulatedDependencies();
            int $$2 = $$1.getRadius();

            for (int $$3 = this.pos.x - $$2; $$3 <= this.pos.x + $$2; $$3++) {
               for (int $$4 = this.pos.z - $$2; $$4 <= this.pos.z + $$2; $$4++) {
                  int $$5 = this.pos.getChessboardDistance($$3, $$4);
                  ChunkStatus $$6 = $$1.get($$5);
                  ChunkStatus $$7 = ((GenerationChunkHolder)this.cache.get($$3, $$4)).getPersistedStatus();
                  if ($$7 == null || $$7.isBefore($$6)) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public GenerationChunkHolder getCenter() {
      return (GenerationChunkHolder)this.cache.get(this.pos.x, this.pos.z);
   }

   private void scheduleLayer(ChunkStatus $$0, boolean $$1) {
      Zone $$2 = Profiler.get().zone("scheduleLayer");

      label59: {
         try {
            $$2.addText($$0::getName);
            int $$3 = this.getRadiusForLayer($$0, $$1);

            for (int $$4 = this.pos.x - $$3; $$4 <= this.pos.x + $$3; $$4++) {
               for (int $$5 = this.pos.z - $$3; $$5 <= this.pos.z + $$3; $$5++) {
                  GenerationChunkHolder $$6 = (GenerationChunkHolder)this.cache.get($$4, $$5);
                  if (this.markedForCancellation || !this.scheduleChunkInLayer($$0, $$1, $$6)) {
                     break label59;
                  }
               }
            }
         } catch (Throwable var9) {
            if ($$2 != null) {
               try {
                  $$2.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if ($$2 != null) {
            $$2.close();
         }

         return;
      }

      if ($$2 != null) {
         $$2.close();
      }
   }

   private int getRadiusForLayer(ChunkStatus $$0, boolean $$1) {
      ChunkPyramid $$2 = $$1 ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
      return $$2.getStepTo(this.targetStatus).getAccumulatedRadiusOf($$0);
   }

   private boolean scheduleChunkInLayer(ChunkStatus $$0, boolean $$1, GenerationChunkHolder $$2) {
      ChunkStatus $$3 = $$2.getPersistedStatus();
      boolean $$4 = $$3 != null && $$0.isAfter($$3);
      ChunkPyramid $$5 = $$4 ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
      if ($$4 && !$$1) {
         throw new IllegalStateException("Can't load chunk, but didn't expect to need to generate");
      } else {
         CompletableFuture<ChunkResult<ChunkAccess>> $$6 = $$2.applyStep($$5.getStepTo($$0), this.chunkMap, this.cache);
         ChunkResult<ChunkAccess> $$7 = $$6.getNow(null);
         if ($$7 == null) {
            this.scheduledLayer.add($$6);
            return true;
         } else if ($$7.isSuccess()) {
            return true;
         } else {
            this.markForCancellation();
            return false;
         }
      }
   }

   @Nullable
   private CompletableFuture<?> waitForScheduledLayer() {
      while (!this.scheduledLayer.isEmpty()) {
         CompletableFuture<ChunkResult<ChunkAccess>> $$0 = this.scheduledLayer.getLast();
         ChunkResult<ChunkAccess> $$1 = $$0.getNow(null);
         if ($$1 == null) {
            return $$0;
         }

         this.scheduledLayer.removeLast();
         if (!$$1.isSuccess()) {
            this.markForCancellation();
         }
      }

      return null;
   }
}
