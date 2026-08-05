package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public enum EmptyBlockAndTintGetter implements net.minecraft.world.level.BlockAndTintGetter {
   INSTANCE;

   @Override
   public float getShade(Direction $$0, boolean $$1) {
      return 1.0F;
   }

   @Override
   public LevelLightEngine getLightEngine() {
      return LevelLightEngine.EMPTY;
   }

   @Override
   public int getBlockTint(BlockPos $$0, net.minecraft.world.level.ColorResolver $$1) {
      return -1;
   }

   
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
   public int getHeight() {
      return 0;
   }

   @Override
   public int getMinY() {
      return 0;
   }
}
