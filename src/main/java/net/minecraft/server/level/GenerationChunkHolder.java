package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.jspecify.annotations.Nullable;

public abstract class GenerationChunkHolder {
   private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
   private static final ChunkResult<ChunkAccess> NOT_DONE_YET = ChunkResult.error("Not done yet");
   public static final ChunkResult<ChunkAccess> UNLOADED_CHUNK = ChunkResult.error("Unloaded chunk");
   public static final CompletableFuture<ChunkResult<ChunkAccess>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
   protected final ChunkPos pos;
   @Nullable
   private volatile ChunkStatus highestAllowedStatus;
   private final AtomicReference<ChunkStatus> startedWork = new AtomicReference<>();
   private final AtomicReferenceArray<CompletableFuture<ChunkResult<ChunkAccess>>> futures = new AtomicReferenceArray<>(CHUNK_STATUSES.size());
   private final AtomicReference<ChunkGenerationTask> task = new AtomicReference<>();
   private final AtomicInteger generationRefCount = new AtomicInteger();
   private volatile CompletableFuture<Void> generationSaveSyncFuture = CompletableFuture.completedFuture(null);

   public GenerationChunkHolder(ChunkPos $$0) {
      this.pos = $$0;
      if (!$$0.isValid()) {
         throw new IllegalStateException("Trying to create chunk out of reasonable bounds: " + $$0);
      }
   }

   public CompletableFuture<ChunkResult<ChunkAccess>> scheduleChunkGenerationTask(ChunkStatus $$0, ChunkMap $$1) {
      if (this.isStatusDisallowed($$0)) {
         return UNLOADED_CHUNK_FUTURE;
      } else {
         CompletableFuture<ChunkResult<ChunkAccess>> $$2 = this.getOrCreateFuture($$0);
         if ($$2.isDone()) {
            return $$2;
         } else {
            ChunkGenerationTask $$3 = this.task.get();
            if ($$3 == null || $$0.isAfter($$3.targetStatus)) {
               this.rescheduleChunkTask($$1, $$0);
            }

            return $$2;
         }
      }
   }

   CompletableFuture<ChunkResult<ChunkAccess>> applyStep(ChunkStep $$0, GeneratingChunkMap $$1, StaticCache2D<GenerationChunkHolder> $$2) {
      if (this.isStatusDisallowed($$0.targetStatus())) {
         return UNLOADED_CHUNK_FUTURE;
      } else {
         return this.acquireStatusBump($$0.targetStatus()) ? $$1.applyStep(this, $$0, $$2).handle(($$1x, $$2x) -> {
            if ($$2x != null) {
               CrashReport $$3 = CrashReport.forThrowable($$2x, "Exception chunk generation/loading");
               net.minecraft.server.MinecraftServer.setFatalException(new ReportedException($$3));
            } else {
               this.completeFuture($$0.targetStatus(), $$1x);
            }

            return ChunkResult.of($$1x);
         }) : this.getOrCreateFuture($$0.targetStatus());
      }
   }

   protected void updateHighestAllowedStatus(ChunkMap $$0) {
      ChunkStatus $$1 = this.highestAllowedStatus;
      ChunkStatus $$2 = ChunkLevel.generationStatus(this.getTicketLevel());
      this.highestAllowedStatus = $$2;
      boolean $$3 = $$1 != null && ($$2 == null || $$2.isBefore($$1));
      if ($$3) {
         this.failAndClearPendingFuturesBetween($$2, $$1);
         if (this.task.get() != null) {
            this.rescheduleChunkTask($$0, this.findHighestStatusWithPendingFuture($$2));
         }
      }
   }

