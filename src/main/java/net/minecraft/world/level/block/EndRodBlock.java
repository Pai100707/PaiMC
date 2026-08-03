package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class EndRodBlock extends RodBlock {
   public static final MapCodec<EndRodBlock> CODEC = simpleCodec(EndRodBlock::new);

   @Override
   public MapCodec<EndRodBlock> codec() {
      return CODEC;
   }

   protected EndRodBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      Direction $$1 = $$0.getClickedFace();
      BlockState $$2 = $$0.getLevel().getBlockState($$0.getClickedPos().relative($$1.getOpposite()));
      return $$2.is(this) && $$2.getValue(FACING) == $$1
         ? this.defaultBlockState().setValue(FACING, $$1.getOpposite())
         : this.defaultBlockState().setValue(FACING, $$1);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      Direction $$4 = $$0.getValue(FACING);
      double $$5 = $$2.getX() + 0.55 - $$3.nextFloat() * 0.1F;
      double $$6 = $$2.getY() + 0.55 - $$3.nextFloat() * 0.1F;
      double $$7 = $$2.getZ() + 0.55 - $$3.nextFloat() * 0.1F;
      double $$8 = 0.4F - ($$3.nextFloat() + $$3.nextFloat()) * 0.4F;
      if ($$3.nextInt(5) == 0) {
         $$1.addParticle(
            ParticleTypes.END_ROD,
            $$5 + $$4.getStepX() * $$8,
            $$6 + $$4.getStepY() * $$8,
            $$7 + $$4.getStepZ() * $$8,
            $$3.nextGaussian() * 0.005,
            $$3.nextGaussian() * 0.005,
            $$3.nextGaussian() * 0.005
         );
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
   }
}
