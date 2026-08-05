package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class StructureCheck {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int NO_STRUCTURE = -1;
   private final ChunkScanAccess storageAccess;
   private final RegistryAccess registryAccess;
   private final StructureTemplateManager structureTemplateManager;
   private final ResourceKey<net.minecraft.world.level.Level> dimension;
   private final ChunkGenerator chunkGenerator;
   private final RandomState randomState;
   private final net.minecraft.world.level.LevelHeightAccessor heightAccessor;
   private final BiomeSource biomeSource;
   private final long seed;
   private final DataFixer fixerUpper;
   private final Long2ObjectMap<Object2IntMap<Structure>> loadedChunks = new Long2ObjectOpenHashMap();
   private final Map<Structure, Long2BooleanMap> featureChecks = new HashMap<>();

   public StructureCheck(
      ChunkScanAccess $$0,
      RegistryAccess $$1,
      StructureTemplateManager $$2,
      ResourceKey<net.minecraft.world.level.Level> $$3,
      ChunkGenerator $$4,
      RandomState $$5,
      net.minecraft.world.level.LevelHeightAccessor $$6,
      BiomeSource $$7,
      long $$8,
      DataFixer $$9
   ) {
      this.storageAccess = $$0;
      this.registryAccess = $$1;
      this.structureTemplateManager = $$2;
      this.dimension = $$3;
      this.chunkGenerator = $$4;
      this.randomState = $$5;
      this.heightAccessor = $$6;
      this.biomeSource = $$7;
      this.seed = $$8;
      this.fixerUpper = $$9;
   }

   public StructureCheckResult checkStart(net.minecraft.world.level.ChunkPos $$0, Structure $$1, StructurePlacement $$2, boolean $$3) {
      long $$4 = $$0.toLong();
      Object2IntMap<Structure> $$5 = (Object2IntMap<Structure>)this.loadedChunks.get($$4);
      if ($$5 != null) {
         return this.checkStructureInfo($$5, $$1, $$3);
      } else {
         StructureCheckResult $$6 = this.tryLoadFromStorage($$0, $$1, $$3, $$4);
         if ($$6 != null) {
            return $$6;
         } else if (!$$2.applyAdditionalChunkRestrictions($$0.x, $$0.z, this.seed)) {
            return StructureCheckResult.START_NOT_PRESENT;
         } else {
            boolean $$7 = this.featureChecks
               .computeIfAbsent($$1, $$0x -> new Long2BooleanOpenHashMap())
               .computeIfAbsent($$4, $$2x -> this.canCreateStructure($$0, $$1));
            return !$$7 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.CHUNK_LOAD_NEEDED;
         }
      }
   }

   private boolean canCreateStructure(net.minecraft.world.level.ChunkPos $$0, Structure $$1) {
      return $$1.findValidGenerationPoint(
            new Structure.GenerationContext(
               this.registryAccess,
               this.chunkGenerator,
               this.biomeSource,
               this.randomState,
               this.structureTemplateManager,
               this.seed,
               $$0,
               this.heightAccessor,
               $$1.biomes()::contains
            )
         )
         .isPresent();
   }

   
   private StructureCheckResult tryLoadFromStorage(net.minecraft.world.level.ChunkPos $$0, Structure $$1, boolean $$2, long $$3) {
      CollectFields $$4 = new CollectFields(
         new FieldSelector[]{
            new FieldSelector(IntTag.TYPE, "DataVersion"),
            new FieldSelector("Level", "Structures", CompoundTag.TYPE, "Starts"),
            new FieldSelector("structures", CompoundTag.TYPE, "starts")
         }
      );

      try {
         this.storageAccess.scanChunk($$0, $$4).join();
      } catch (Exception var13) {
         LOGGER.warn("Failed to read chunk {}", $$0, var13);
         return StructureCheckResult.CHUNK_LOAD_NEEDED;
      }

      if (!($$4.getResult() instanceof CompoundTag $$7)) {
         return null;
      } else {
         int $$8 = NbtUtils.getDataVersion($$7);
         if ($$8 <= 1493) {
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
         } else {
            SimpleRegionStorage.injectDatafixingContext($$7, ChunkMap.getChunkDataFixContextTag(this.dimension, this.chunkGenerator.getTypeNameForDataFixer()));

            CompoundTag $$9;
            try {
               $$9 = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, $$7, $$8);
            } catch (Exception var12) {
               LOGGER.warn("Failed to partially datafix chunk {}", $$0, var12);
               return StructureCheckResult.CHUNK_LOAD_NEEDED;
            }

            Object2IntMap<Structure> $$12 = this.loadStructures($$9);
            if ($$12 == null) {
               return null;
            } else {
               this.storeFullResults($$3, $$12);
               return this.checkStructureInfo($$12, $$1, $$2);
            }
         }
      }
   }

   
   private Object2IntMap<Structure> loadStructures(CompoundTag $$0) {
      Optional<CompoundTag> $$1 = $$0.getCompound("structures").flatMap($$0x -> $$0x.getCompound("starts"));
      if ($$1.isEmpty()) {
         return null;
      } else {
         CompoundTag $$2 = $$1.get();
         if ($$2.isEmpty()) {
            return Object2IntMaps.emptyMap();
         } else {
            Object2IntMap<Structure> $$3 = new Object2IntOpenHashMap();
            Registry<Structure> $$4 = this.registryAccess.lookupOrThrow(Registries.STRUCTURE);
            $$2.forEach(($$2x, $$3x) -> {
               Identifier $$4x = Identifier.tryParse($$2x);
               if ($$4x != null) {
                  Structure $$5 = (Structure)$$4.getValue($$4x);
                  if ($$5 != null) {
                     $$3x.asCompound().ifPresent($$2xx -> {
                        String $$3xx = $$2xx.getStringOr("id", "");
                        if (!"INVALID".equals($$3xx)) {
                           int $$4xx = $$2xx.getIntOr("references", 0);
                           $$3.put($$5, $$4xx);
                        }
                     });
                  }
               }
            });
            return $$3;
         }
      }
   }

   private static Object2IntMap<Structure> deduplicateEmptyMap(Object2IntMap<Structure> $$0) {
      return $$0.isEmpty() ? Object2IntMaps.emptyMap() : $$0;
   }

   private StructureCheckResult checkStructureInfo(Object2IntMap<Structure> $$0, Structure $$1, boolean $$2) {
      int $$3 = $$0.getOrDefault($$1, -1);
      return $$3 == -1 || $$2 && $$3 != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
   }

   public void onStructureLoad(net.minecraft.world.level.ChunkPos $$0, Map<Structure, StructureStart> $$1) {
      long $$2 = $$0.toLong();
      Object2IntMap<Structure> $$3 = new Object2IntOpenHashMap();
      $$1.forEach(($$1x, $$2x) -> {
         if ($$2x.isValid()) {
            $$3.put($$1x, $$2x.getReferences());
         }
      });
      this.storeFullResults($$2, $$3);
   }

   private void storeFullResults(long $$0, Object2IntMap<Structure> $$1) {
      this.loadedChunks.put($$0, deduplicateEmptyMap($$1));
      this.featureChecks.values().forEach($$1x -> $$1x.remove($$0));
   }

   public void incrementReference(net.minecraft.world.level.ChunkPos $$0, Structure $$1) {
      this.loadedChunks.compute($$0.toLong(), ($$1x, $$2) -> {
         if ($$2 == null || $$2.isEmpty()) {
            $$2 = new Object2IntOpenHashMap();
         }

         $$2.computeInt($$1, ($$0xx, $$1xx) -> $$1xx == null ? 1 : $$1xx + 1);
         return $$2;
      });
   }
}
