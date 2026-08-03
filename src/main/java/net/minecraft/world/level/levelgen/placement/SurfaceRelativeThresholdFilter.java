package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceRelativeThresholdFilter extends PlacementFilter {
   public static final MapCodec<SurfaceRelativeThresholdFilter> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Heightmap.Types.CODEC.fieldOf("heightmap").forGetter($$0x -> $$0x.heightmap),
            Codec.INT.optionalFieldOf("min_inclusive", Integer.MIN_VALUE).forGetter($$0x -> $$0x.minInclusive),
            Codec.INT.optionalFieldOf("max_inclusive", Integer.MAX_VALUE).forGetter($$0x -> $$0x.maxInclusive)
         )
         .apply($$0, SurfaceRelativeThresholdFilter::new)
   );
   private final Heightmap.Types heightmap;
   private final int minInclusive;
   private final int maxInclusive;

   private SurfaceRelativeThresholdFilter(Heightmap.Types $$0, int $$1, int $$2) {
      this.heightmap = $$0;
      this.minInclusive = $$1;
      this.maxInclusive = $$2;
   }

   public static SurfaceRelativeThresholdFilter of(Heightmap.Types $$0, int $$1, int $$2) {
      return new SurfaceRelativeThresholdFilter($$0, $$1, $$2);
   }

   @Override
   protected boolean shouldPlace(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      long $$3 = $$0.getHeight(this.heightmap, $$2.getX(), $$2.getZ());
      long $$4 = $$3 + this.minInclusive;
      long $$5 = $$3 + this.maxInclusive;
      return $$4 <= $$2.getY() && $$2.getY() <= $$5;
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
   }
}
