package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SoulFireBlock extends BaseFireBlock {
   public static final MapCodec<SoulFireBlock> CODEC = simpleCodec(SoulFireBlock::new);

   @Override
   public MapCodec<SoulFireBlock> codec() {
      return CODEC;
   }

   public SoulFireBlock(BlockBehaviour.Properties $$0) {
      super($$0, 2.0F);
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      return this.canSurvive($$0, $$1, $$3) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return canSurviveOnBlock($$1.getBlockState($$2.below()));
   }

   public static boolean canSurviveOnBlock(BlockState $$0) {
      return $$0.is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
   }

   @Override
   protected boolean canBurn(BlockState $$0) {
      return true;
   }
}
