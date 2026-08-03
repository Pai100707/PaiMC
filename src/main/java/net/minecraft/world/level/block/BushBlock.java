package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BushBlock extends VegetationBlock implements BonemealableBlock {
   public static final MapCodec<BushBlock> CODEC = simpleCodec(BushBlock::new);
   private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 13.0);

   @Override
   public MapCodec<BushBlock> codec() {
      return CODEC;
   }

   protected BushBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return BonemealableBlock.hasSpreadableNeighbourPos($$0, $$1, $$2);
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BonemealableBlock.findSpreadableNeighbourPos($$0, $$2, $$3).ifPresent($$1x -> $$0.setBlockAndUpdate($$1x, this.defaultBlockState()));
   }
}
