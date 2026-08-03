package net.minecraft.world.level.levelgen.placement;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class RepeatingPlacement extends PlacementModifier {
   protected abstract int count(RandomSource var1, BlockPos var2);

   @Override
   public Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      return IntStream.range(0, this.count($$1, $$2)).mapToObj($$1x -> $$2);
   }
}
