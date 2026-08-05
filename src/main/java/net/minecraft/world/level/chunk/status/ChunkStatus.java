package net.minecraft.world.level.chunk.status;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChunkStatus {
   public static final int MAX_STRUCTURE_DISTANCE = 8;
   private static final EnumSet<Heightmap.Types> WORLDGEN_HEIGHTMAPS = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
   public static final EnumSet<Heightmap.Types> FINAL_HEIGHTMAPS = EnumSet.of(
      Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
   );
   public static final ChunkStatus EMPTY = register("empty", null, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus STRUCTURE_STARTS = register("structure_starts", EMPTY, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus STRUCTURE_REFERENCES = register("structure_references", STRUCTURE_STARTS, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus BIOMES = register("biomes", STRUCTURE_REFERENCES, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus NOISE = register("noise", BIOMES, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus SURFACE = register("surface", NOISE, WORLDGEN_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus CARVERS = register("carvers", SURFACE, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus FEATURES = register("features", CARVERS, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus INITIALIZE_LIGHT = register("initialize_light", FEATURES, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus LIGHT = register("light", INITIALIZE_LIGHT, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus SPAWN = register("spawn", LIGHT, FINAL_HEIGHTMAPS, ChunkType.PROTOCHUNK);
   public static final ChunkStatus FULL = register("full", SPAWN, FINAL_HEIGHTMAPS, ChunkType.LEVELCHUNK);
   public static final Codec<ChunkStatus> CODEC = BuiltInRegistries.CHUNK_STATUS.byNameCodec();
   private final int index;
   private final ChunkStatus parent;
   private final ChunkType chunkType;
   private final EnumSet<Heightmap.Types> heightmapsAfter;

   private static ChunkStatus register(String $$0, ChunkStatus $$1, EnumSet<Heightmap.Types> $$2, ChunkType $$3) {
      return (ChunkStatus)Registry.register(BuiltInRegistries.CHUNK_STATUS, $$0, new ChunkStatus($$1, $$2, $$3));
   }

   public static List<ChunkStatus> getStatusList() {
      List<ChunkStatus> $$0 = Lists.newArrayList();

      ChunkStatus $$1;
      for ($$1 = FULL; $$1.getParent() != $$1; $$1 = $$1.getParent()) {
         $$0.add($$1);
      }

      $$0.add($$1);
      Collections.reverse($$0);
      return $$0;
   }

   @VisibleForTesting
   protected ChunkStatus(ChunkStatus $$0, EnumSet<Heightmap.Types> $$1, ChunkType $$2) {
      this.parent = $$0 == null ? this : $$0;
      this.chunkType = $$2;
      this.heightmapsAfter = $$1;
      this.index = $$0 == null ? 0 : $$0.getIndex() + 1;
   }

   public int getIndex() {
      return this.index;
   }

   public ChunkStatus getParent() {
      return this.parent;
   }

   public ChunkType getChunkType() {
      return this.chunkType;
   }

   public static ChunkStatus byName(String $$0) {
      return (ChunkStatus)BuiltInRegistries.CHUNK_STATUS.getValue(Identifier.tryParse($$0));
   }

   public EnumSet<Heightmap.Types> heightmapsAfter() {
      return this.heightmapsAfter;
   }

   public boolean isOrAfter(ChunkStatus $$0) {
      return this.getIndex() >= $$0.getIndex();
   }

   public boolean isAfter(ChunkStatus $$0) {
      return this.getIndex() > $$0.getIndex();
   }

   public boolean isOrBefore(ChunkStatus $$0) {
      return this.getIndex() <= $$0.getIndex();
   }

   public boolean isBefore(ChunkStatus $$0) {
      return this.getIndex() < $$0.getIndex();
   }

   public static ChunkStatus max(ChunkStatus $$0, ChunkStatus $$1) {
      return $$0.isAfter($$1) ? $$0 : $$1;
   }

   @Override
   public String toString() {
      return this.getName();
   }

   public String getName() {
      return BuiltInRegistries.CHUNK_STATUS.getKey(this).toString();
   }
}
