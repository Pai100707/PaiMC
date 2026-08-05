package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<SculkSensorBlock> CODEC = simpleCodec(SculkSensorBlock::new);
   public static final int ACTIVE_TICKS = 30;
   public static final int COOLDOWN_TICKS = 10;
   public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 8.0);
   private static final float[] RESONANCE_PITCH_BEND = (float[])Util.make(new float[16], $$0 -> {
      int[] $$1 = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

      for (int $$2 = 0; $$2 < 16; $$2++) {
         $$0[$$2] = NoteBlock.getPitchFromNote($$1[$$2]);
      }
   });

   @Override
   public MapCodec<? extends SculkSensorBlock> codec() {
      return CODEC;
   }

   public SculkSensorBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, 0).setValue(WATERLOGGED, false));
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockPos $$1 = $$0.getClickedPos();
      FluidState $$2 = $$0.getLevel().getFluidState($$1);
      return this.defaultBlockState().setValue(WATERLOGGED, $$2.getType() == Fluids.WATER);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (getPhase($$0) != SculkSensorPhase.ACTIVE) {
         if (getPhase($$0) == SculkSensorPhase.COOLDOWN) {
            $$1.setBlock($$2, $$0.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
            if (!$$0.getValue(WATERLOGGED)) {
               $$1.playSound(null, $$2, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, $$1.random.nextFloat() * 0.2F + 0.8F);
            }
         }
      } else {
         deactivate($$1, $$2, $$0);
      }
   }

   @Override
   public void stepOn(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Entity $$3) {
      if (!$$0.isClientSide()
         && canActivate($$2)
         && $$3.getType() != EntityType.WARDEN
         && $$0.getBlockEntity($$1) instanceof SculkSensorBlockEntity $$5
         && $$0 instanceof ServerLevel $$6
         && $$5.getVibrationUser().canReceiveVibration($$6, $$1, GameEvent.STEP, GameEvent.Context.of($$2))) {
         $$5.getListener().forceScheduleVibration($$6, GameEvent.STEP, GameEvent.Context.of($$3), $$3.position());
      }

      super.stepOn($$0, $$1, $$2, $$3);
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$1.isClientSide() && !$$0.is($$3.getBlock())) {
         if ($$0.getValue(POWER) > 0 && !$$1.getBlockTicks().hasScheduledTick($$2, this)) {
            $$1.setBlock($$2, $$0.setValue(POWER, 0), 18);
         }
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (getPhase($$0) == SculkSensorPhase.ACTIVE) {
         updateNeighbours($$1, $$2, $$0);
      }
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
      if ($$0.getValue(WATERLOGGED)) {
         $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   private static void updateNeighbours(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      Block $$3 = $$2.getBlock();
      $$0.updateNeighborsAt($$1, $$3);
      $$0.updateNeighborsAt($$1.below(), $$3);
   }

   
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new SculkSensorBlockEntity($$0, $$1);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return !$$0.isClientSide()
         ? createTickerHelper(
            $$2, BlockEntityType.SCULK_SENSOR, ($$0x, $$1x, $$2x, $$3) -> VibrationSystem.Ticker.tick($$0x, $$3.getVibrationData(), $$3.getVibrationUser())
         )
         : null;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(POWER);
   }

   @Override
   public int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$3 == Direction.UP ? $$0.getSignal($$1, $$2, $$3) : 0;
   }

   public static SculkSensorPhase getPhase(BlockState $$0) {
      return $$0.getValue(PHASE);
   }

   public static boolean canActivate(BlockState $$0) {
      return getPhase($$0) == SculkSensorPhase.INACTIVE;
   }

   public static void deactivate(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      $$0.setBlock($$1, $$2.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, 0), 3);
      $$0.scheduleTick($$1, $$2.getBlock(), 10);
      updateNeighbours($$0, $$1, $$2);
   }

   @VisibleForTesting
   public int getActiveTicks() {
      return 30;
   }

   public void activate(Entity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, int $$4, int $$5) {
      $$1.setBlock($$2, $$3.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, $$4), 3);
      $$1.scheduleTick($$2, $$3.getBlock(), this.getActiveTicks());
      updateNeighbours($$1, $$2, $$3);
      tryResonateVibration($$0, $$1, $$2, $$5);
      $$1.gameEvent($$0, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, $$2);
      if (!$$3.getValue(WATERLOGGED)) {
         $$1.playSound(
            null,
            $$2.getX() + 0.5,
            $$2.getY() + 0.5,
            $$2.getZ() + 0.5,
            SoundEvents.SCULK_CLICKING,
            SoundSource.BLOCKS,
            1.0F,
            $$1.random.nextFloat() * 0.2F + 0.8F
         );
      }
   }

   public static void tryResonateVibration(Entity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, int $$3) {
      for (Direction $$4 : Direction.values()) {
         BlockPos $$5 = $$2.relative($$4);
         BlockState $$6 = $$1.getBlockState($$5);
         if ($$6.is(BlockTags.VIBRATION_RESONATORS)) {
            $$1.gameEvent(VibrationSystem.getResonanceEventByFrequency($$3), $$5, GameEvent.Context.of($$0, $$6));
            float $$7 = RESONANCE_PITCH_BEND[$$3];
            $$1.playSound(null, $$5, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, $$7);
         }
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if (getPhase($$0) == SculkSensorPhase.ACTIVE) {
         Direction $$4 = Direction.getRandom($$3);
         if ($$4 != Direction.UP && $$4 != Direction.DOWN) {
            double $$5 = $$2.getX() + 0.5 + ($$4.getStepX() == 0 ? 0.5 - $$3.nextDouble() : $$4.getStepX() * 0.6);
            double $$6 = $$2.getY() + 0.25;
            double $$7 = $$2.getZ() + 0.5 + ($$4.getStepZ() == 0 ? 0.5 - $$3.nextDouble() : $$4.getStepZ() * 0.6);
            double $$8 = $$3.nextFloat() * 0.04;
            $$1.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, $$5, $$6, $$7, 0.0, $$8, 0.0);
         }
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(PHASE, POWER, WATERLOGGED);
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      if ($$1.getBlockEntity($$2) instanceof SculkSensorBlockEntity $$5) {
         return getPhase($$0) == SculkSensorPhase.ACTIVE ? $$5.getLastVibrationFrequency() : 0;
      } else {
         return 0;
      }
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return true;
   }

   @Override
   protected void spawnAfterBreak(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3, boolean $$4) {
      super.spawnAfterBreak($$0, $$1, $$2, $$3, $$4);
      if ($$4) {
         this.tryDropExperience($$1, $$2, $$3, ConstantInt.of(5));
      }
   }
}
