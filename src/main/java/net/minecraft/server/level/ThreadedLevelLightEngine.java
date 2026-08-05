package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {
   public static final int DEFAULT_BATCH_SIZE = 1000;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ConsecutiveExecutor consecutiveExecutor;
   private final ObjectList<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> lightTasks = new ObjectArrayList();
   private final ChunkMap chunkMap;
   private final ChunkTaskDispatcher taskDispatcher;
   private final int taskPerBatch = 1000;
   private final AtomicBoolean scheduled = new AtomicBoolean();

   public ThreadedLevelLightEngine(LightChunkGetter $$0, ChunkMap $$1, boolean $$2, ConsecutiveExecutor $$3, ChunkTaskDispatcher $$4) {
      super($$0, true, $$2);
      this.chunkMap = $$1;
      this.taskDispatcher = $$4;
      this.consecutiveExecutor = $$3;
   }

   @Override
   public void close() {
   }

   public int runLightUpdates() {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
   }

   public void checkBlock(BlockPos $$0) {
      BlockPos $$1 = $$0.immutable();
      this.addTask(
         SectionPos.blockToSectionCoord($$0.getX()),
         SectionPos.blockToSectionCoord($$0.getZ()),
         ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
         Util.name(() -> super.checkBlock($$1), () -> "checkBlock " + $$1)
      );
   }

   protected void updateChunkStatus(ChunkPos $$0) {
      this.addTask($$0.x, $$0.z, () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.retainData($$0, false);
         super.setLightEnabled($$0, false);

         for (int $$1 = this.getMinLightSection(); $$1 < this.getMaxLightSection(); $$1++) {
            super.queueSectionData(LightLayer.BLOCK, SectionPos.of($$0, $$1), null);
            super.queueSectionData(LightLayer.SKY, SectionPos.of($$0, $$1), null);
         }

         for (int $$2 = this.levelHeightAccessor.getMinSectionY(); $$2 <= this.levelHeightAccessor.getMaxSectionY(); $$2++) {
            super.updateSectionStatus(SectionPos.of($$0, $$2), true);
         }
      }, () -> "updateChunkStatus " + $$0 + " true"));
   }

   public void updateSectionStatus(SectionPos $$0, boolean $$1) {
      this.addTask(
         $$0.x(),
         $$0.z(),
         () -> 0,
         ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
         Util.name(() -> super.updateSectionStatus($$0, $$1), () -> "updateSectionStatus " + $$0 + " " + $$1)
      );
   }

   public void propagateLightSources(ChunkPos $$0) {
      this.addTask($$0.x, $$0.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.propagateLightSources($$0), () -> "propagateLight " + $$0));
   }

   public void setLightEnabled(ChunkPos $$0, boolean $$1) {
      this.addTask(
         $$0.x, $$0.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.setLightEnabled($$0, $$1), () -> "enableLight " + $$0 + " " + $$1)
      );
   }

   public void queueSectionData(LightLayer $$0, SectionPos $$1, DataLayer $$2) {
      this.addTask(
         $$1.x(),
         $$1.z(),
         () -> 0,
         ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
         Util.name(() -> super.queueSectionData($$0, $$1, $$2), () -> "queueData " + $$1)
      );
   }

   private void addTask(int $$0, int $$1, ThreadedLevelLightEngine.TaskType $$2, Runnable $$3) {
      this.addTask($$0, $$1, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong($$0, $$1)), $$2, $$3);
   }

   private void addTask(int $$0, int $$1, IntSupplier $$2, ThreadedLevelLightEngine.TaskType $$3, Runnable $$4) {
      this.taskDispatcher.submit(() -> {
         this.lightTasks.add(Pair.of($$3, $$4));
         if (this.lightTasks.size() >= 1000) {
            this.runUpdate();
         }
      }, ChunkPos.asLong($$0, $$1), $$2);
   }

   public void retainData(ChunkPos $$0, boolean $$1) {
      this.addTask($$0.x, $$0.z, () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> super.retainData($$0, $$1), () -> "retainData " + $$0));
   }

   public CompletableFuture<ChunkAccess> initializeLight(ChunkAccess $$0, boolean $$1) {
      ChunkPos $$2 = $$0.getPos();
      this.addTask($$2.x, $$2.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         LevelChunkSection[] $$2x = $$0.getSections();

         for (int $$3 = 0; $$3 < $$0.getSectionsCount(); $$3++) {
            LevelChunkSection $$4 = $$2x[$$3];
            if (!$$4.hasOnlyAir()) {
               int $$5 = this.levelHeightAccessor.getSectionYFromSectionIndex($$3);
               super.updateSectionStatus(SectionPos.of($$2, $$5), false);
            }
         }
      }, () -> "initializeLight: " + $$2));
      return CompletableFuture.supplyAsync(() -> {
         super.setLightEnabled($$2, $$1);
         super.retainData($$2, false);
         return $$0;
      }, $$1x -> this.addTask($$2.x, $$2.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, $$1x));
   }

   public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess $$0, boolean $$1) {
      ChunkPos $$2 = $$0.getPos();
      $$0.setLightCorrect(false);
      this.addTask($$2.x, $$2.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         if (!$$1) {
            super.propagateLightSources($$2);
         }

         if (SharedConstants.DEBUG_VERBOSE_SERVER_EVENTS) {
            LOGGER.debug("LIT {}", $$2);
         }
      }, () -> "lightChunk " + $$2 + " " + $$1));
      return CompletableFuture.supplyAsync(() -> {
         $$0.setLightCorrect(true);
         return $$0;
      }, $$1x -> this.addTask($$2.x, $$2.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, $$1x));
   }

   public void tryScheduleUpdate() {
      if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
         this.consecutiveExecutor.schedule(() -> {
            this.runUpdate();
            this.scheduled.set(false);
         });
      }
   }

   private void runUpdate() {
      int $$0 = Math.min(this.lightTasks.size(), 1000);
      ObjectListIterator<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> $$1 = this.lightTasks.iterator();

      int $$2;
      for ($$2 = 0; $$1.hasNext() && $$2 < $$0; $$2++) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> $$3 = (Pair<ThreadedLevelLightEngine.TaskType, Runnable>)$$1.next();
         if ($$3.getFirst() == ThreadedLevelLightEngine.TaskType.PRE_UPDATE) {
            ((Runnable)$$3.getSecond()).run();
         }
      }

      $$1.back($$2);
      super.runLightUpdates();

      for (int var5 = 0; $$1.hasNext() && var5 < $$0; var5++) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> $$4 = (Pair<ThreadedLevelLightEngine.TaskType, Runnable>)$$1.next();
         if ($$4.getFirst() == ThreadedLevelLightEngine.TaskType.POST_UPDATE) {
            ((Runnable)$$4.getSecond()).run();
         }

         $$1.remove();
      }
   }

   public CompletableFuture<?> waitForPendingTasks(int $$0, int $$1) {
      return CompletableFuture.runAsync(() -> {}, $$2 -> this.addTask($$0, $$1, ThreadedLevelLightEngine.TaskType.POST_UPDATE, $$2));
   }

   static enum TaskType {
      PRE_UPDATE,
      POST_UPDATE;
   }
}