   public void replaceProtoChunk(ImposterProtoChunk $$0) {
      CompletableFuture<ChunkResult<ChunkAccess>> $$1 = CompletableFuture.completedFuture(ChunkResult.of($$0));

      for (int $$2 = 0; $$2 < this.futures.length() - 1; $$2++) {
         CompletableFuture<ChunkResult<ChunkAccess>> $$3 = this.futures.get($$2);
         Objects.requireNonNull($$3);
         ChunkAccess $$4 = $$3.getNow(NOT_DONE_YET).orElse(null);
         if (!($$4 instanceof ProtoChunk)) {
            throw new IllegalStateException("Trying to replace a ProtoChunk, but found " + $$4);
         }

         if (!this.futures.compareAndSet($$2, $$3, $$1)) {
            throw new IllegalStateException("Future changed by other thread while trying to replace it");
         }
      }
   }

   void removeTask(ChunkGenerationTask $$0) {
      this.task.compareAndSet($$0, null);
   }

   private void rescheduleChunkTask(ChunkMap $$0, @Nullable ChunkStatus $$1) {
      ChunkGenerationTask $$2;
      if ($$1 != null) {
         $$2 = $$0.scheduleGenerationTask($$1, this.getPos());
      } else {
         $$2 = null;
      }

      ChunkGenerationTask $$4 = this.task.getAndSet($$2);
      if ($$4 != null) {
         $$4.markForCancellation();
      }
   }

   private CompletableFuture<ChunkResult<ChunkAccess>> getOrCreateFuture(ChunkStatus $$0) {
      if (this.isStatusDisallowed($$0)) {
         return UNLOADED_CHUNK_FUTURE;
      } else {
         int $$1 = $$0.getIndex();
         CompletableFuture<ChunkResult<ChunkAccess>> $$2 = this.futures.get($$1);

         while ($$2 == null) {
            CompletableFuture<ChunkResult<ChunkAccess>> $$3 = new CompletableFuture<>();
            $$2 = this.futures.compareAndExchange($$1, null, $$3);
            if ($$2 == null) {
               if (this.isStatusDisallowed($$0)) {
                  this.failAndClearPendingFuture($$1, $$3);
                  return UNLOADED_CHUNK_FUTURE;
               }

               return $$3;
            }
         }

         return $$2;
      }
   }

   private void failAndClearPendingFuturesBetween(@Nullable ChunkStatus $$0, ChunkStatus $$1) {
      int $$2 = $$0 == null ? 0 : $$0.getIndex() + 1;
      int $$3 = $$1.getIndex();

      for (int $$4 = $$2; $$4 <= $$3; $$4++) {
         CompletableFuture<ChunkResult<ChunkAccess>> $$5 = this.futures.get($$4);
         if ($$5 != null) {
            this.failAndClearPendingFuture($$4, $$5);
         }
      }
   }

   private void failAndClearPendingFuture(int $$0, CompletableFuture<ChunkResult<ChunkAccess>> $$1) {
      if ($$1.complete(UNLOADED_CHUNK) && !this.futures.compareAndSet($$0, $$1, null)) {
         throw new IllegalStateException("Nothing else should replace the future here");
      }
   }

   private void completeFuture(ChunkStatus $$0, ChunkAccess $$1) {
      ChunkResult<ChunkAccess> $$2 = ChunkResult.of($$1);
      int $$3 = $$0.getIndex();

      while (true) {
         CompletableFuture<ChunkResult<ChunkAccess>> $$4 = this.futures.get($$3);
         if ($$4 == null) {
            if (this.futures.compareAndSet($$3, null, CompletableFuture.completedFuture($$2))) {
               return;
            }
         } else {
            if ($$4.complete($$2)) {
               return;
            }

            if ($$4.getNow(NOT_DONE_YET).isSuccess()) {
               throw new IllegalStateException("Trying to complete a future but found it to be completed successfully already");
            }

            Thread.yield();
         }
      }
   }

