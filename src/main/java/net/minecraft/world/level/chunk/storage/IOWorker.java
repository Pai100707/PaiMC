package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.thread.PriorityConsecutiveExecutor;
import net.minecraft.util.thread.StrictQueue.RunnableWithPriority;
import org.slf4j.Logger;

public class IOWorker implements ChunkScanAccess, AutoCloseable {
   public static final Supplier<CompoundTag> STORE_EMPTY = () -> null;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final AtomicBoolean shutdownRequested = new AtomicBoolean();
   private final PriorityConsecutiveExecutor consecutiveExecutor;
   private final RegionFileStorage storage;
   private final SequencedMap<net.minecraft.world.level.ChunkPos, IOWorker.PendingStore> pendingWrites = new LinkedHashMap<>();
   private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap();
   private static final int REGION_CACHE_SIZE = 1024;

   protected IOWorker(RegionStorageInfo $$0, Path $$1, boolean $$2) {
      this.storage = new RegionFileStorage($$0, $$1, $$2);
      this.consecutiveExecutor = new PriorityConsecutiveExecutor(IOWorker.Priority.values().length, Util.ioPool(), "IOWorker-" + $$0.type());
   }

   public boolean isOldChunkAround(net.minecraft.world.level.ChunkPos $$0, int $$1) {
      net.minecraft.world.level.ChunkPos $$2 = new net.minecraft.world.level.ChunkPos($$0.x - $$1, $$0.z - $$1);
      net.minecraft.world.level.ChunkPos $$3 = new net.minecraft.world.level.ChunkPos($$0.x + $$1, $$0.z + $$1);

      for (int $$4 = $$2.getRegionX(); $$4 <= $$3.getRegionX(); $$4++) {
         for (int $$5 = $$2.getRegionZ(); $$5 <= $$3.getRegionZ(); $$5++) {
            BitSet $$6 = this.getOrCreateOldDataForRegion($$4, $$5).join();
            if (!$$6.isEmpty()) {
               net.minecraft.world.level.ChunkPos $$7 = net.minecraft.world.level.ChunkPos.minFromRegion($$4, $$5);
               int $$8 = Math.max($$2.x - $$7.x, 0);
               int $$9 = Math.max($$2.z - $$7.z, 0);
               int $$10 = Math.min($$3.x - $$7.x, 31);
               int $$11 = Math.min($$3.z - $$7.z, 31);

               for (int $$12 = $$8; $$12 <= $$10; $$12++) {
                  for (int $$13 = $$9; $$13 <= $$11; $$13++) {
                     int $$14 = $$13 * 32 + $$12;
                     if ($$6.get($$14)) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int $$0, int $$1) {
      long $$2 = net.minecraft.world.level.ChunkPos.asLong($$0, $$1);
      synchronized (this.regionCacheForBlender) {
         CompletableFuture<BitSet> $$3 = (CompletableFuture<BitSet>)this.regionCacheForBlender.getAndMoveToFirst($$2);
         if ($$3 == null) {
            $$3 = this.createOldDataForRegion($$0, $$1);
            this.regionCacheForBlender.putAndMoveToFirst($$2, $$3);
            if (this.regionCacheForBlender.size() > 1024) {
               this.regionCacheForBlender.removeLast();
            }
         }

         return $$3;
      }
   }

   private CompletableFuture<BitSet> createOldDataForRegion(int $$0, int $$1) {
      return CompletableFuture.supplyAsync(
         () -> {
            net.minecraft.world.level.ChunkPos $$2 = net.minecraft.world.level.ChunkPos.minFromRegion($$0, $$1);
            net.minecraft.world.level.ChunkPos $$3 = net.minecraft.world.level.ChunkPos.maxFromRegion($$0, $$1);
            BitSet $$4 = new BitSet();
            net.minecraft.world.level.ChunkPos.rangeClosed($$2, $$3)
               .forEach(
                  $$1xx -> {
                     CollectFields $$2x = new CollectFields(
                        new FieldSelector[]{new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data")}
                     );

                     try {
                        this.scanChunk($$1xx, $$2x).join();
                     } catch (Exception var7) {
                        LOGGER.warn("Failed to scan chunk {}", $$1xx, var7);
                        return;
                     }

                     if ($$2x.getResult() instanceof CompoundTag $$5 && this.isOldChunk($$5)) {
                        int $$6 = $$1xx.getRegionLocalZ() * 32 + $$1xx.getRegionLocalX();
                        $$4.set($$6);
                     }
                  }
               );
            return $$4;
         },
         Util.backgroundExecutor()
      );
   }

   private boolean isOldChunk(CompoundTag $$0) {
      return $$0.getIntOr("DataVersion", 0) < 4295 ? true : $$0.getCompound("blending_data").isPresent();
   }

   public CompletableFuture<Void> store(net.minecraft.world.level.ChunkPos $$0, CompoundTag $$1) {
      return this.store($$0, (Supplier<CompoundTag>)(() -> $$1));
   }

   public CompletableFuture<Void> store(net.minecraft.world.level.ChunkPos $$0, Supplier<CompoundTag> $$1) {
      return this.<CompletableFuture<Void>>submitTask(() -> {
         CompoundTag $$2 = $$1.get();
         IOWorker.PendingStore $$3 = this.pendingWrites.computeIfAbsent($$0, $$1xx -> new IOWorker.PendingStore($$2));
         $$3.data = $$2;
         return $$3.result;
      }).thenCompose(Function.identity());
   }

   public CompletableFuture<Optional<CompoundTag>> loadAsync(net.minecraft.world.level.ChunkPos $$0) {
      return this.submitThrowingTask(() -> {
         IOWorker.PendingStore $$1 = this.pendingWrites.get($$0);
         if ($$1 != null) {
            return Optional.ofNullable($$1.copyData());
         } else {
            try {
               CompoundTag $$2 = this.storage.read($$0);
               return Optional.ofNullable($$2);
            } catch (Exception var4) {
               LOGGER.warn("Failed to read chunk {}", $$0, var4);
               throw var4;
            }
         }
      });
   }

   public CompletableFuture<Void> synchronize(boolean $$0) {
      CompletableFuture<Void> $$1 = this.<CompletableFuture<Void>>submitTask(
            () -> CompletableFuture.allOf(this.pendingWrites.values().stream().map($$0x -> $$0x.result).toArray(CompletableFuture[]::new))
         )
         .thenCompose(Function.identity());
      return $$0 ? $$1.thenCompose($$0x -> this.submitThrowingTask(() -> {
         try {
            this.storage.flush();
            return null;
         } catch (Exception var2x) {
            LOGGER.warn("Failed to synchronize chunks", var2x);
            throw var2x;
         }
      })) : $$1.thenCompose($$0x -> this.submitTask(() -> null));
   }

   @Override
   public CompletableFuture<Void> scanChunk(net.minecraft.world.level.ChunkPos $$0, StreamTagVisitor $$1) {
      return this.submitThrowingTask(() -> {
         try {
            IOWorker.PendingStore $$2 = this.pendingWrites.get($$0);
            if ($$2 != null) {
               if ($$2.data != null) {
                  $$2.data.acceptAsRoot($$1);
               }
            } else {
               this.storage.scanChunk($$0, $$1);
            }

            return null;
         } catch (Exception var4) {
            LOGGER.warn("Failed to bulk scan chunk {}", $$0, var4);
            throw var4;
         }
      });
   }

   private <T> CompletableFuture<T> submitThrowingTask(IOWorker.ThrowingSupplier<T> $$0) {
      return this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.FOREGROUND.ordinal(), $$1 -> {
         if (!this.shutdownRequested.get()) {
            try {
               $$1.complete($$0.get());
            } catch (Exception var4) {
               $$1.completeExceptionally(var4);
            }
         }

         this.tellStorePending();
      });
   }

   private <T> CompletableFuture<T> submitTask(Supplier<T> $$0) {
      return this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.FOREGROUND.ordinal(), $$1 -> {
         if (!this.shutdownRequested.get()) {
            $$1.complete($$0.get());
         }

         this.tellStorePending();
      });
   }

   private void storePendingChunk() {
      Entry<net.minecraft.world.level.ChunkPos, IOWorker.PendingStore> $$0 = this.pendingWrites.pollFirstEntry();
      if ($$0 != null) {
         this.runStore($$0.getKey(), $$0.getValue());
         this.tellStorePending();
      }
   }

   private void tellStorePending() {
      this.consecutiveExecutor.schedule(new RunnableWithPriority(IOWorker.Priority.BACKGROUND.ordinal(), this::storePendingChunk));
   }

   private void runStore(net.minecraft.world.level.ChunkPos $$0, IOWorker.PendingStore $$1) {
      try {
         this.storage.write($$0, $$1.data);
         $$1.result.complete(null);
      } catch (Exception var4) {
         LOGGER.error("Failed to store chunk {}", $$0, var4);
         $$1.result.completeExceptionally(var4);
      }
   }

   @Override
   public void close() throws IOException {
      if (this.shutdownRequested.compareAndSet(false, true)) {
         this.waitForShutdown();
         this.consecutiveExecutor.close();

         try {
            this.storage.close();
         } catch (Exception var2) {
            LOGGER.error("Failed to close storage", var2);
         }
      }
   }

   private void waitForShutdown() {
      this.consecutiveExecutor.scheduleWithResult(IOWorker.Priority.SHUTDOWN.ordinal(), $$0 -> $$0.complete(Unit.INSTANCE)).join();
   }

   public RegionStorageInfo storageInfo() {
      return this.storage.info();
   }

   static class PendingStore {
      
      CompoundTag data;
      final CompletableFuture<Void> result = new CompletableFuture<>();

      public PendingStore(CompoundTag $$0) {
         this.data = $$0;
      }

      
      CompoundTag copyData() {
         CompoundTag $$0 = this.data;
         return $$0 == null ? null : $$0.copy();
      }
   }

   static enum Priority {
      FOREGROUND,
      BACKGROUND,
      SHUTDOWN;
   }

   @FunctionalInterface
   interface ThrowingSupplier<T> {
      
      T get() throws Exception;
   }
}
