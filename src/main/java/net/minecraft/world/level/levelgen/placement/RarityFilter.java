package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public class RarityFilter extends PlacementFilter {
   public static final MapCodec<RarityFilter> CODEC = ExtraCodecs.POSITIVE_INT.fieldOf("chance").xmap(RarityFilter::new, $$0 -> $$0.chance);
   private final int chance;

   private RarityFilter(int $$0) {
      this.chance = $$0;
   }

   public static RarityFilter onAverageOnceEvery(int $$0) {
      return new RarityFilter($$0);
   }

   @Override
   protected boolean shouldPlace(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      return $$1.nextFloat() < 1.0F / this.chance;
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.RARITY_FILTER;
   }
}
