package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LeavesBlock extends Block implements SimpleWaterloggedBlock {
   public static final int DECAY_DISTANCE = 7;
   public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
   public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected final float leafParticleChance;
   private static final int TICK_DELAY = 1;
   private static boolean cutoutLeaves = true;

   @Override
   public abstract MapCodec<? extends LeavesBlock> codec();

   public LeavesBlock(float $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.leafParticleChance = $$0;
      this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, 7).setValue(PERSISTENT, false).setValue(WATERLOGGED, false));
   }

   @Override
   protected boolean skipRendering(BlockState $$0, BlockState $$1, Direction $$2) {
      return !cutoutLeaves && $$1.getBlock() instanceof LeavesBlock ? true : super.skipRendering($$0, $$1, $$2);
   }

   public static void setCutoutLeaves(boolean $$0) {
      cutoutLeaves = $$0;
   }

   @Override
   protected VoxelShape getBlockSupportShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return Shapes.empty();
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.getValue(DISTANCE) == 7 && !$$0.getValue(PERSISTENT);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (this.decaying($$0)) {
         dropResources($$0, $$1, $$2);
         $$1.removeBlock($$2, false);
      }
   }

   protected boolean decaying(BlockState $$0) {
      return !$$0.getValue(PERSISTENT) && $$0.getValue(DISTANCE) == 7;
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      $$1.setBlock($$2, updateDistance($$0, $$1, $$2), 3);
   }

   @Override
   protected int getLightBlock(BlockState $$0) {
      return 1;
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

      int $$8 = getDistanceAt($$6) + 1;
      if ($$8 != 1 || $$0.getValue(DISTANCE) != $$8) {
         $$2.scheduleTick($$3, this, 1);
      }

      return $$0;
   }

   private static BlockState updateDistance(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
      int $$3 = 7;
      MutableBlockPos $$4 = new MutableBlockPos();

      for (Direction $$5 : Direction.values()) {
         $$4.setWithOffset($$2, $$5);
         $$3 = Math.min($$3, getDistanceAt($$1.getBlockState($$4)) + 1);
         if ($$3 == 1) {
            break;
         }
      }

      return $$0.setValue(DISTANCE, $$3);
   }

   private static int getDistanceAt(BlockState $$0) {
      return getOptionalDistanceAt($$0).orElse(7);
   }

   public static OptionalInt getOptionalDistanceAt(BlockState $$0) {
      if ($$0.is(BlockTags.LOGS)) {
         return OptionalInt.of(0);
      } else {
         return $$0.hasProperty(DISTANCE) ? OptionalInt.of($$0.getValue(DISTANCE)) : OptionalInt.empty();
      }
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      super.animateTick($$0, $$1, $$2, $$3);
      BlockPos $$4 = $$2.below();
      BlockState $$5 = $$1.getBlockState($$4);
      makeDrippingWaterParticles($$1, $$2, $$3, $$5, $$4);
      this.makeFallingLeavesParticles($$1, $$2, $$3, $$5, $$4);
   }

   private static void makeDrippingWaterParticles(net.minecraft.world.level.Level $$0, BlockPos $$1, RandomSource $$2, BlockState $$3, BlockPos $$4) {
      if ($$0.isRainingAt($$1.above())) {
         if ($$2.nextInt(15) == 1) {
            if (!$$3.canOcclude() || !$$3.isFaceSturdy($$0, $$4, Direction.UP)) {
               ParticleUtils.spawnParticleBelow($$0, $$1, $$2, ParticleTypes.DRIPPING_WATER);
            }
         }
      }
   }

   private void makeFallingLeavesParticles(net.minecraft.world.level.Level $$0, BlockPos $$1, RandomSource $$2, BlockState $$3, BlockPos $$4) {
      if (!($$2.nextFloat() >= this.leafParticleChance)) {
         if (!isFaceFull($$3.getCollisionShape($$0, $$4), Direction.UP)) {
            this.spawnFallingLeavesParticle($$0, $$1, $$2);
         }
      }
   }

   protected abstract void spawnFallingLeavesParticle(net.minecraft.world.level.Level var1, BlockPos var2, RandomSource var3);

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(DISTANCE, PERSISTENT, WATERLOGGED);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      BlockState $$2 = this.defaultBlockState().setValue(PERSISTENT, true).setValue(WATERLOGGED, $$1.getType() == Fluids.WATER);
      return updateDistance($$2, $$0.getLevel(), $$0.getClickedPos());
   }
}
