package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products.P4;
import com.mojang.datafixers.Products.P5;
import com.mojang.datafixers.Products.P9;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;

public class ConcentricRingsStructurePlacement extends StructurePlacement {
   public static final MapCodec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> codec($$0).apply($$0, ConcentricRingsStructurePlacement::new)
   );
   private final int distance;
   private final int spread;
   private final int count;
   private final HolderSet<Biome> preferredBiomes;

   private static P9<Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, HolderSet<Biome>> codec(
      Instance<ConcentricRingsStructurePlacement> $$0
   ) {
      P5<Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> $$1 = placementCodec(
         $$0
      );
      P4<Mu<ConcentricRingsStructurePlacement>, Integer, Integer, Integer, HolderSet<Biome>> $$2 = $$0.group(
         Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance),
         Codec.intRange(0, 1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread),
         Codec.intRange(1, 4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count),
         RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("preferred_biomes").forGetter(ConcentricRingsStructurePlacement::preferredBiomes)
      );
      return new P9($$1.t1(), $$1.t2(), $$1.t3(), $$1.t4(), $$1.t5(), $$2.t1(), $$2.t2(), $$2.t3(), $$2.t4());
   }

   public ConcentricRingsStructurePlacement(
      Vec3i $$0,
      StructurePlacement.FrequencyReductionMethod $$1,
      float $$2,
      int $$3,
      Optional<StructurePlacement.ExclusionZone> $$4,
      int $$5,
      int $$6,
      int $$7,
      HolderSet<Biome> $$8
   ) {
      super($$0, $$1, $$2, $$3, $$4);
      this.distance = $$5;
      this.spread = $$6;
      this.count = $$7;
      this.preferredBiomes = $$8;
   }

   public ConcentricRingsStructurePlacement(int $$0, int $$1, int $$2, HolderSet<Biome> $$3) {
      this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty(), $$0, $$1, $$2, $$3);
   }

   public int distance() {
      return this.distance;
   }

   public int spread() {
      return this.spread;
   }

   public int count() {
      return this.count;
   }

   public HolderSet<Biome> preferredBiomes() {
      return this.preferredBiomes;
   }

   @Override
   protected boolean isPlacementChunk(ChunkGeneratorStructureState $$0, int $$1, int $$2) {
      List<net.minecraft.world.level.ChunkPos> $$3 = $$0.getRingPositionsFor(this);
      return $$3 == null ? false : $$3.contains(new net.minecraft.world.level.ChunkPos($$1, $$2));
   }

   @Override
   public StructurePlacementType<?> type() {
      return StructurePlacementType.CONCENTRIC_RINGS;
   }
}
