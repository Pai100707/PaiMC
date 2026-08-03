package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

public class CheckerboardColumnBiomeSource extends BiomeSource {
   public static final MapCodec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter($$0x -> $$0x.allowedBiomes),
            Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter($$0x -> $$0x.size)
         )
         .apply($$0, CheckerboardColumnBiomeSource::new)
   );
   private final HolderSet<Biome> allowedBiomes;
   private final int bitShift;
   private final int size;

   public CheckerboardColumnBiomeSource(HolderSet<Biome> $$0, int $$1) {
      this.allowedBiomes = $$0;
      this.bitShift = $$1 + 2;
      this.size = $$1;
   }

   @Override
   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return this.allowedBiomes.stream();
   }

   @Override
   protected MapCodec<? extends BiomeSource> codec() {
      return CODEC;
   }

   @Override
   public Holder<Biome> getNoiseBiome(int $$0, int $$1, int $$2, Climate.Sampler $$3) {
      return this.allowedBiomes.get(Math.floorMod(($$0 >> this.bitShift) + ($$2 >> this.bitShift), this.allowedBiomes.size()));
   }
}
