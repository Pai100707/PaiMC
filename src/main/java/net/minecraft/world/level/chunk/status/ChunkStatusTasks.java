package net.minecraft.world.level.chunk.status;

import com.mojang.logging.LogUtils;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

public class ChunkStatusTasks {
   private static final Logger LOGGER = LogUtils.getLogger();

   private static boolean isLighted(ChunkAccess $$0) {
      return $$0.getPersistedStatus().isOrAfter(ChunkStatus.LIGHT) && $$0.isLightCorrect();
   }

   static CompletableFuture<ChunkAccess> passThrough(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> generateStructureStarts(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      ServerLevel $$4 = $$0.level();
      if ($$4.getServer().getWorldData().worldGenOptions().generateStructures()) {
         $$0.generator()
            .createStructures(
               $$4.registryAccess(), $$4.getChunkSource().getGeneratorState(), $$4.structureManager(), $$3, $$0.structureManager(), $$4.dimension()
            );
      }

      $$4.onStructureStartsAvailable($$3);
      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> loadStructureStarts(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      $$0.level().onStructureStartsAvailable($$3);
      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> generateStructureReferences(
      WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3
   ) {
      ServerLevel $$4 = $$0.level();
      WorldGenRegion $$5 = new WorldGenRegion($$4, $$2, $$1, $$3);
      $$0.generator().createReferences($$5, $$4.structureManager().forWorldGenRegion($$5), $$3);
      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> generateBiomes(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      ServerLevel $$4 = $$0.level();
      WorldGenRegion $$5 = new WorldGenRegion($$4, $$2, $$1, $$3);
      return $$0.generator().createBiomes($$4.getChunkSource().randomState(), Blender.of($$5), $$4.structureManager().forWorldGenRegion($$5), $$3);
   }

   static CompletableFuture<ChunkAccess> generateNoise(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      ServerLevel $$4 = $$0.level();
      WorldGenRegion $$5 = new WorldGenRegion($$4, $$2, $$1, $$3);
      return $$0.generator()
         .fillFromNoise(Blender.of($$5), $$4.getChunkSource().randomState(), $$4.structureManager().forWorldGenRegion($$5), $$3)
         .thenApply($$0x -> {
            if ($$0x instanceof ProtoChunk $$1x) {
               BelowZeroRetrogen $$2x = $$1x.getBelowZeroRetrogen();
               if ($$2x != null) {
                  BelowZeroRetrogen.replaceOldBedrock($$1x);
                  if ($$2x.hasBedrockHoles()) {
                     $$2x.applyBedrockMask($$1x);
                  }
               }
            }

            return $$0x;
         });
   }

   static CompletableFuture<ChunkAccess> generateSurface(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      ServerLevel $$4 = $$0.level();
      WorldGenRegion $$5 = new WorldGenRegion($$4, $$2, $$1, $$3);
      $$0.generator().buildSurface($$5, $$4.structureManager().forWorldGenRegion($$5), $$4.getChunkSource().randomState(), $$3);
      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> generateCarvers(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      ServerLevel $$4 = $$0.level();
      WorldGenRegion $$5 = new WorldGenRegion($$4, $$2, $$1, $$3);
      if ($$3 instanceof ProtoChunk $$6) {
         Blender.addAroundOldChunksCarvingMaskFilter($$5, $$6);
      }

      $$0.generator()
         .applyCarvers($$5, $$4.getSeed(), $$4.getChunkSource().randomState(), $$4.getBiomeManager(), $$4.structureManager().forWorldGenRegion($$5), $$3);
      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> generateFeatures(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      ServerLevel $$4 = $$0.level();
      Heightmap.primeHeightmaps(
         $$3,
         EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE)
      );
      WorldGenRegion $$5 = new WorldGenRegion($$4, $$2, $$1, $$3);
      if (!SharedConstants.DEBUG_DISABLE_FEATURES) {
         $$0.generator().applyBiomeDecoration($$5, $$3, $$4.structureManager().forWorldGenRegion($$5));
      }

      Blender.generateBorderTicks($$5, $$3);
      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> initializeLight(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      ThreadedLevelLightEngine $$4 = $$0.lightEngine();
      $$3.initializeLightSources();
      ((ProtoChunk)$$3).setLightEngine($$4);
      boolean $$5 = isLighted($$3);
      return $$4.initializeLight($$3, $$5);
   }

   static CompletableFuture<ChunkAccess> light(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      boolean $$4 = isLighted($$3);
      return $$0.lightEngine().lightChunk($$3, $$4);
   }

   static CompletableFuture<ChunkAccess> generateSpawn(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      if (!$$3.isUpgrading()) {
         $$0.generator().spawnOriginalMobs(new WorldGenRegion($$0.level(), $$2, $$1, $$3));
      }

      return CompletableFuture.completedFuture($$3);
   }

   static CompletableFuture<ChunkAccess> full(WorldGenContext $$0, ChunkStep $$1, StaticCache2D<GenerationChunkHolder> $$2, ChunkAccess $$3) {
      net.minecraft.world.level.ChunkPos $$4 = $$3.getPos();
      GenerationChunkHolder $$5 = (GenerationChunkHolder)$$2.get($$4.x, $$4.z);
      return CompletableFuture.supplyAsync(() -> {
         ProtoChunk $$3x = (ProtoChunk)$$3;
         ServerLevel $$4x = $$0.level();
         LevelChunk $$6;
         if ($$3x instanceof ImposterProtoChunk $$5x) {
            $$6 = $$5x.getWrapped();
         } else {
            $$6 = new LevelChunk($$4x, $$3x, $$3xx -> {
               ScopedCollector $$4xx = new ScopedCollector($$3.problemPath(), LOGGER);

               try {
                  postLoadProtoChunk($$4x, TagValueInput.create($$4xx, $$4x.registryAccess(), $$3x.getEntities()));
               } catch (Throwable var8) {
                  try {
                     $$4xx.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }

                  throw var8;
               }

               $$4xx.close();
            });
            $$5.replaceProtoChunk(new ImposterProtoChunk($$6, false));
         }

         $$6.setFullStatus($$5::getFullStatus);
         $$6.runPostLoad();
         $$6.setLoaded(true);
         $$6.registerAllBlockEntitiesAfterLevelLoad();
         $$6.registerTickContainerInLevel($$4x);
         $$6.setUnsavedListener($$0.unsavedListener());
         return $$6;
      }, $$0.mainThreadExecutor());
   }

   private static void postLoadProtoChunk(ServerLevel $$0, ValueInput.ValueInputList $$1) {
      if (!$$1.isEmpty()) {
         $$0.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive($$1, $$0, EntitySpawnReason.LOAD));
      }
   }
}
