package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {
   public static final MapCodec<PointedDripstoneBlock> CODEC = simpleCodec(PointedDripstoneBlock::new);
   public static final EnumProperty<Direction> TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
   public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final int MAX_SEARCH_LENGTH_WHEN_CHECKING_DRIP_TYPE = 11;
   private static final int DELAY_BEFORE_FALLING = 2;
   private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK = 0.02F;
   private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK_IF_UNDER_LIQUID_SOURCE = 0.12F;
   private static final int MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON = 11;
   private static final float WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.17578125F;
   private static final float LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.05859375F;
   private static final double MIN_TRIDENT_VELOCITY_TO_BREAK_DRIPSTONE = 0.6;
   private static final float STALACTITE_DAMAGE_PER_FALL_DISTANCE_AND_SIZE = 1.0F;
   private static final int STALACTITE_MAX_DAMAGE = 40;
   private static final int MAX_STALACTITE_HEIGHT_FOR_DAMAGE_CALCULATION = 6;
   private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.5F;
   private static final int STALAGMITE_FALL_DAMAGE_MODIFIER = 2;
   private static final float AVERAGE_DAYS_PER_GROWTH = 5.0F;
   private static final float GROWTH_PROBABILITY_PER_RANDOM_TICK = 0.011377778F;
   private static final int MAX_GROWTH_LENGTH = 7;
   private static final int MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING = 10;
   private static final VoxelShape SHAPE_TIP_MERGE = Block.column(6.0, 0.0, 16.0);
   private static final VoxelShape SHAPE_TIP_UP = Block.column(6.0, 0.0, 11.0);
   private static final VoxelShape SHAPE_TIP_DOWN = Block.column(6.0, 5.0, 16.0);
   private static final VoxelShape SHAPE_FRUSTUM = Block.column(8.0, 0.0, 16.0);
   private static final VoxelShape SHAPE_MIDDLE = Block.column(10.0, 0.0, 16.0);
   private static final VoxelShape SHAPE_BASE = Block.column(12.0, 0.0, 16.0);
   private static final double STALACTITE_DRIP_START_PIXEL = SHAPE_TIP_DOWN.min(Axis.Y);
   private static final float MAX_HORIZONTAL_OFFSET = (float)SHAPE_BASE.min(Axis.X);
   private static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.column(4.0, 0.0, 16.0);

   @Override
   public MapCodec<PointedDripstoneBlock> codec() {
      return CODEC;
   }

   public PointedDripstoneBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(
         this.stateDefinition.any().setValue(TIP_DIRECTION, Direction.UP).setValue(THICKNESS, DripstoneThickness.TIP).setValue(WATERLOGGED, false)
      );
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return isValidPointedDripstonePlacement($$1, $$2, $$0.getValue(TIP_DIRECTION));
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

      if ($$4 != Direction.UP && $$4 != Direction.DOWN) {
         return $$0;
      } else {
         Direction $$8 = $$0.getValue(TIP_DIRECTION);
         if ($$8 == Direction.DOWN && $$2.getBlockTicks().hasScheduledTick($$3, this)) {
            return $$0;
         } else if ($$4 == $$8.getOpposite() && !this.canSurvive($$0, $$1, $$3)) {
            if ($$8 == Direction.DOWN) {
               $$2.scheduleTick($$3, this, 2);
            } else {
               $$2.scheduleTick($$3, this, 1);
            }

            return $$0;
         } else {
            boolean $$9 = $$0.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
            DripstoneThickness $$10 = calculateDripstoneThickness($$1, $$3, $$8, $$9);
            return $$0.setValue(THICKNESS, $$10);
         }
      }
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      if (!$$0.isClientSide()) {
         BlockPos $$4 = $$2.getBlockPos();
         if ($$0 instanceof ServerLevel $$5
            && $$3.mayInteract($$5, $$4)
            && $$3.mayBreak($$5)
            && $$3 instanceof ThrownTrident
            && $$3.getDeltaMovement().length() > 0.6) {
            $$0.destroyBlock($$4, true);
         }
      }
   }

   @Override
   public void fallOn(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, double $$4) {
      if ($$1.getValue(TIP_DIRECTION) == Direction.UP && $$1.getValue(THICKNESS) == DripstoneThickness.TIP) {
         $$3.causeFallDamage($$4 + 2.5, 2.0F, $$0.damageSources().stalagmite());
      } else {
         super.fallOn($$0, $$1, $$2, $$3, $$4);
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if (canDrip($$0)) {
         float $$4 = $$3.nextFloat();
         if (!($$4 > 0.12F)) {
            getFluidAboveStalactite($$1, $$2, $$0)
               .filter($$1x -> $$4 < 0.02F || canFillCauldron($$1x.fluid))
               .ifPresent($$3x -> spawnDripParticle($$1, $$2, $$0, $$3x.fluid, $$3x.pos));
         }
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (isStalagmite($$0) && !this.canSurvive($$0, $$1, $$2)) {
         $$1.destroyBlock($$2, true);
      } else {
         spawnFallingStalactite($$0, $$1, $$2);
      }
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      maybeTransferFluid($$0, $$1, $$2, $$3.nextFloat());
      if ($$3.nextFloat() < 0.011377778F && isStalactiteStartPos($$0, $$1, $$2)) {
         growStalactiteOrStalagmiteIfPossible($$0, $$1, $$2, $$3);
      }
   }

   @VisibleForTesting
   public static void maybeTransferFluid(BlockState $$0, ServerLevel $$1, BlockPos $$2, float $$3) {
      if (!($$3 > 0.17578125F) || !($$3 > 0.05859375F)) {
         if (isStalactiteStartPos($$0, $$1, $$2)) {
            Optional<PointedDripstoneBlock.FluidInfo> $$4 = getFluidAboveStalactite($$1, $$2, $$0);
            if (!$$4.isEmpty()) {
               Fluid $$5 = $$4.get().fluid;
               float $$6;
               if ($$5 == Fluids.WATER) {
                  $$6 = 0.17578125F;
               } else {
                  if ($$5 != Fluids.LAVA) {
                     return;
                  }

                  $$6 = 0.05859375F;
               }

               if (!($$3 >= $$6)) {
                  BlockPos $$9 = findTip($$0, $$1, $$2, 11, false);
                  if ($$9 != null) {
                     if ($$4.get().sourceState.is(Blocks.MUD) && $$5 == Fluids.WATER) {
                        BlockState $$10 = Blocks.CLAY.defaultBlockState();
                        $$1.setBlockAndUpdate($$4.get().pos, $$10);
                        Block.pushEntitiesUp($$4.get().sourceState, $$10, $$1, $$4.get().pos);
                        $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$4.get().pos, GameEvent.Context.of($$10));
                        $$1.levelEvent(1504, $$9, 0);
                     } else {
                        BlockPos $$11 = findFillableCauldronBelowStalactiteTip($$1, $$9, $$5);
                        if ($$11 != null) {
                           $$1.levelEvent(1504, $$9, 0);
                           int $$12 = $$9.getY() - $$11.getY();
                           int $$13 = 50 + $$12;
                           BlockState $$14 = $$1.getBlockState($$11);
                           $$1.scheduleTick($$11, $$14.getBlock(), $$13);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      net.minecraft.world.level.LevelAccessor $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      Direction $$3 = $$0.getNearestLookingVerticalDirection().getOpposite();
      Direction $$4 = calculateTipDirection($$1, $$2, $$3);
      if ($$4 == null) {
         return null;
      } else {
         boolean $$5 = !$$0.isSecondaryUseActive();
         DripstoneThickness $$6 = calculateDripstoneThickness($$1, $$2, $$4, $$5);
         return this.defaultBlockState()
            .setValue(TIP_DIRECTION, $$4)
            .setValue(THICKNESS, $$6)
            .setValue(WATERLOGGED, $$1.getFluidState($$2).getType() == Fluids.WATER);
      }
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      VoxelShape $$4 = switch ((DripstoneThickness)$$0.getValue(THICKNESS)) {
         case TIP_MERGE -> SHAPE_TIP_MERGE;
         case TIP -> $$0.getValue(TIP_DIRECTION) == Direction.DOWN ? SHAPE_TIP_DOWN : SHAPE_TIP_UP;
         case FRUSTUM -> SHAPE_FRUSTUM;
         case MIDDLE -> SHAPE_MIDDLE;
         case BASE -> SHAPE_BASE;
      };
      return $$4.move($$0.getOffset($$2));
   }

   @Override
   protected boolean isCollisionShapeFullBlock(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return false;
   }

   @Override
   protected float getMaxHorizontalOffset() {
      return MAX_HORIZONTAL_OFFSET;
   }

   @Override
   public void onBrokenAfterFall(net.minecraft.world.level.Level $$0, BlockPos $$1, FallingBlockEntity $$2) {
      if (!$$2.isSilent()) {
         $$0.levelEvent(1045, $$1, 0);
      }
   }

   @Override
   public DamageSource getFallDamageSource(Entity $$0) {
      return $$0.damageSources().fallingStalactite($$0);
   }

   private static void spawnFallingStalactite(BlockState $$0, ServerLevel $$1, BlockPos $$2) {
      MutableBlockPos $$3 = $$2.mutable();
      BlockState $$4 = $$0;

      while (isStalactite($$4)) {
         FallingBlockEntity $$5 = FallingBlockEntity.fall($$1, $$3, $$4);
         if (isTip($$4, true)) {
            int $$6 = Math.max(1 + $$2.getY() - $$3.getY(), 6);
            float $$7 = 1.0F * $$6;
            $$5.setHurtsEntities($$7, 40);
            break;
         }

         $$3.move(Direction.DOWN);
         $$4 = $$1.getBlockState($$3);
      }
   }

   @VisibleForTesting
   public static void growStalactiteOrStalagmiteIfPossible(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      BlockState $$4 = $$1.getBlockState($$2.above(1));
      BlockState $$5 = $$1.getBlockState($$2.above(2));
      if (canGrow($$4, $$5)) {
         BlockPos $$6 = findTip($$0, $$1, $$2, 7, false);
         if ($$6 != null) {
            BlockState $$7 = $$1.getBlockState($$6);
            if (canDrip($$7) && canTipGrow($$7, $$1, $$6)) {
               if ($$3.nextBoolean()) {
                  grow($$1, $$6, Direction.DOWN);
               } else {
                  growStalagmiteBelow($$1, $$6);
               }
            }
         }
      }
   }

   private static void growStalagmiteBelow(ServerLevel $$0, BlockPos $$1) {
      MutableBlockPos $$2 = $$1.mutable();

      for (int $$3 = 0; $$3 < 10; $$3++) {
         $$2.move(Direction.DOWN);
         BlockState $$4 = $$0.getBlockState($$2);
         if (!$$4.getFluidState().isEmpty()) {
            return;
         }

         if (isUnmergedTipWithDirection($$4, Direction.UP) && canTipGrow($$4, $$0, $$2)) {
            grow($$0, $$2, Direction.UP);
            return;
         }

         if (isValidPointedDripstonePlacement($$0, $$2, Direction.UP) && !$$0.isWaterAt($$2.below())) {
            grow($$0, $$2.below(), Direction.UP);
            return;
         }

         if (!canDripThrough($$0, $$2, $$4)) {
            return;
         }
      }
   }

   private static void grow(ServerLevel $$0, BlockPos $$1, Direction $$2) {
      BlockPos $$3 = $$1.relative($$2);
      BlockState $$4 = $$0.getBlockState($$3);
      if (isUnmergedTipWithDirection($$4, $$2.getOpposite())) {
         createMergedTips($$4, $$0, $$3);
      } else if ($$4.isAir() || $$4.is(Blocks.WATER)) {
         createDripstone($$0, $$3, $$2, DripstoneThickness.TIP);
      }
   }

   private static void createDripstone(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, Direction $$2, DripstoneThickness $$3) {
      BlockState $$4 = Blocks.POINTED_DRIPSTONE
         .defaultBlockState()
         .setValue(TIP_DIRECTION, $$2)
         .setValue(THICKNESS, $$3)
         .setValue(WATERLOGGED, $$0.getFluidState($$1).getType() == Fluids.WATER);
      $$0.setBlock($$1, $$4, 3);
   }

   private static void createMergedTips(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
      BlockPos $$4;
      BlockPos $$3;
      if ($$0.getValue(TIP_DIRECTION) == Direction.UP) {
         $$3 = $$2;
         $$4 = $$2.above();
      } else {
         $$4 = $$2;
         $$3 = $$2.below();
      }

      createDripstone($$1, $$4, Direction.DOWN, DripstoneThickness.TIP_MERGE);
      createDripstone($$1, $$3, Direction.UP, DripstoneThickness.TIP_MERGE);
   }

   public static void spawnDripParticle(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      getFluidAboveStalactite($$0, $$1, $$2).ifPresent($$3 -> spawnDripParticle($$0, $$1, $$2, $$3.fluid, $$3.pos));
   }

   private static void spawnDripParticle(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Fluid $$3, BlockPos $$4) {
      Vec3 $$5 = $$2.getOffset($$1);
      double $$6 = 0.0625;
      double $$7 = $$1.getX() + 0.5 + $$5.x;
      double $$8 = $$1.getY() + STALACTITE_DRIP_START_PIXEL - 0.0625;
      double $$9 = $$1.getZ() + 0.5 + $$5.z;
      ParticleOptions $$10 = getDripParticle($$0, $$3, $$4);
      $$0.addParticle($$10, $$7, $$8, $$9, 0.0, 0.0, 0.0);
   }

   
   private static BlockPos findTip(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, int $$3, boolean $$4) {
      if (isTip($$0, $$4)) {
         return $$2;
      } else {
         Direction $$5 = $$0.getValue(TIP_DIRECTION);
         BiPredicate<BlockPos, BlockState> $$6 = ($$1x, $$2x) -> $$2x.is(Blocks.POINTED_DRIPSTONE) && $$2x.getValue(TIP_DIRECTION) == $$5;
         return findBlockVertical($$1, $$2, $$5.getAxisDirection(), $$6, $$1x -> isTip($$1x, $$4), $$3).orElse(null);
      }
   }

   
   private static Direction calculateTipDirection(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, Direction $$2) {
      Direction $$3;
      if (isValidPointedDripstonePlacement($$0, $$1, $$2)) {
         $$3 = $$2;
      } else {
         if (!isValidPointedDripstonePlacement($$0, $$1, $$2.getOpposite())) {
            return null;
         }

         $$3 = $$2.getOpposite();
      }

      return $$3;
   }

   private static DripstoneThickness calculateDripstoneThickness(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, Direction $$2, boolean $$3) {
      Direction $$4 = $$2.getOpposite();
      BlockState $$5 = $$0.getBlockState($$1.relative($$2));
      if (isPointedDripstoneWithDirection($$5, $$4)) {
         return !$$3 && $$5.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
      } else if (!isPointedDripstoneWithDirection($$5, $$2)) {
         return DripstoneThickness.TIP;
      } else {
         DripstoneThickness $$6 = $$5.getValue(THICKNESS);
         if ($$6 != DripstoneThickness.TIP && $$6 != DripstoneThickness.TIP_MERGE) {
            BlockState $$7 = $$0.getBlockState($$1.relative($$4));
            return !isPointedDripstoneWithDirection($$7, $$2) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
         } else {
            return DripstoneThickness.FRUSTUM;
         }
      }
   }

   public static boolean canDrip(BlockState $$0) {
      return isStalactite($$0) && $$0.getValue(THICKNESS) == DripstoneThickness.TIP && !$$0.getValue(WATERLOGGED);
   }

   private static boolean canTipGrow(BlockState $$0, ServerLevel $$1, BlockPos $$2) {
      Direction $$3 = $$0.getValue(TIP_DIRECTION);
      BlockPos $$4 = $$2.relative($$3);
      BlockState $$5 = $$1.getBlockState($$4);
      if (!$$5.getFluidState().isEmpty()) {
         return false;
      } else {
         return $$5.isAir() ? true : isUnmergedTipWithDirection($$5, $$3.getOpposite());
      }
   }

   private static Optional<BlockPos> findRootBlock(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, int $$3) {
      Direction $$4 = $$2.getValue(TIP_DIRECTION);
      BiPredicate<BlockPos, BlockState> $$5 = ($$1x, $$2x) -> $$2x.is(Blocks.POINTED_DRIPSTONE) && $$2x.getValue(TIP_DIRECTION) == $$4;
      return findBlockVertical($$0, $$1, $$4.getOpposite().getAxisDirection(), $$5, $$0x -> !$$0x.is(Blocks.POINTED_DRIPSTONE), $$3);
   }

   private static boolean isValidPointedDripstonePlacement(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, Direction $$2) {
      BlockPos $$3 = $$1.relative($$2.getOpposite());
      BlockState $$4 = $$0.getBlockState($$3);
      return $$4.isFaceSturdy($$0, $$3, $$2) || isPointedDripstoneWithDirection($$4, $$2);
   }

   private static boolean isTip(BlockState $$0, boolean $$1) {
      if (!$$0.is(Blocks.POINTED_DRIPSTONE)) {
         return false;
      } else {
         DripstoneThickness $$2 = $$0.getValue(THICKNESS);
         return $$2 == DripstoneThickness.TIP || $$1 && $$2 == DripstoneThickness.TIP_MERGE;
      }
   }

   private static boolean isUnmergedTipWithDirection(BlockState $$0, Direction $$1) {
      return isTip($$0, false) && $$0.getValue(TIP_DIRECTION) == $$1;
   }

   private static boolean isStalactite(BlockState $$0) {
      return isPointedDripstoneWithDirection($$0, Direction.DOWN);
   }

   private static boolean isStalagmite(BlockState $$0) {
      return isPointedDripstoneWithDirection($$0, Direction.UP);
   }

   private static boolean isStalactiteStartPos(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return isStalactite($$0) && !$$1.getBlockState($$2.above()).is(Blocks.POINTED_DRIPSTONE);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   private static boolean isPointedDripstoneWithDirection(BlockState $$0, Direction $$1) {
      return $$0.is(Blocks.POINTED_DRIPSTONE) && $$0.getValue(TIP_DIRECTION) == $$1;
   }

   
   private static BlockPos findFillableCauldronBelowStalactiteTip(net.minecraft.world.level.Level $$0, BlockPos $$1, Fluid $$2) {
      Predicate<BlockState> $$3 = $$1x -> $$1x.getBlock() instanceof AbstractCauldronBlock
         && ((AbstractCauldronBlock)$$1x.getBlock()).canReceiveStalactiteDrip($$2);
      BiPredicate<BlockPos, BlockState> $$4 = ($$1x, $$2x) -> canDripThrough($$0, $$1x, $$2x);
      return findBlockVertical($$0, $$1, Direction.DOWN.getAxisDirection(), $$4, $$3, 11).orElse(null);
   }

   
   public static BlockPos findStalactiteTipAboveCauldron(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      BiPredicate<BlockPos, BlockState> $$2 = ($$1x, $$2x) -> canDripThrough($$0, $$1x, $$2x);
      return findBlockVertical($$0, $$1, Direction.UP.getAxisDirection(), $$2, PointedDripstoneBlock::canDrip, 11).orElse(null);
   }

   public static Fluid getCauldronFillFluidType(ServerLevel $$0, BlockPos $$1) {
      return getFluidAboveStalactite($$0, $$1, $$0.getBlockState($$1))
         .map($$0x -> $$0x.fluid)
         .filter(PointedDripstoneBlock::canFillCauldron)
         .orElse(Fluids.EMPTY);
   }

   private static Optional<PointedDripstoneBlock.FluidInfo> getFluidAboveStalactite(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      return !isStalactite($$2) ? Optional.empty() : findRootBlock($$0, $$1, $$2, 11).map($$1x -> {
         BlockPos $$2x = $$1x.above();
         BlockState $$3 = $$0.getBlockState($$2x);
         Fluid $$4;
         if ($$3.is(Blocks.MUD) && !(Boolean)$$0.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, $$2x)) {
            $$4 = Fluids.WATER;
         } else {
            $$4 = $$0.getFluidState($$2x).getType();
         }

         return new PointedDripstoneBlock.FluidInfo($$2x, $$4, $$3);
      });
   }

   private static boolean canFillCauldron(Fluid $$0) {
      return $$0 == Fluids.LAVA || $$0 == Fluids.WATER;
   }

   private static boolean canGrow(BlockState $$0, BlockState $$1) {
      return $$0.is(Blocks.DRIPSTONE_BLOCK) && $$1.is(Blocks.WATER) && $$1.getFluidState().isSource();
   }

   private static ParticleOptions getDripParticle(net.minecraft.world.level.Level $$0, Fluid $$1, BlockPos $$2) {
      if ($$1.isSame(Fluids.EMPTY)) {
         return (ParticleOptions)$$0.environmentAttributes().getValue(EnvironmentAttributes.DEFAULT_DRIPSTONE_PARTICLE, $$2);
      } else {
         return $$1.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
      }
   }

   private static Optional<BlockPos> findBlockVertical(
      net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, AxisDirection $$2, BiPredicate<BlockPos, BlockState> $$3, Predicate<BlockState> $$4, int $$5
   ) {
      Direction $$6 = Direction.get($$2, Axis.Y);
      MutableBlockPos $$7 = $$1.mutable();

      for (int $$8 = 1; $$8 < $$5; $$8++) {
         $$7.move($$6);
         BlockState $$9 = $$0.getBlockState($$7);
         if ($$4.test($$9)) {
            return Optional.of($$7.immutable());
         }

         if ($$0.isOutsideBuildHeight($$7.getY()) || !$$3.test($$7, $$9)) {
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   private static boolean canDripThrough(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      if ($$2.isAir()) {
         return true;
      } else if ($$2.isSolidRender()) {
         return false;
      } else if (!$$2.getFluidState().isEmpty()) {
         return false;
      } else {
         VoxelShape $$3 = $$2.getCollisionShape($$0, $$1);
         return !Shapes.joinIsNotEmpty(REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK, $$3, BooleanOp.AND);
      }
   }

   record FluidInfo(BlockPos pos, Fluid fluid, BlockState sourceState) {
   }
}
