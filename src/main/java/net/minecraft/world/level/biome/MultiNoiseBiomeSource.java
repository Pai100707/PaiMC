package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class MultiNoiseBiomeSource extends BiomeSource {
   private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
   public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes");
   private static final MapCodec<Holder<MultiNoiseBiomeSourceParameterList>> PRESET_CODEC = MultiNoiseBiomeSourceParameterList.CODEC
      .fieldOf("preset")
      .withLifecycle(Lifecycle.stable());
   public static final MapCodec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC)
      .xmap(MultiNoiseBiomeSource::new, $$0 -> $$0.parameters);
   private final Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

   private MultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> $$0) {
      this.parameters = $$0;
   }

   public static MultiNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> $$0) {
      return new MultiNoiseBiomeSource(Either.left($$0));
   }

   public static MultiNoiseBiomeSource createFromPreset(Holder<MultiNoiseBiomeSourceParameterList> $$0) {
      return new MultiNoiseBiomeSource(Either.right($$0));
   }

   private Climate.ParameterList<Holder<Biome>> parameters() {
      return (Climate.ParameterList<Holder<Biome>>)this.parameters.map($$0 -> $$0, $$0 -> ((MultiNoiseBiomeSourceParameterList)$$0.value()).parameters());
   }

   @Override
   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return this.parameters().values().stream().map(Pair::getSecond);
   }

   @Override
   protected MapCodec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> $$0) {
      Optional<Holder<MultiNoiseBiomeSourceParameterList>> $$1 = this.parameters.right();
      return $$1.isPresent() && $$1.get().is($$0);
   }

   @Override
   public Holder<Biome> getNoiseBiome(int $$0, int $$1, int $$2, Climate.Sampler $$3) {
      return this.getNoiseBiome($$3.sample($$0, $$1, $$2));
   }

   @VisibleForDebug
   public Holder<Biome> getNoiseBiome(Climate.TargetPoint $$0) {
      return this.parameters().findValue($$0);
   }

   @Override
   public void addDebugInfo(List<String> $$0, BlockPos $$1, Climate.Sampler $$2) {
      int $$3 = QuartPos.fromBlock($$1.getX());
      int $$4 = QuartPos.fromBlock($$1.getY());
      int $$5 = QuartPos.fromBlock($$1.getZ());
      Climate.TargetPoint $$6 = $$2.sample($$3, $$4, $$5);
      float $$7 = Climate.unquantizeCoord($$6.continentalness());
      float $$8 = Climate.unquantizeCoord($$6.erosion());
      float $$9 = Climate.unquantizeCoord($$6.temperature());
      float $$10 = Climate.unquantizeCoord($$6.humidity());
      float $$11 = Climate.unquantizeCoord($$6.weirdness());
      double $$12 = NoiseRouterData.peaksAndValleys($$11);
      OverworldBiomeBuilder $$13 = new OverworldBiomeBuilder();
      $$0.add(
         "Biome builder PV: "
            + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys($$12)
            + " C: "
            + $$13.getDebugStringForContinentalness($$7)
            + " E: "
            + $$13.getDebugStringForErosion($$8)
            + " T: "
            + $$13.getDebugStringForTemperature($$9)
            + " H: "
            + $$13.getDebugStringForHumidity($$10)
      );
   }
}
