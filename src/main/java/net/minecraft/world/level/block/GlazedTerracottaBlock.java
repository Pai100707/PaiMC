package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class GlazedTerracottaBlock extends HorizontalDirectionalBlock {
   public static final MapCodec<GlazedTerracottaBlock> CODEC = simpleCodec(GlazedTerracottaBlock::new);

   @Override
   public MapCodec<GlazedTerracottaBlock> codec() {
      return CODEC;
   }

   public GlazedTerracottaBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite());
   }
}
