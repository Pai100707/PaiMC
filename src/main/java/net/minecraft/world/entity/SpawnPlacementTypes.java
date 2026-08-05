package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public interface SpawnPlacementTypes {
   net.minecraft.world.entity.SpawnPlacementType NO_RESTRICTIONS = ($$0, $$1, $$2) -> true;
   net.minecraft.world.entity.SpawnPlacementType IN_WATER = ($$0, $$1, $$2) -> {
      if ($$2 != null && $$0.getWorldBorder().isWithinBounds($$1)) {
         BlockPos $$3 = $$1.above();
         return $$0.getFluidState($$1).is(FluidTags.WATER) && !$$0.getBlockState($$3).isRedstoneConductor($$0, $$3);
      } else {
         return false;
      }
   };
   net.minecraft.world.entity.SpawnPlacementType IN_LAVA = ($$0, $$1, $$2) -> $$2 != null && $$0.getWorldBorder().isWithinBounds($$1)
      ? $$0.getFluidState($$1).is(FluidTags.LAVA)
      : false;
   net.minecraft.world.entity.SpawnPlacementType ON_GROUND = new net.minecraft.world.entity.SpawnPlacementType() {
      @Override
      public boolean isSpawnPositionOk(LevelReader $$0, BlockPos $$1, net.minecraft.world.entity.EntityType<?> $$2) {
         if ($$2 != null && $$0.getWorldBorder().isWithinBounds($$1)) {
            BlockPos $$3 = $$1.above();
            BlockPos $$4 = $$1.below();
            BlockState $$5 = $$0.getBlockState($$4);
            return !$$5.isValidSpawn($$0, $$4, $$2) ? false : this.isValidEmptySpawnBlock($$0, $$1, $$2) && this.isValidEmptySpawnBlock($$0, $$3, $$2);
         } else {
            return false;
         }
      }

      private boolean isValidEmptySpawnBlock(LevelReader $$0, BlockPos $$1, net.minecraft.world.entity.EntityType<?> $$2) {
         BlockState $$3 = $$0.getBlockState($$1);
         return NaturalSpawner.isValidEmptySpawnBlock($$0, $$1, $$3, $$3.getFluidState(), $$2);
      }

      @Override
      public BlockPos adjustSpawnPosition(LevelReader $$0, BlockPos $$1) {
         BlockPos $$2 = $$1.below();
         return $$0.getBlockState($$2).isPathfindable(PathComputationType.LAND) ? $$2 : $$1;
      }
   };
}