   @Nullable
   private ChunkStatus findHighestStatusWithPendingFuture(@Nullable ChunkStatus $$0) {
      if ($$0 == null) {
         return null;
      } else {
         ChunkStatus $$1 = $$0;

         for (ChunkStatus $$2 = this.startedWork.get(); $$2 == null || $$1.isAfter($$2); $$1 = $$1.getParent()) {
            if (this.futures.get($$1.getIndex()) != null) {
               return $$1;
            }

            if ($$1 == ChunkStatus.EMPTY) {
               break;
            }
         }

         return null;
      }
   }

   private boolean acquireStatusBump(ChunkStatus $$0) {
      ChunkStatus $$1 = $$0 == ChunkStatus.EMPTY ? null : $$0.getParent();
      ChunkStatus $$2 = this.startedWork.compareAndExchange($$1, $$0);
      if ($$2 == $$1) {
         return true;
      } else if ($$2 != null && !$$0.isAfter($$2)) {
         return false;
      } else {
         throw new IllegalStateException("Unexpected last startedWork status: " + $$2 + " while trying to start: " + $$0);
      }
   }

   private boolean isStatusDisallowed(ChunkStatus $$0) {
      ChunkStatus $$1 = this.highestAllowedStatus;
      return $$1 == null || $$0.isAfter($$1);
   }

   protected abstract void addSaveDependency(CompletableFuture<?> var1);

   public void increaseGenerationRefCount() {
      if (this.generationRefCount.getAndIncrement() == 0) {
         this.generationSaveSyncFuture = new CompletableFuture<>();
         this.addSaveDependency(this.generationSaveSyncFuture);
      }
   }

   public void decreaseGenerationRefCount() {
      CompletableFuture<Void> $$0 = this.generationSaveSyncFuture;
      int $$1 = this.generationRefCount.decrementAndGet();
      if ($$1 == 0) {
         $$0.complete(null);
      }

      if ($$1 < 0) {
         throw new IllegalStateException("More releases than claims. Count: " + $$1);
      }
   }

   @Nullable
   public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus $$0) {
      CompletableFuture<ChunkResult<ChunkAccess>> $$1 = this.futures.get($$0.getIndex());
      return $$1 == null ? null : $$1.getNow(NOT_DONE_YET).orElse(null);
   }

   @Nullable
   public ChunkAccess getChunkIfPresent(ChunkStatus $$0) {
      return this.isStatusDisallowed($$0) ? null : this.getChunkIfPresentUnchecked($$0);
   }

   @Nullable
   public ChunkAccess getLatestChunk() {
      ChunkStatus $$0 = this.startedWork.get();
      if ($$0 == null) {
         return null;
      } else {
         ChunkAccess $$1 = this.getChunkIfPresentUnchecked($$0);
         return $$1 != null ? $$1 : this.getChunkIfPresentUnchecked($$0.getParent());
      }
   }

   @Nullable
   public ChunkStatus getPersistedStatus() {
      CompletableFuture<ChunkResult<ChunkAccess>> $$0 = this.futures.get(ChunkStatus.EMPTY.getIndex());
      ChunkAccess $$1 = $$0 == null ? null : $$0.getNow(NOT_DONE_YET).orElse(null);
      return $$1 == null ? null : $$1.getPersistedStatus();
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public FullChunkStatus getFullStatus() {
      return ChunkLevel.fullStatus(this.getTicketLevel());
   }

   public abstract int getTicketLevel();

   public abstract int getQueueLevel();

   @VisibleForDebug
   public List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> getAllFutures() {
      List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> $$0 = new ArrayList<>();

      for (int $$1 = 0; $$1 < CHUNK_STATUSES.size(); $$1++) {
         $$0.add(Pair.of(CHUNK_STATUSES.get($$1), this.futures.get($$1)));
      }

      return $$0;
   }

   @VisibleForDebug
   @Nullable
   public ChunkStatus getLatestStatus() {
      ChunkStatus $$0 = this.startedWork.get();
      if ($$0 == null) {
         return null;
      } else {
         ChunkAccess $$1 = this.getChunkIfPresentUnchecked($$0);
         return $$1 != null ? $$0 : $$0.getParent();
      }
   }
}
