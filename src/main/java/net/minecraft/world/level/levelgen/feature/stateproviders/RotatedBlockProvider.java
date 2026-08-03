package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RotatedBlockProvider extends BlockStateProvider {
   public static final MapCodec<RotatedBlockProvider> CODEC = BlockState.CODEC
      .fieldOf("state")
      .xmap(BlockBehaviour.BlockStateBase::getBlock, Block::defaultBlockState)
      .xmap(RotatedBlockProvider::new, $$0 -> $$0.block);
   private final Block block;

   public RotatedBlockProvider(Block $$0) {
      this.block = $$0;
   }

   @Override
   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.ROTATED_BLOCK_PROVIDER;
   }

   @Override
   public BlockState getState(RandomSource $$0, BlockPos $$1) {
      Axis $$2 = Axis.getRandom($$0);
      return this.block.defaultBlockState().trySetValue(RotatedPillarBlock.AXIS, $$2);
   }
}
