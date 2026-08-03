package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.sounds.AmbientDesertBlockSoundsPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DryVegetationBlock extends VegetationBlock {
   public static final MapCodec<DryVegetationBlock> CODEC = simpleCodec(DryVegetationBlock::new);
   private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);

   @Override
   public MapCodec<? extends DryVegetationBlock> codec() {
      return CODEC;
   }

   protected DryVegetationBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.is(BlockTags.DRY_VEGETATION_MAY_PLACE_ON);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      AmbientDesertBlockSoundsPlayer.playAmbientDeadBushSounds($$1, $$2, $$3);
   }
}
