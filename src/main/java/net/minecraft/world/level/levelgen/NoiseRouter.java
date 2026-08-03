package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouter(
   DensityFunction barrierNoise,
   DensityFunction fluidLevelFloodednessNoise,
   DensityFunction fluidLevelSpreadNoise,
   DensityFunction lavaNoise,
   DensityFunction temperature,
   DensityFunction vegetation,
   DensityFunction continents,
   DensityFunction erosion,
   DensityFunction depth,
   DensityFunction ridges,
   DensityFunction preliminarySurfaceLevel,
   DensityFunction finalDensity,
   DensityFunction veinToggle,
   DensityFunction veinRidged,
   DensityFunction veinGap
) {
   public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            field("barrier", NoiseRouter::barrierNoise),
            field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise),
            field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise),
            field("lava", NoiseRouter::lavaNoise),
            field("temperature", NoiseRouter::temperature),
            field("vegetation", NoiseRouter::vegetation),
            field("continents", NoiseRouter::continents),
            field("erosion", NoiseRouter::erosion),
            field("depth", NoiseRouter::depth),
            field("ridges", NoiseRouter::ridges),
            field("preliminary_surface_level", NoiseRouter::preliminarySurfaceLevel),
            field("final_density", NoiseRouter::finalDensity),
            field("vein_toggle", NoiseRouter::veinToggle),
            field("vein_ridged", NoiseRouter::veinRidged),
            field("vein_gap", NoiseRouter::veinGap)
         )
         .apply($$0, NoiseRouter::new)
   );

   private static RecordCodecBuilder<NoiseRouter, DensityFunction> field(String $$0, Function<NoiseRouter, DensityFunction> $$1) {
      return DensityFunction.HOLDER_HELPER_CODEC.fieldOf($$0).forGetter($$1);
   }

   public NoiseRouter mapAll(DensityFunction.Visitor $$0) {
      return new NoiseRouter(
         this.barrierNoise.mapAll($$0),
         this.fluidLevelFloodednessNoise.mapAll($$0),
         this.fluidLevelSpreadNoise.mapAll($$0),
         this.lavaNoise.mapAll($$0),
         this.temperature.mapAll($$0),
         this.vegetation.mapAll($$0),
         this.continents.mapAll($$0),
         this.erosion.mapAll($$0),
         this.depth.mapAll($$0),
         this.ridges.mapAll($$0),
         this.preliminarySurfaceLevel.mapAll($$0),
         this.finalDensity.mapAll($$0),
         this.veinToggle.mapAll($$0),
         this.veinRidged.mapAll($$0),
         this.veinGap.mapAll($$0)
      );
   }
}
