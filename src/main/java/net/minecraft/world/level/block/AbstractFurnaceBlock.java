package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public abstract class AbstractFurnaceBlock extends BaseEntityBlock {
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   protected AbstractFurnaceBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
   }

   @Override
   protected abstract MapCodec<? extends AbstractFurnaceBlock> codec();

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide()) {
         this.openContainer($$1, $$2, $$3);
      }

      return InteractionResult.SUCCESS;
   }

   protected abstract void openContainer(net.minecraft.world.level.Level var1, BlockPos var2, Player var3);

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity($$1.getBlockEntity($$2));
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, LIT);
   }

   @Nullable
   protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(
      net.minecraft.world.level.Level $$0, BlockEntityType<T> $$1, BlockEntityType<? extends AbstractFurnaceBlockEntity> $$2
   ) {
      return $$0 instanceof ServerLevel $$3
         ? createTickerHelper($$1, $$2, ($$1x, $$2x, $$3x, $$4) -> AbstractFurnaceBlockEntity.serverTick($$3, $$2x, $$3x, $$4))
         : null;
   }
}
