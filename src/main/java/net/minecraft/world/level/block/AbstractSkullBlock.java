package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;

public abstract class AbstractSkullBlock extends BaseEntityBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private final SkullBlock.Type type;

   public AbstractSkullBlock(SkullBlock.Type $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.type = $$0;
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
   }

   @Override
   protected abstract MapCodec<? extends AbstractSkullBlock> codec();

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new SkullBlockEntity($$0, $$1);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      if ($$0.isClientSide()) {
         boolean $$3 = $$1.is(Blocks.DRAGON_HEAD) || $$1.is(Blocks.DRAGON_WALL_HEAD) || $$1.is(Blocks.PIGLIN_HEAD) || $$1.is(Blocks.PIGLIN_WALL_HEAD);
         if ($$3) {
            return createTickerHelper($$2, BlockEntityType.SKULL, SkullBlockEntity::animation);
         }
      }

      return null;
   }

   public SkullBlock.Type getType() {
      return this.type;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(POWERED);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(POWERED, $$0.getLevel().hasNeighborSignal($$0.getClickedPos()));
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         boolean $$6 = $$1.hasNeighborSignal($$2);
         if ($$6 != $$0.getValue(POWERED)) {
            $$1.setBlock($$2, $$0.setValue(POWERED, $$6), 2);
         }
      }
   }
}
