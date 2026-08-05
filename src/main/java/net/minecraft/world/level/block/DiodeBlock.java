package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

public abstract class DiodeBlock extends HorizontalDirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 2.0);

   protected DiodeBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected abstract MapCodec<? extends DiodeBlock> codec();

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      return this.canSurviveOn($$1, $$3, $$1.getBlockState($$3));
   }

   protected boolean canSurviveOn(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return $$2.isFaceSturdy($$0, $$1, Direction.UP, SupportType.RIGID);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!this.isLocked($$1, $$2, $$0)) {
         boolean $$4 = $$0.getValue(POWERED);
         boolean $$5 = this.shouldTurnOn($$1, $$2, $$0);
         if ($$4 && !$$5) {
            $$1.setBlock($$2, $$0.setValue(POWERED, false), 2);
         } else if (!$$4) {
            $$1.setBlock($$2, $$0.setValue(POWERED, true), 2);
            if (!$$5) {
               $$1.scheduleTick($$2, this, this.getDelay($$0), TickPriority.VERY_HIGH);
            }
         }
      }
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getSignal($$1, $$2, $$3);
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      if (!$$0.getValue(POWERED)) {
         return 0;
      } else {
         return $$0.getValue(FACING) == $$3 ? this.getOutputSignal($$1, $$2, $$0) : 0;
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if ($$0.canSurvive($$1, $$2)) {
         this.checkTickOnNeighbor($$1, $$2, $$0);
      } else {
         BlockEntity $$6 = $$0.hasBlockEntity() ? $$1.getBlockEntity($$2) : null;
         dropResources($$0, $$1, $$2, $$6);
         $$1.removeBlock($$2, false);

         for (Direction $$7 : Direction.values()) {
            $$1.updateNeighborsAt($$2.relative($$7), this);
         }
      }
   }

   protected void checkTickOnNeighbor(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      if (!this.isLocked($$0, $$1, $$2)) {
         boolean $$3 = $$2.getValue(POWERED);
         boolean $$4 = this.shouldTurnOn($$0, $$1, $$2);
         if ($$3 != $$4 && !$$0.getBlockTicks().willTickThisTick($$1, this)) {
            TickPriority $$5 = TickPriority.HIGH;
            if (this.shouldPrioritize($$0, $$1, $$2)) {
               $$5 = TickPriority.EXTREMELY_HIGH;
            } else if ($$3) {
               $$5 = TickPriority.VERY_HIGH;
            }

            $$0.scheduleTick($$1, this, this.getDelay($$2), $$5);
         }
      }
   }

   public boolean isLocked(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return false;
   }

   protected boolean shouldTurnOn(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      return this.getInputSignal($$0, $$1, $$2) > 0;
   }

   protected int getInputSignal(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      Direction $$3 = $$2.getValue(FACING);
      BlockPos $$4 = $$1.relative($$3);
      int $$5 = $$0.getSignal($$4, $$3);
      if ($$5 >= 15) {
         return $$5;
      } else {
         BlockState $$6 = $$0.getBlockState($$4);
         return Math.max($$5, $$6.is(Blocks.REDSTONE_WIRE) ? $$6.getValue(RedStoneWireBlock.POWER) : 0);
      }
   }

   protected int getAlternateSignal(net.minecraft.world.level.SignalGetter $$0, BlockPos $$1, BlockState $$2) {
      Direction $$3 = $$2.getValue(FACING);
      Direction $$4 = $$3.getClockWise();
      Direction $$5 = $$3.getCounterClockWise();
      boolean $$6 = this.sideInputDiodesOnly();
      return Math.max($$0.getControlInputSignal($$1.relative($$4), $$4, $$6), $$0.getControlInputSignal($$1.relative($$5), $$5, $$6));
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite());
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, LivingEntity $$3, ItemStack $$4) {
      if (this.shouldTurnOn($$0, $$1, $$2)) {
         $$0.scheduleTick($$1, this, 1);
      }
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      this.updateNeighborsInFront($$1, $$2, $$0);
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (!$$3) {
         this.updateNeighborsInFront($$1, $$2, $$0);
      }
   }

   protected void updateNeighborsInFront(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      Direction $$3 = $$2.getValue(FACING);
      BlockPos $$4 = $$1.relative($$3.getOpposite());
      Orientation $$5 = ExperimentalRedstoneUtils.initialOrientation($$0, $$3.getOpposite(), Direction.UP);
      $$0.neighborChanged($$4, this, $$5);
      $$0.updateNeighborsAtExceptFromFacing($$4, this, $$3, $$5);
   }

   protected boolean sideInputDiodesOnly() {
      return false;
   }

   protected int getOutputSignal(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      return 15;
   }

   public static boolean isDiode(BlockState $$0) {
      return $$0.getBlock() instanceof DiodeBlock;
   }

   public boolean shouldPrioritize(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      Direction $$3 = ((Direction)$$2.getValue(FACING)).getOpposite();
      BlockState $$4 = $$0.getBlockState($$1.relative($$3));
      return isDiode($$4) && $$4.getValue(FACING) != $$3;
   }

   protected abstract int getDelay(BlockState var1);
}
