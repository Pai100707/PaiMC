package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public enum EmptyBlockGetter implements net.minecraft.world.level.BlockGetter {
   INSTANCE;

   
   @Override
   public BlockEntity getBlockEntity(BlockPos $$0) {
      return null;
   }

   @Override
   public BlockState getBlockState(BlockPos $$0) {
      return Blocks.AIR.defaultBlockState();
   }

   @Override
   public FluidState getFluidState(BlockPos $$0) {
      return Fluids.EMPTY.defaultFluidState();
   }

   @Override
   public int getMinY() {
      return 0;
   }

   @Override
   public int getHeight() {
      return 0;
   }
}
