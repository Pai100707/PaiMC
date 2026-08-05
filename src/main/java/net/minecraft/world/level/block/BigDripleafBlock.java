package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   public static final MapCodec<BigDripleafBlock> CODEC = simpleCodec(BigDripleafBlock::new);
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
   private static final int NO_TICK = -1;
   private static final Object2IntMap<Tilt> DELAY_UNTIL_NEXT_TILT_STATE = (Object2IntMap<Tilt>)Util.make(new Object2IntArrayMap(), $$0 -> {
      $$0.defaultReturnValue(-1);
      $$0.put(Tilt.UNSTABLE, 10);
      $$0.put(Tilt.PARTIAL, 10);
      $$0.put(Tilt.FULL, 100);
   });
   private static final int MAX_GEN_HEIGHT = 5;
   private static final int ENTITY_DETECTION_MIN_Y = 11;
   private static final int LOWEST_LEAF_TOP = 13;
   private static final Map<Tilt, VoxelShape> SHAPE_LEAF = Maps.newEnumMap(
      Map.of(
         Tilt.NONE,
         Block.column(16.0, 11.0, 15.0),
         Tilt.UNSTABLE,
         Block.column(16.0, 11.0, 15.0),
         Tilt.PARTIAL,
         Block.column(16.0, 11.0, 13.0),
         Tilt.FULL,
         Shapes.empty()
      )
   );
   private final Function<BlockState, VoxelShape> shapes;

   @Override
   public MapCodec<BigDripleafBlock> codec() {
      return CODEC;
   }

   protected BigDripleafBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH).setValue(TILT, Tilt.NONE));
      this.shapes = this.makeShapes();
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      Map<Direction, VoxelShape> $$0 = Shapes.rotateHorizontal(Block.column(6.0, 0.0, 13.0).move(0.0, 0.0, 0.25).optimize());
      return this.getShapeForEachState($$1 -> Shapes.or(SHAPE_LEAF.get($$1.getValue(TILT)), $$0.get($$1.getValue(FACING))), new Property[]{WATERLOGGED});
   }

   public static void placeWithRandomHeight(net.minecraft.world.level.LevelAccessor $$0, RandomSource $$1, BlockPos $$2, Direction $$3) {
      int $$4 = Mth.nextInt($$1, 2, 5);
      MutableBlockPos $$5 = $$2.mutable();
      int $$6 = 0;

      while ($$6 < $$4 && canPlaceAt($$0, $$5, $$0.getBlockState($$5))) {
         $$6++;
         $$5.move(Direction.UP);
      }

      int $$7 = $$2.getY() + $$6 - 1;
      $$5.setY($$2.getY());

      while ($$5.getY() < $$7) {
         BigDripleafStemBlock.place($$0, $$5, $$0.getFluidState($$5), $$3);
         $$5.move(Direction.UP);
      }

      place($$0, $$5, $$0.getFluidState($$5), $$3);
   }

   private static boolean canReplace(BlockState $$0) {
      return $$0.isAir() || $$0.is(Blocks.WATER) || $$0.is(Blocks.SMALL_DRIPLEAF);
   }

   protected static boolean canPlaceAt(net.minecraft.world.level.LevelHeightAccessor $$0, BlockPos $$1, BlockState $$2) {
      return !$$0.isOutsideBuildHeight($$1) && canReplace($$2);
   }

   protected static boolean place(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, FluidState $$2, Direction $$3) {
      BlockState $$4 = Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(WATERLOGGED, $$2.isSourceOfType(Fluids.WATER)).setValue(FACING, $$3);
      return $$0.setBlock($$1, $$4, 3);
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      this.setTiltAndScheduleTick($$1, $$0, $$2.getBlockPos(), Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      BlockState $$4 = $$1.getBlockState($$3);
      return $$4.is(this) || $$4.is(Blocks.BIG_DRIPLEAF_STEM) || $$4.is(BlockTags.BIG_DRIPLEAF_PLACEABLE);
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
      if ($$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if ($$0.getValue(WATERLOGGED)) {
            $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
         }

         return $$4 == Direction.UP && $$6.is(this)
            ? Blocks.BIG_DRIPLEAF_STEM.withPropertiesOf($$0)
            : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      BlockState $$3 = $$0.getBlockState($$1.above());
      return canReplace($$3);
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BlockPos $$4 = $$2.above();
      BlockState $$5 = $$0.getBlockState($$4);
      if (canPlaceAt($$0, $$4, $$5)) {
         Direction $$6 = $$3.getValue(FACING);
         BigDripleafStemBlock.place($$0, $$2, $$3.getFluidState(), $$6);
         place($$0, $$4, $$5.getFluidState(), $$6);
      }
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         if ($$0.getValue(TILT) == Tilt.NONE && canEntityTilt($$2, $$3) && !$$1.hasNeighborSignal($$2)) {
            this.setTiltAndScheduleTick($$0, $$1, $$2, Tilt.UNSTABLE, null);
         }
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.hasNeighborSignal($$2)) {
         resetTilt($$0, $$1, $$2);
      } else {
         Tilt $$4 = $$0.getValue(TILT);
         if ($$4 == Tilt.UNSTABLE) {
            this.setTiltAndScheduleTick($$0, $$1, $$2, Tilt.PARTIAL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
         } else if ($$4 == Tilt.PARTIAL) {
            this.setTiltAndScheduleTick($$0, $$1, $$2, Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
         } else if ($$4 == Tilt.FULL) {
            resetTilt($$0, $$1, $$2);
         }
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if ($$1.hasNeighborSignal($$2)) {
         resetTilt($$0, $$1, $$2);
      }
   }

   private static void playTiltSound(net.minecraft.world.level.Level $$0, BlockPos $$1, SoundEvent $$2) {
      float $$3 = Mth.randomBetween($$0.random, 0.8F, 1.2F);
      $$0.playSound(null, $$1, $$2, SoundSource.BLOCKS, 1.0F, $$3);
   }

   private static boolean canEntityTilt(BlockPos $$0, Entity $$1) {
      return $$1.onGround() && $$1.position().y > $$0.getY() + 0.6875F;
   }

   private void setTiltAndScheduleTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Tilt $$3, SoundEvent $$4) {
      setTilt($$0, $$1, $$2, $$3);
      if ($$4 != null) {
         playTiltSound($$1, $$2, $$4);
      }

      int $$5 = DELAY_UNTIL_NEXT_TILT_STATE.getInt($$3);
      if ($$5 != -1) {
         $$1.scheduleTick($$2, this, $$5);
      }
   }

   private static void resetTilt(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      setTilt($$0, $$1, $$2, Tilt.NONE);
      if ($$0.getValue(TILT) != Tilt.NONE) {
         playTiltSound($$1, $$2, SoundEvents.BIG_DRIPLEAF_TILT_UP);
      }
   }

   private static void setTilt(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Tilt $$3) {
      Tilt $$4 = $$0.getValue(TILT);
      $$1.setBlock($$2, $$0.setValue(TILT, $$3), 2);
      if ($$3.causesVibration() && $$3 != $$4) {
         $$1.gameEvent(null, GameEvent.BLOCK_CHANGE, $$2);
      }
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE_LEAF.get($$0.getValue(TILT));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos().below());
      FluidState $$2 = $$0.getLevel().getFluidState($$0.getClickedPos());
      boolean $$3 = $$1.is(Blocks.BIG_DRIPLEAF) || $$1.is(Blocks.BIG_DRIPLEAF_STEM);
      return this.defaultBlockState()
         .setValue(WATERLOGGED, $$2.isSourceOfType(Fluids.WATER))
         .setValue(FACING, $$3 ? (Direction)$$1.getValue(FACING) : $$0.getHorizontalDirection().getOpposite());
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(WATERLOGGED, FACING, TILT);
   }
}
