package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

public class ComparatorBlock extends DiodeBlock implements EntityBlock {
   public static final MapCodec<ComparatorBlock> CODEC = simpleCodec(ComparatorBlock::new);
   public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

   @Override
   public MapCodec<ComparatorBlock> codec() {
      return CODEC;
   }

   public ComparatorBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(MODE, ComparatorMode.COMPARE));
   }

   @Override
   protected int getDelay(BlockState $$0) {
      return 2;
   }

   @Override
   public BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      return $$4 == Direction.DOWN && !this.canSurviveOn($$1, $$5, $$6)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected int getOutputSignal(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      BlockEntity $$3 = $$0.getBlockEntity($$1);
      return $$3 instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)$$3).getOutputSignal() : 0;
   }

   private int calculateOutputSignal(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      int $$3 = this.getInputSignal($$0, $$1, $$2);
      if ($$3 == 0) {
         return 0;
      } else {
         int $$4 = this.getAlternateSignal($$0, $$1, $$2);
         if ($$4 > $$3) {
            return 0;
         } else {
            return $$2.getValue(MODE) == ComparatorMode.SUBTRACT ? $$3 - $$4 : $$3;
         }
      }
   }

   @Override
   protected boolean shouldTurnOn(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      int $$3 = this.getInputSignal($$0, $$1, $$2);
      if ($$3 == 0) {
         return false;
      } else {
         int $$4 = this.getAlternateSignal($$0, $$1, $$2);
         return $$3 > $$4 ? true : $$3 == $$4 && $$2.getValue(MODE) == ComparatorMode.COMPARE;
      }
   }

   @Override
   protected int getInputSignal(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      int $$3 = super.getInputSignal($$0, $$1, $$2);
      Direction $$4 = $$2.getValue(FACING);
      BlockPos $$5 = $$1.relative($$4);
      BlockState $$6 = $$0.getBlockState($$5);
      if ($$6.hasAnalogOutputSignal()) {
         $$3 = $$6.getAnalogOutputSignal($$0, $$5, $$4.getOpposite());
      } else if ($$3 < 15 && $$6.isRedstoneConductor($$0, $$5)) {
         $$5 = $$5.relative($$4);
         $$6 = $$0.getBlockState($$5);
         ItemFrame $$7 = this.getItemFrame($$0, $$4, $$5);
         int $$8 = Math.max(
            $$7 == null ? Integer.MIN_VALUE : $$7.getAnalogOutput(),
            $$6.hasAnalogOutputSignal() ? $$6.getAnalogOutputSignal($$0, $$5, $$4.getOpposite()) : Integer.MIN_VALUE
         );
         if ($$8 != Integer.MIN_VALUE) {
            $$3 = $$8;
         }
      }

      return $$3;
   }

   
   private ItemFrame getItemFrame(net.minecraft.world.level.Level $$0, Direction $$1, BlockPos $$2) {
      List<ItemFrame> $$3 = $$0.getEntitiesOfClass(
         ItemFrame.class, new AABB($$2.getX(), $$2.getY(), $$2.getZ(), $$2.getX() + 1, $$2.getY() + 1, $$2.getZ() + 1), $$1x -> $$1x.getDirection() == $$1
      );
      return $$3.size() == 1 ? $$3.get(0) : null;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$3.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else {
         $$0 = $$0.cycle(MODE);
         float $$5 = $$0.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F;
         $$1.playSound($$3, $$2, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, $$5);
         $$1.setBlock($$2, $$0, 2);
         this.refreshOutputState($$1, $$2, $$0);
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   protected void checkTickOnNeighbor(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      if (!$$0.getBlockTicks().willTickThisTick($$1, this)) {
         int $$3 = this.calculateOutputSignal($$0, $$1, $$2);
         BlockEntity $$4 = $$0.getBlockEntity($$1);
         int $$5 = $$4 instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)$$4).getOutputSignal() : 0;
         if ($$3 != $$5 || $$2.getValue(POWERED) != this.shouldTurnOn($$0, $$1, $$2)) {
            TickPriority $$6 = this.shouldPrioritize($$0, $$1, $$2) ? TickPriority.HIGH : TickPriority.NORMAL;
            $$0.scheduleTick($$1, this, 2, $$6);
         }
      }
   }

   private void refreshOutputState(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      int $$3 = this.calculateOutputSignal($$0, $$1, $$2);
      BlockEntity $$4 = $$0.getBlockEntity($$1);
      int $$5 = 0;
      if ($$4 instanceof ComparatorBlockEntity $$6) {
         $$5 = $$6.getOutputSignal();
         $$6.setOutputSignal($$3);
      }

      if ($$5 != $$3 || $$2.getValue(MODE) == ComparatorMode.COMPARE) {
         boolean $$7 = this.shouldTurnOn($$0, $$1, $$2);
         boolean $$8 = $$2.getValue(POWERED);
         if ($$8 && !$$7) {
            $$0.setBlock($$1, $$2.setValue(POWERED, false), 2);
         } else if (!$$8 && $$7) {
            $$0.setBlock($$1, $$2.setValue(POWERED, true), 2);
         }

         this.updateNeighborsInFront($$0, $$1, $$2);
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      this.refreshOutputState($$1, $$2, $$0);
   }

   @Override
   protected boolean triggerEvent(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, int $$3, int $$4) {
      super.triggerEvent($$0, $$1, $$2, $$3, $$4);
      BlockEntity $$5 = $$1.getBlockEntity($$2);
      return $$5 != null && $$5.triggerEvent($$3, $$4);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new ComparatorBlockEntity($$0, $$1);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, MODE, POWERED);
   }
}
