package net.minecraft.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EmptyFluid extends Fluid {
   @Override
   public Item getBucket() {
      return Items.AIR;
   }

   @Override
   public boolean canBeReplacedWith(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Fluid $$3, Direction $$4) {
      return true;
   }

   @Override
   public Vec3 getFlow(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, FluidState $$2) {
      return Vec3.ZERO;
   }

   @Override
   public int getTickDelay(net.minecraft.world.level.LevelReader $$0) {
      return 0;
   }

   @Override
   protected boolean isEmpty() {
      return true;
   }

   @Override
   protected float getExplosionResistance() {
      return 0.0F;
   }

   @Override
   public float getHeight(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return 0.0F;
   }

   @Override
   public float getOwnHeight(FluidState $$0) {
      return 0.0F;
   }

   @Override
   protected BlockState createLegacyBlock(FluidState $$0) {
      return Blocks.AIR.defaultBlockState();
   }

   @Override
   public boolean isSource(FluidState $$0) {
      return false;
   }

   @Override
   public int getAmount(FluidState $$0) {
      return 0;
   }

   @Override
   public VoxelShape getShape(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return Shapes.empty();
   }
}
