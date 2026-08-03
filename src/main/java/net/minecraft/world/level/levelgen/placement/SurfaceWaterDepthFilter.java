package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceWaterDepthFilter extends PlacementFilter {
   public static final MapCodec<SurfaceWaterDepthFilter> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.INT.fieldOf("max_water_depth").forGetter($$0x -> $$0x.maxWaterDepth)).apply($$0, SurfaceWaterDepthFilter::new)
   );
   private final int maxWaterDepth;

   private SurfaceWaterDepthFilter(int $$0) {
      this.maxWaterDepth = $$0;
   }

   public static SurfaceWaterDepthFilter forMaxDepth(int $$0) {
      return new SurfaceWaterDepthFilter($$0);
   }

   @Override
   protected boolean shouldPlace(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      int $$3 = $$0.getHeight(Heightmap.Types.OCEAN_FLOOR, $$2.getX(), $$2.getZ());
      int $$4 = $$0.getHeight(Heightmap.Types.WORLD_SURFACE, $$2.getX(), $$2.getZ());
      return $$4 - $$3 <= this.maxWaterDepth;
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.SURFACE_WATER_DEPTH_FILTER;
   }
}
