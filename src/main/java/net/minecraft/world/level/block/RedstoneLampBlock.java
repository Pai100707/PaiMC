package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;

public class RedstoneLampBlock extends Block {
   public static final MapCodec<RedstoneLampBlock> CODEC = simpleCodec(RedstoneLampBlock::new);
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   @Override
   public MapCodec<RedstoneLampBlock> codec() {
      return CODEC;
   }

   public RedstoneLampBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(LIT, $$0.getLevel().hasNeighborSignal($$0.getClickedPos()));
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         boolean $$6 = $$0.getValue(LIT);
         if ($$6 != $$1.hasNeighborSignal($$2)) {
            if ($$6) {
               $$1.scheduleTick($$2, this, 4);
            } else {
               $$1.setBlock($$2, $$0.cycle(LIT), 2);
            }
         }
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LIT) && !$$1.hasNeighborSignal($$2)) {
         $$1.setBlock($$2, $$0.cycle(LIT), 2);
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LIT);
   }
}
