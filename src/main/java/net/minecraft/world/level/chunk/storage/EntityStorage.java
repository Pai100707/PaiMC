package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String ENTITIES_TAG = "Entities";
   private static final String POSITION_TAG = "Position";
   private final ServerLevel level;
   private final SimpleRegionStorage simpleRegionStorage;
   private final LongSet emptyChunks = new LongOpenHashSet();
   private final ConsecutiveExecutor entityDeserializerQueue;

   public EntityStorage(SimpleRegionStorage $$0, ServerLevel $$1, Executor $$2) {
      this.simpleRegionStorage = $$0;
      this.level = $$1;
      this.entityDeserializerQueue = new ConsecutiveExecutor($$2, "entity-deserializer");
   }

   @Override
   public CompletableFuture<ChunkEntities<Entity>> loadEntities(net.minecraft.world.level.ChunkPos $$0) {
      if (this.emptyChunks.contains($$0.toLong())) {
         return CompletableFuture.completedFuture(emptyChunk($$0));
      } else {
         CompletableFuture<Optional<CompoundTag>> $$1 = this.simpleRegionStorage.read($$0);
         this.reportLoadFailureIfPresent($$1, $$0);
         return $$1.thenApplyAsync(
            $$1x -> {
               if ($$1x.isEmpty()) {
                  this.emptyChunks.add($$0.toLong());
                  return emptyChunk($$0);
               } else {
                  try {
                     net.minecraft.world.level.ChunkPos $$2 = (net.minecraft.world.level.ChunkPos)((CompoundTag)$$1x.get())
                        .read("Position", net.minecraft.world.level.ChunkPos.CODEC)
                        .orElseThrow();
                     if (!Objects.equals($$0, $$2)) {
                        LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", new Object[]{$$0, $$0, $$2});
                        this.level.getServer().reportMisplacedChunk($$2, $$0, this.simpleRegionStorage.storageInfo());
                     }
                  } catch (Exception var11) {
                     LOGGER.warn("Failed to parse chunk {} position info", $$0, var11);
                     this.level.getServer().reportChunkLoadFailure(var11, this.simpleRegionStorage.storageInfo(), $$0);
                  }

                  CompoundTag $$4 = this.simpleRegionStorage.upgradeChunkTag((CompoundTag)$$1x.get(), -1);
                  ScopedCollector $$5 = new ScopedCollector(ChunkAccess.problemPath($$0), LOGGER);

                  ChunkEntities var8;
                  try {
                     ValueInput $$6 = TagValueInput.create($$5, this.level.registryAccess(), $$4);
                     ValueInput.ValueInputList $$7 = $$6.childrenListOrEmpty("Entities");
                     List<Entity> $$8 = EntityType.loadEntitiesRecursive($$7, this.level, EntitySpawnReason.LOAD).toList();
                     var8 = new ChunkEntities<>($$0, $$8);
                  } catch (Throwable var10) {
                     try {
                        $$5.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }

                     throw var10;
                  }

                  $$5.close();
                  return var8;
               }
            },
            this.entityDeserializerQueue::schedule
         );
      }
   }

   private static ChunkEntities<Entity> emptyChunk(net.minecraft.world.level.ChunkPos $$0) {
      return new ChunkEntities<>($$0, List.of());
   }

   @Override
   public void storeEntities(ChunkEntities<Entity> $$0) {
      net.minecraft.world.level.ChunkPos $$1 = $$0.getPos();
      if ($$0.isEmpty()) {
         if (this.emptyChunks.add($$1.toLong())) {
            this.reportSaveFailureIfPresent(this.simpleRegionStorage.write($$1, IOWorker.STORE_EMPTY), $$1);
         }
      } else {
         ScopedCollector $$2 = new ScopedCollector(ChunkAccess.problemPath($$1), LOGGER);

         try {
            ListTag $$3 = new ListTag();
            $$0.getEntities().forEach($$2x -> {
               TagValueOutput $$3x = TagValueOutput.createWithContext($$2.forChild($$2x.problemPath()), $$2x.registryAccess());
               if ($$2x.save($$3x)) {
                  CompoundTag $$4x = $$3x.buildResult();
                  $$3.add($$4x);
               }
            });
            CompoundTag $$4 = NbtUtils.addCurrentDataVersion(new CompoundTag());
            $$4.put("Entities", $$3);
            $$4.store("Position", net.minecraft.world.level.ChunkPos.CODEC, $$1);
            this.reportSaveFailureIfPresent(this.simpleRegionStorage.write($$1, $$4), $$1);
            this.emptyChunks.remove($$1.toLong());
         } catch (Throwable var7) {
            try {
               $$2.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         $$2.close();
      }
   }

   private void reportSaveFailureIfPresent(CompletableFuture<?> $$0, net.minecraft.world.level.ChunkPos $$1) {
      $$0.exceptionally($$1x -> {
         LOGGER.error("Failed to store entity chunk {}", $$1, $$1x);
         this.level.getServer().reportChunkSaveFailure($$1x, this.simpleRegionStorage.storageInfo(), $$1);
         return null;
      });
   }

   private void reportLoadFailureIfPresent(CompletableFuture<?> $$0, net.minecraft.world.level.ChunkPos $$1) {
      $$0.exceptionally($$1x -> {
         LOGGER.error("Failed to load entity chunk {}", $$1, $$1x);
         this.level.getServer().reportChunkLoadFailure($$1x, this.simpleRegionStorage.storageInfo(), $$1);
         return null;
      });
   }

   @Override
   public void flush(boolean $$0) {
      this.simpleRegionStorage.synchronize($$0).join();
      this.entityDeserializerQueue.runAll();
   }

   @Override
   public void close() throws IOException {
      this.simpleRegionStorage.close();
   }
}
