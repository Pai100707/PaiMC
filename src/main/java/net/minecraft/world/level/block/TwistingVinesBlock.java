package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TwistingVinesBlock extends GrowingPlantHeadBlock {
   public static final MapCodec<TwistingVinesBlock> CODEC = simpleCodec(TwistingVinesBlock::new);
   private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 15.0);

   @Override
   public MapCodec<TwistingVinesBlock> codec() {
      return CODEC;
   }

   public TwistingVinesBlock(BlockBehaviour.Properties $$0) {
      super($$0, Direction.UP, SHAPE, false, 0.1);
   }

   @Override
   protected int getBlocksToGrowWhenBonemealed(RandomSource $$0) {
      return NetherVines.getBlocksToGrowWhenBonemealed($$0);
   }

   @Override
   protected Block getBodyBlock() {
      return Blocks.TWISTING_VINES_PLANT;
   }

   @Override
   protected boolean canGrowInto(BlockState $$0) {
      return NetherVines.isValidGrowthState($$0);
   }
}
