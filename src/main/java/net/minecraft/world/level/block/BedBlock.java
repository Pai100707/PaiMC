package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock {
   public static final MapCodec<BedBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(DyeColor.CODEC.fieldOf("color").forGetter(BedBlock::getColor), propertiesCodec()).apply($$0, BedBlock::new)
   );
   public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
   public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
   private static final Map<Direction, VoxelShape> SHAPES = (Map<Direction, VoxelShape>)Util.make(() -> {
      VoxelShape $$0 = Block.box(0.0, 0.0, 0.0, 3.0, 3.0, 3.0);
      VoxelShape $$1 = Shapes.rotate($$0, OctahedralGroup.BLOCK_ROT_Y_90);
      return Shapes.rotateHorizontal(Shapes.or(Block.column(16.0, 3.0, 9.0), new VoxelShape[]{$$0, $$1}));
   });
   private final DyeColor color;

   @Override
   public MapCodec<BedBlock> codec() {
      return CODEC;
   }

   public BedBlock(DyeColor $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.color = $$0;
      this.registerDefaultState(this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, false));
   }

   @Nullable
   public static Direction getBedOrientation(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      return $$2.getBlock() instanceof BedBlock ? $$2.getValue(FACING) : null;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.isClientSide()) {
         return InteractionResult.SUCCESS_SERVER;
      } else {
         if ($$0.getValue(PART) != BedPart.HEAD) {
            $$2 = $$2.relative($$0.getValue(FACING));
            $$0 = $$1.getBlockState($$2);
            if (!$$0.is(this)) {
               return InteractionResult.CONSUME;
            }
         }

         BedRule $$5 = (BedRule)$$1.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, $$2);
         if ($$5.explodes()) {
            $$5.errorMessage().ifPresent($$1x -> $$3.displayClientMessage($$1x, true));
            $$1.removeBlock($$2, false);
            BlockPos $$6 = $$2.relative(((Direction)$$0.getValue(FACING)).getOpposite());
            if ($$1.getBlockState($$6).is(this)) {
               $$1.removeBlock($$6, false);
            }

            Vec3 $$7 = $$2.getCenter();
            $$1.explode(
               null, $$1.damageSources().badRespawnPointExplosion($$7), null, $$7, 5.0F, true, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK
            );
            return InteractionResult.SUCCESS_SERVER;
         } else if ($$0.getValue(OCCUPIED)) {
            if (!this.kickVillagerOutOfBed($$1, $$2)) {
               $$3.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            }

            return InteractionResult.SUCCESS_SERVER;
         } else {
            $$3.startSleepInBed($$2).ifLeft($$1x -> {
               if ($$1x.message() != null) {
                  $$3.displayClientMessage($$1x.message(), true);
               }
            });
            return InteractionResult.SUCCESS_SERVER;
         }
      }
   }

   private boolean kickVillagerOutOfBed(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      List<Villager> $$2 = $$0.getEntitiesOfClass(Villager.class, new AABB($$1), LivingEntity::isSleeping);
      if ($$2.isEmpty()) {
         return false;
      } else {
         $$2.get(0).stopSleeping();
         return true;
      }
   }

   @Override
   public void fallOn(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, double $$4) {
      super.fallOn($$0, $$1, $$2, $$3, $$4 * 0.5);
   }

   @Override
   public void updateEntityMovementAfterFallOn(net.minecraft.world.level.BlockGetter $$0, Entity $$1) {
      if ($$1.isSuppressingBounce()) {
         super.updateEntityMovementAfterFallOn($$0, $$1);
      } else {
         this.bounceUp($$1);
      }
   }

   private void bounceUp(Entity $$0) {
      Vec3 $$1 = $$0.getDeltaMovement();
      if ($$1.y < 0.0) {
         double $$2 = $$0 instanceof LivingEntity ? 1.0 : 0.8;
         $$0.setDeltaMovement($$1.x, -$$1.y * 0.66F * $$2, $$1.z);
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
      if ($$4 == getNeighbourDirection($$0.getValue(PART), $$0.getValue(FACING))) {
         return $$6.is(this) && $$6.getValue(PART) != $$0.getValue(PART) ? $$0.setValue(OCCUPIED, $$6.getValue(OCCUPIED)) : Blocks.AIR.defaultBlockState();
      } else {
         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }

   private static Direction getNeighbourDirection(BedPart $$0, Direction $$1) {
      return $$0 == BedPart.FOOT ? $$1 : $$1.getOpposite();
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      if (!$$0.isClientSide() && $$3.preventsBlockDrops()) {
         BedPart $$4 = $$2.getValue(PART);
         if ($$4 == BedPart.FOOT) {
            BlockPos $$5 = $$1.relative(getNeighbourDirection($$4, $$2.getValue(FACING)));
            BlockState $$6 = $$0.getBlockState($$5);
            if ($$6.is(this) && $$6.getValue(PART) == BedPart.HEAD) {
               $$0.setBlock($$5, Blocks.AIR.defaultBlockState(), 35);
               $$0.levelEvent($$3, 2001, $$5, Block.getId($$6));
            }
         }
      }

      return super.playerWillDestroy($$0, $$1, $$2, $$3);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      Direction $$1 = $$0.getHorizontalDirection();
      BlockPos $$2 = $$0.getClickedPos();
      BlockPos $$3 = $$2.relative($$1);
      net.minecraft.world.level.Level $$4 = $$0.getLevel();
      return $$4.getBlockState($$3).canBeReplaced($$0) && $$4.getWorldBorder().isWithinBounds($$3) ? this.defaultBlockState().setValue(FACING, $$1) : null;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get(getConnectedDirection($$0).getOpposite());
   }

   public static Direction getConnectedDirection(BlockState $$0) {
      Direction $$1 = $$0.getValue(FACING);
      return $$0.getValue(PART) == BedPart.HEAD ? $$1.getOpposite() : $$1;
   }

   public static DoubleBlockCombiner.BlockType getBlockType(BlockState $$0) {
      BedPart $$1 = $$0.getValue(PART);
      return $$1 == BedPart.HEAD ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
   }

   private static boolean isBunkBed(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return $$0.getBlockState($$1.below()).getBlock() instanceof BedBlock;
   }

   public static Optional<Vec3> findStandUpPosition(EntityType<?> $$0, net.minecraft.world.level.CollisionGetter $$1, BlockPos $$2, Direction $$3, float $$4) {
      Direction $$5 = $$3.getClockWise();
      Direction $$6 = $$5.isFacingAngle($$4) ? $$5.getOpposite() : $$5;
      if (isBunkBed($$1, $$2)) {
         return findBunkBedStandUpPosition($$0, $$1, $$2, $$3, $$6);
      } else {
         int[][] $$7 = bedStandUpOffsets($$3, $$6);
         Optional<Vec3> $$8 = findStandUpPositionAtOffset($$0, $$1, $$2, $$7, true);
         return $$8.isPresent() ? $$8 : findStandUpPositionAtOffset($$0, $$1, $$2, $$7, false);
      }
   }

   private static Optional<Vec3> findBunkBedStandUpPosition(
      EntityType<?> $$0, net.minecraft.world.level.CollisionGetter $$1, BlockPos $$2, Direction $$3, Direction $$4
   ) {
      int[][] $$5 = bedSurroundStandUpOffsets($$3, $$4);
      Optional<Vec3> $$6 = findStandUpPositionAtOffset($$0, $$1, $$2, $$5, true);
      if ($$6.isPresent()) {
         return $$6;
      } else {
         BlockPos $$7 = $$2.below();
         Optional<Vec3> $$8 = findStandUpPositionAtOffset($$0, $$1, $$7, $$5, true);
         if ($$8.isPresent()) {
            return $$8;
         } else {
            int[][] $$9 = bedAboveStandUpOffsets($$3);
            Optional<Vec3> $$10 = findStandUpPositionAtOffset($$0, $$1, $$2, $$9, true);
            if ($$10.isPresent()) {
               return $$10;
            } else {
               Optional<Vec3> $$11 = findStandUpPositionAtOffset($$0, $$1, $$2, $$5, false);
               if ($$11.isPresent()) {
                  return $$11;
               } else {
                  Optional<Vec3> $$12 = findStandUpPositionAtOffset($$0, $$1, $$7, $$5, false);
                  return $$12.isPresent() ? $$12 : findStandUpPositionAtOffset($$0, $$1, $$2, $$9, false);
               }
            }
         }
      }
   }

   private static Optional<Vec3> findStandUpPositionAtOffset(
      EntityType<?> $$0, net.minecraft.world.level.CollisionGetter $$1, BlockPos $$2, int[][] $$3, boolean $$4
   ) {
      MutableBlockPos $$5 = new MutableBlockPos();

      for (int[] $$6 : $$3) {
         $$5.set($$2.getX() + $$6[0], $$2.getY(), $$2.getZ() + $$6[1]);
         Vec3 $$7 = DismountHelper.findSafeDismountLocation($$0, $$1, $$5, $$4);
         if ($$7 != null) {
            return Optional.of($$7);
         }
      }

      return Optional.empty();
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, PART, OCCUPIED);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new BedBlockEntity($$0, $$1, this.color);
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, @Nullable LivingEntity $$3, ItemStack $$4) {
      super.setPlacedBy($$0, $$1, $$2, $$3, $$4);
      if (!$$0.isClientSide()) {
         BlockPos $$5 = $$1.relative($$2.getValue(FACING));
         $$0.setBlock($$5, $$2.setValue(PART, BedPart.HEAD), 3);
         $$0.updateNeighborsAt($$1, Blocks.AIR);
         $$2.updateNeighbourShapes($$0, $$1, 3);
      }
   }

   public DyeColor getColor() {
      return this.color;
   }

   @Override
   protected long getSeed(BlockState $$0, BlockPos $$1) {
      BlockPos $$2 = $$1.relative($$0.getValue(FACING), $$0.getValue(PART) == BedPart.HEAD ? 0 : 1);
      return Mth.getSeed($$2.getX(), $$1.getY(), $$2.getZ());
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   private static int[][] bedStandUpOffsets(Direction $$0, Direction $$1) {
      return (int[][])ArrayUtils.addAll(bedSurroundStandUpOffsets($$0, $$1), bedAboveStandUpOffsets($$0));
   }

   private static int[][] bedSurroundStandUpOffsets(Direction $$0, Direction $$1) {
      return new int[][]{
         {$$1.getStepX(), $$1.getStepZ()},
         {$$1.getStepX() - $$0.getStepX(), $$1.getStepZ() - $$0.getStepZ()},
         {$$1.getStepX() - $$0.getStepX() * 2, $$1.getStepZ() - $$0.getStepZ() * 2},
         {-$$0.getStepX() * 2, -$$0.getStepZ() * 2},
         {-$$1.getStepX() - $$0.getStepX() * 2, -$$1.getStepZ() - $$0.getStepZ() * 2},
         {-$$1.getStepX() - $$0.getStepX(), -$$1.getStepZ() - $$0.getStepZ()},
         {-$$1.getStepX(), -$$1.getStepZ()},
         {-$$1.getStepX() + $$0.getStepX(), -$$1.getStepZ() + $$0.getStepZ()},
         {$$0.getStepX(), $$0.getStepZ()},
         {$$1.getStepX() + $$0.getStepX(), $$1.getStepZ() + $$0.getStepZ()}
      };
   }

   private static int[][] bedAboveStandUpOffsets(Direction $$0) {
      return new int[][]{{0, 0}, {-$$0.getStepX(), -$$0.getStepZ()}};
   }
}
