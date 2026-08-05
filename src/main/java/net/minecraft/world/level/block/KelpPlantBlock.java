package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;

public class KelpPlantBlock extends GrowingPlantBodyBlock implements LiquidBlockContainer {
   public static final MapCodec<KelpPlantBlock> CODEC = simpleCodec(KelpPlantBlock::new);

   @Override
   public MapCodec<KelpPlantBlock> codec() {
      return CODEC;
   }

   protected KelpPlantBlock(BlockBehaviour.Properties $$0) {
      super($$0, Direction.UP, Shapes.block(), true);
   }

   @Override
   protected GrowingPlantHeadBlock getHeadBlock() {
      return (GrowingPlantHeadBlock)Blocks.KELP;
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return Fluids.WATER.getSource(false);
   }

   @Override
   protected boolean canAttachTo(BlockState $$0) {
      return this.getHeadBlock().canAttachTo($$0);
   }

   @Override
   public boolean canPlaceLiquid(LivingEntity $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, Fluid $$4) {
      return false;
   }

   @Override
   public boolean placeLiquid(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
      return false;
   }
}
