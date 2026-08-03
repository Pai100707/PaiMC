package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealableBlock {
   boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader var1, BlockPos var2, BlockState var3);

   boolean isBonemealSuccess(net.minecraft.world.level.Level var1, RandomSource var2, BlockPos var3, BlockState var4);

   void performBonemeal(ServerLevel var1, RandomSource var2, BlockPos var3, BlockState var4);

   static boolean hasSpreadableNeighbourPos(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return getSpreadableNeighbourPos(Plane.HORIZONTAL.stream().toList(), $$0, $$1, $$2).isPresent();
   }

   static Optional<BlockPos> findSpreadableNeighbourPos(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      return getSpreadableNeighbourPos(Plane.HORIZONTAL.shuffledCopy($$0.random), $$0, $$1, $$2);
   }

   private static Optional<BlockPos> getSpreadableNeighbourPos(List<Direction> $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2, BlockState $$3) {
      for (Direction $$4 : $$0) {
         BlockPos $$5 = $$2.relative($$4);
         if ($$1.isEmptyBlock($$5) && $$3.canSurvive($$1, $$5)) {
            return Optional.of($$5);
         }
      }

      return Optional.empty();
   }

   default BlockPos getParticlePos(BlockPos $$0) {
      return switch (this.getType()) {
         case NEIGHBOR_SPREADER -> $$0.above();
         case GROWER -> $$0;
      };
   }

   default BonemealableBlock.Type getType() {
      return BonemealableBlock.Type.GROWER;
   }

   public static enum Type {
      NEIGHBOR_SPREADER,
      GROWER;
   }
}
