package net.minecraft.world.level.levelgen.placement;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class PlacementFilter extends PlacementModifier {
   @Override
   public final Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      return this.shouldPlace($$0, $$1, $$2) ? Stream.of($$2) : Stream.of();
   }

   protected abstract boolean shouldPlace(PlacementContext var1, RandomSource var2, BlockPos var3);
}
