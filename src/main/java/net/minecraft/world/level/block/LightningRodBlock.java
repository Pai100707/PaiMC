package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;

public class LightningRodBlock extends RodBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<LightningRodBlock> CODEC = simpleCodec(LightningRodBlock::new);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final int ACTIVATION_TICKS = 8;
   public static final int RANGE = 128;
   private static final int SPARK_CYCLE = 200;

   @Override
   public MapCodec<? extends LightningRodBlock> codec() {
      return CODEC;
   }

   public LightningRodBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, false).setValue(POWERED, false));
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      boolean $$2 = $$1.getType() == Fluids.WATER;
      return this.defaultBlockState().setValue(FACING, $$0.getClickedFace()).setValue(WATERLOGGED, $$2);
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

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(POWERED) ? 15 : 0;
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(POWERED) && $$0.getValue(FACING) == $$3 ? 15 : 0;
   }

   public void onLightningStrike(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      $$1.setBlock($$2, $$0.setValue(POWERED, true), 3);
      this.updateNeighbours($$0, $$1, $$2);
      $$1.scheduleTick($$2, this, 8);
      $$1.levelEvent(3002, $$2, ((Direction)$$0.getValue(FACING)).getAxis().ordinal());
   }

   private void updateNeighbours(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      Direction $$3 = ((Direction)$$0.getValue(FACING)).getOpposite();
      $$1.updateNeighborsAt($$2.relative($$3), this, ExperimentalRedstoneUtils.initialOrientation($$1, $$3, null));
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      $$1.setBlock($$2, $$0.setValue(POWERED, false), 3);
      this.updateNeighbours($$0, $$1, $$2);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.isThundering()
         && $$1.random.nextInt(200) <= $$1.getGameTime() % 200L
         && $$2.getY() == $$1.getHeight(Heightmap.Types.WORLD_SURFACE, $$2.getX(), $$2.getZ()) - 1) {
         ParticleUtils.spawnParticlesAlongAxis(((Direction)$$0.getValue(FACING)).getAxis(), $$1, $$2, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(1, 2));
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if ($$0.getValue(POWERED)) {
         this.updateNeighbours($$0, $$1, $$2);
      }
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$0.is($$3.getBlock())) {
         if ($$0.getValue(POWERED) && !$$1.getBlockTicks().hasScheduledTick($$2, this)) {
            $$1.scheduleTick($$2, this, 8);
         }
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, POWERED, WATERLOGGED);
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }
}
