package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.LegacyTagFixer;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class LegacyStructureDataHandler implements LegacyTagFixer {
   public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
   private static final Map<String, String> CURRENT_TO_LEGACY_MAP = (Map<String, String>)Util.make(Maps.newHashMap(), $$0 -> {
      $$0.put("Village", "Village");
      $$0.put("Mineshaft", "Mineshaft");
      $$0.put("Mansion", "Mansion");
      $$0.put("Igloo", "Temple");
      $$0.put("Desert_Pyramid", "Temple");
      $$0.put("Jungle_Pyramid", "Temple");
      $$0.put("Swamp_Hut", "Temple");
      $$0.put("Stronghold", "Stronghold");
      $$0.put("Monument", "Monument");
      $$0.put("Fortress", "Fortress");
      $$0.put("EndCity", "EndCity");
   });
   private static final Map<String, String> LEGACY_TO_CURRENT_MAP = (Map<String, String>)Util.make(Maps.newHashMap(), $$0 -> {
      $$0.put("Iglu", "Igloo");
      $$0.put("TeDP", "Desert_Pyramid");
      $$0.put("TeJP", "Jungle_Pyramid");
      $$0.put("TeSH", "Swamp_Hut");
   });
   private static final Set<String> OLD_STRUCTURE_REGISTRY_KEYS = Set.of(
      "pillager_outpost",
      "mineshaft",
      "mansion",
      "jungle_pyramid",
      "desert_pyramid",
      "igloo",
      "ruined_portal",
      "shipwreck",
      "swamp_hut",
      "stronghold",
      "monument",
      "ocean_ruin",
      "fortress",
      "endcity",
      "buried_treasure",
      "village",
      "nether_fossil",
      "bastion_remnant"
   );
   private final boolean hasLegacyData;
   private final Map<String, Long2ObjectMap<CompoundTag>> dataMap = Maps.newHashMap();
   private final Map<String, StructureFeatureIndexSavedData> indexMap = Maps.newHashMap();
   
   private final DimensionDataStorage dimensionDataStorage;
   private final List<String> legacyKeys;
   private final List<String> currentKeys;
   private final DataFixer dataFixer;
   private boolean cachesInitialized;

   public LegacyStructureDataHandler(DimensionDataStorage $$0, List<String> $$1, List<String> $$2, DataFixer $$3) {
      this.dimensionDataStorage = $$0;
      this.legacyKeys = $$1;
      this.currentKeys = $$2;
      this.dataFixer = $$3;
      boolean $$4 = false;

      for (String $$5 : this.currentKeys) {
         $$4 |= this.dataMap.get($$5) != null;
      }

      this.hasLegacyData = $$4;
   }

   @Override
   public void markChunkDone(net.minecraft.world.level.ChunkPos $$0) {
      long $$1 = $$0.toLong();

      for (String $$2 : this.legacyKeys) {
         StructureFeatureIndexSavedData $$3 = this.indexMap.get($$2);
         if ($$3 != null && $$3.hasUnhandledIndex($$1)) {
            $$3.removeIndex($$1);
         }
      }
   }

   @Override
   public int targetDataVersion() {
      return 1493;
   }

   @Override
   public CompoundTag applyFix(CompoundTag $$0) {
      if (!this.cachesInitialized && this.dimensionDataStorage != null) {
         this.populateCaches(this.dimensionDataStorage);
      }

      int $$1 = NbtUtils.getDataVersion($$0);
      if ($$1 < 1493) {
         $$0 = DataFixTypes.CHUNK.update(this.dataFixer, $$0, $$1, 1493);
         if ($$0.getCompound("Level").flatMap($$0x -> $$0x.getBoolean("hasLegacyStructureData")).orElse(false)) {
            $$0 = this.updateFromLegacy($$0);
         }
      }

      return $$0;
   }

   private CompoundTag updateFromLegacy(CompoundTag $$0) {
      CompoundTag $$1 = $$0.getCompoundOrEmpty("Level");
      net.minecraft.world.level.ChunkPos $$2 = new net.minecraft.world.level.ChunkPos($$1.getIntOr("xPos", 0), $$1.getIntOr("zPos", 0));
      if (this.isUnhandledStructureStart($$2.x, $$2.z)) {
         $$0 = this.updateStructureStart($$0, $$2);
      }

      CompoundTag $$3 = $$1.getCompoundOrEmpty("Structures");
      CompoundTag $$4 = $$3.getCompoundOrEmpty("References");

      for (String $$5 : this.currentKeys) {
         boolean $$6 = OLD_STRUCTURE_REGISTRY_KEYS.contains($$5.toLowerCase(Locale.ROOT));
         if (!$$4.getLongArray($$5).isPresent() && $$6) {
            int $$7 = 8;
            LongList $$8 = new LongArrayList();

            for (int $$9 = $$2.x - 8; $$9 <= $$2.x + 8; $$9++) {
               for (int $$10 = $$2.z - 8; $$10 <= $$2.z + 8; $$10++) {
                  if (this.hasLegacyStart($$9, $$10, $$5)) {
                     $$8.add(net.minecraft.world.level.ChunkPos.asLong($$9, $$10));
                  }
               }
            }

            $$4.putLongArray($$5, $$8.toLongArray());
         }
      }

      $$3.put("References", $$4);
      $$1.put("Structures", $$3);
      $$0.put("Level", $$1);
      return $$0;
   }

   private boolean hasLegacyStart(int $$0, int $$1, String $$2) {
      return !this.hasLegacyData
         ? false
         : this.dataMap.get($$2) != null
            && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get($$2)).hasStartIndex(net.minecraft.world.level.ChunkPos.asLong($$0, $$1));
   }

   private boolean isUnhandledStructureStart(int $$0, int $$1) {
      if (!this.hasLegacyData) {
         return false;
      } else {
         for (String $$2 : this.currentKeys) {
            if (this.dataMap.get($$2) != null
               && this.indexMap.get(CURRENT_TO_LEGACY_MAP.get($$2)).hasUnhandledIndex(net.minecraft.world.level.ChunkPos.asLong($$0, $$1))) {
               return true;
            }
         }

         return false;
      }
   }

   private CompoundTag updateStructureStart(CompoundTag $$0, net.minecraft.world.level.ChunkPos $$1) {
      CompoundTag $$2 = $$0.getCompoundOrEmpty("Level");
      CompoundTag $$3 = $$2.getCompoundOrEmpty("Structures");
      CompoundTag $$4 = $$3.getCompoundOrEmpty("Starts");

      for (String $$5 : this.currentKeys) {
         Long2ObjectMap<CompoundTag> $$6 = this.dataMap.get($$5);
         if ($$6 != null) {
            long $$7 = $$1.toLong();
            if (this.indexMap.get(CURRENT_TO_LEGACY_MAP.get($$5)).hasUnhandledIndex($$7)) {
               CompoundTag $$8 = (CompoundTag)$$6.get($$7);
               if ($$8 != null) {
                  $$4.put($$5, $$8);
               }
            }
         }
      }

      $$3.put("Starts", $$4);
      $$2.put("Structures", $$3);
      $$0.put("Level", $$2);
      return $$0;
   }

   private synchronized void populateCaches(DimensionDataStorage $$0) {
      if (!this.cachesInitialized) {
         for (String $$1 : this.legacyKeys) {
            CompoundTag $$2 = new CompoundTag();

            try {
               $$2 = $$0.readTagFromDisk($$1, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES, 1493)
                  .getCompoundOrEmpty("data")
                  .getCompoundOrEmpty("Features");
               if ($$2.isEmpty()) {
                  continue;
               }
            } catch (IOException var8) {
            }

            $$2.forEach(($$0x, $$1x) -> {
               if ($$1x instanceof CompoundTag $$2x) {
                  long $$4x = net.minecraft.world.level.ChunkPos.asLong($$2x.getIntOr("ChunkX", 0), $$2x.getIntOr("ChunkZ", 0));
                  ListTag $$5x = $$2x.getListOrEmpty("Children");
                  if (!$$5x.isEmpty()) {
                     Optional<String> $$6 = $$5x.getCompound(0).flatMap($$0xx -> $$0xx.getString("id"));
                     $$6.map(LEGACY_TO_CURRENT_MAP::get).ifPresent($$1xx -> $$2x.putString("id", $$1xx));
                  }

                  $$2x.getString("id").ifPresent($$2xx -> this.dataMap.computeIfAbsent($$2xx, $$0xxx -> new Long2ObjectOpenHashMap()).put($$4x, $$2x));
               }
            });
            String $$3 = $$1 + "_index";
            StructureFeatureIndexSavedData $$4 = $$0.computeIfAbsent(StructureFeatureIndexSavedData.type($$3));
            if ($$4.getAll().isEmpty()) {
               StructureFeatureIndexSavedData $$5 = new StructureFeatureIndexSavedData();
               this.indexMap.put($$1, $$5);
               $$2.forEach(($$1x, $$2x) -> {
                  if ($$2x instanceof CompoundTag $$3x) {
                     $$5.addIndex(net.minecraft.world.level.ChunkPos.asLong($$3x.getIntOr("ChunkX", 0), $$3x.getIntOr("ChunkZ", 0)));
                  }
               });
            } else {
               this.indexMap.put($$1, $$4);
            }
         }

         this.cachesInitialized = true;
      }
   }

   public static Supplier<LegacyTagFixer> getLegacyTagFixer(ResourceKey<net.minecraft.world.level.Level> $$0, Supplier<DimensionDataStorage> $$1, DataFixer $$2) {
      if ($$0 == net.minecraft.world.level.Level.OVERWORLD) {
         return () -> new LegacyStructureDataHandler(
            $$1.get(),
            ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"),
            ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"),
            $$2
         );
      } else if ($$0 == net.minecraft.world.level.Level.NETHER) {
         List<String> $$3 = ImmutableList.of("Fortress");
         return () -> new LegacyStructureDataHandler($$1.get(), $$3, $$3, $$2);
      } else if ($$0 == net.minecraft.world.level.Level.END) {
         List<String> $$4 = ImmutableList.of("EndCity");
         return () -> new LegacyStructureDataHandler($$1.get(), $$4, $$4, $$2);
      } else {
         return LegacyTagFixer.EMPTY;
      }
   }
}
