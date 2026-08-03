package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.sounds.AmbientDesertBlockSoundsPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TallDryGrassBlock extends DryVegetationBlock implements BonemealableBlock {
   public static final MapCodec<TallDryGrassBlock> CODEC = simpleCodec(TallDryGrassBlock::new);
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);

   @Override
   public MapCodec<TallDryGrassBlock> codec() {
      return CODEC;
   }

   protected TallDryGrassBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      AmbientDesertBlockSoundsPlayer.playAmbientDryGrassSounds($$1, $$2, $$3);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return BonemealableBlock.hasSpreadableNeighbourPos($$0, $$1, Blocks.SHORT_DRY_GRASS.defaultBlockState());
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BonemealableBlock.findSpreadableNeighbourPos($$0, $$2, Blocks.SHORT_DRY_GRASS.defaultBlockState())
         .ifPresent($$1x -> $$0.setBlockAndUpdate($$1x, Blocks.SHORT_DRY_GRASS.defaultBlockState()));
   }
}
