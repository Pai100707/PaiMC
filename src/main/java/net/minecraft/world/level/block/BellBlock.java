package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BellBlock extends BaseEntityBlock {
   public static final MapCodec<BellBlock> CODEC = simpleCodec(BellBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final VoxelShape BELL_SHAPE = Shapes.or(Block.column(6.0, 6.0, 13.0), Block.column(8.0, 4.0, 6.0));
   private static final VoxelShape SHAPE_CEILING = Shapes.or(BELL_SHAPE, Block.column(2.0, 13.0, 16.0));
   private static final Map<Axis, VoxelShape> SHAPE_FLOOR = Shapes.rotateHorizontalAxis(Block.cube(16.0, 16.0, 8.0));
   private static final Map<Axis, VoxelShape> SHAPE_DOUBLE_WALL = Shapes.rotateHorizontalAxis(Shapes.or(BELL_SHAPE, Block.column(2.0, 16.0, 13.0, 15.0)));
   private static final Map<Direction, VoxelShape> SHAPE_SINGLE_WALL = Shapes.rotateHorizontal(Shapes.or(BELL_SHAPE, Block.boxZ(2.0, 13.0, 15.0, 0.0, 13.0)));
   public static final int EVENT_BELL_RING = 1;

   @Override
   public MapCodec<BellBlock> codec() {
      return CODEC;
   }

   public BellBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(
         this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, false)
      );
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      boolean $$6 = $$1.hasNeighborSignal($$2);
      if ($$6 != $$0.getValue(POWERED)) {
         if ($$6) {
            this.attemptToRing($$1, $$2, null);
         }

         $$1.setBlock($$2, $$0.setValue(POWERED, $$6), 3);
      }
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      Player $$6 = $$3.getOwner() instanceof Player $$5 ? $$5 : null;
      this.onHit($$0, $$1, $$2, $$6, true);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      return (InteractionResult)(this.onHit($$1, $$0, $$4, $$3, true) ? InteractionResult.SUCCESS : InteractionResult.PASS);
   }

   public boolean onHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, @Nullable Player $$3, boolean $$4) {
      Direction $$5 = $$2.getDirection();
      BlockPos $$6 = $$2.getBlockPos();
      boolean $$7 = !$$4 || this.isProperHit($$1, $$5, $$2.getLocation().y - $$6.getY());
      if ($$7) {
         boolean $$8 = this.attemptToRing($$3, $$0, $$6, $$5);
         if ($$8 && $$3 != null) {
            $$3.awardStat(Stats.BELL_RING);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isProperHit(BlockState $$0, Direction $$1, double $$2) {
      if ($$1.getAxis() != Axis.Y && !($$2 > 0.8124F)) {
         Direction $$3 = $$0.getValue(FACING);
         BellAttachType $$4 = $$0.getValue(ATTACHMENT);
         switch ($$4) {
            case FLOOR:
               return $$3.getAxis() == $$1.getAxis();
            case SINGLE_WALL:
            case DOUBLE_WALL:
               return $$3.getAxis() != $$1.getAxis();
            case CEILING:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public boolean attemptToRing(net.minecraft.world.level.Level $$0, BlockPos $$1, @Nullable Direction $$2) {
      return this.attemptToRing(null, $$0, $$1, $$2);
   }

   public boolean attemptToRing(@Nullable Entity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, @Nullable Direction $$3) {
      BlockEntity $$4 = $$1.getBlockEntity($$2);
      if (!$$1.isClientSide() && $$4 instanceof BellBlockEntity) {
         if ($$3 == null) {
            $$3 = $$1.getBlockState($$2).getValue(FACING);
         }

         ((BellBlockEntity)$$4).onHit($$3);
         $$1.playSound(null, $$2, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.0F);
         $$1.gameEvent($$0, GameEvent.BLOCK_CHANGE, $$2);
         return true;
      } else {
         return false;
      }
   }

   private VoxelShape getVoxelShape(BlockState $$0) {
      Direction $$1 = $$0.getValue(FACING);

      return switch ((BellAttachType)$$0.getValue(ATTACHMENT)) {
         case FLOOR -> (VoxelShape)SHAPE_FLOOR.get($$1.getAxis());
         case SINGLE_WALL -> (VoxelShape)SHAPE_SINGLE_WALL.get($$1);
         case DOUBLE_WALL -> (VoxelShape)SHAPE_DOUBLE_WALL.get($$1.getAxis());
         case CEILING -> SHAPE_CEILING;
      };
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getVoxelShape($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getVoxelShape($$0);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      Direction $$1 = $$0.getClickedFace();
      BlockPos $$2 = $$0.getClickedPos();
      net.minecraft.world.level.Level $$3 = $$0.getLevel();
      Axis $$4 = $$1.getAxis();
      if ($$4 == Axis.Y) {
         BlockState $$5 = this.defaultBlockState()
            .setValue(ATTACHMENT, $$1 == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)
            .setValue(FACING, $$0.getHorizontalDirection());
         if ($$5.canSurvive($$0.getLevel(), $$2)) {
            return $$5;
         }
      } else {
         boolean $$6 = $$4 == Axis.X
               && $$3.getBlockState($$2.west()).isFaceSturdy($$3, $$2.west(), Direction.EAST)
               && $$3.getBlockState($$2.east()).isFaceSturdy($$3, $$2.east(), Direction.WEST)
            || $$4 == Axis.Z
               && $$3.getBlockState($$2.north()).isFaceSturdy($$3, $$2.north(), Direction.SOUTH)
               && $$3.getBlockState($$2.south()).isFaceSturdy($$3, $$2.south(), Direction.NORTH);
         BlockState $$7 = this.defaultBlockState()
            .setValue(FACING, $$1.getOpposite())
            .setValue(ATTACHMENT, $$6 ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
         if ($$7.canSurvive($$0.getLevel(), $$0.getClickedPos())) {
            return $$7;
         }

         boolean $$8 = $$3.getBlockState($$2.below()).isFaceSturdy($$3, $$2.below(), Direction.UP);
         $$7 = $$7.setValue(ATTACHMENT, $$8 ? BellAttachType.FLOOR : BellAttachType.CEILING);
         if ($$7.canSurvive($$0.getLevel(), $$0.getClickedPos())) {
            return $$7;
         }
      }

      return null;
   }

   @Override
   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if ($$3.canTriggerBlocks()) {
         this.attemptToRing($$1, $$2, null);
      }

      super.onExplosionHit($$0, $$1, $$2, $$3, $$4);
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
      BellAttachType $$8 = $$0.getValue(ATTACHMENT);
      Direction $$9 = getConnectedDirection($$0).getOpposite();
      if ($$9 == $$4 && !$$0.canSurvive($$1, $$3) && $$8 != BellAttachType.DOUBLE_WALL) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if ($$4.getAxis() == ((Direction)$$0.getValue(FACING)).getAxis()) {
            if ($$8 == BellAttachType.DOUBLE_WALL && !$$6.isFaceSturdy($$1, $$5, $$4)) {
               return $$0.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, $$4.getOpposite());
            }

            if ($$8 == BellAttachType.SINGLE_WALL && $$9.getOpposite() == $$4 && $$6.isFaceSturdy($$1, $$5, $$0.getValue(FACING))) {
               return $$0.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
            }
         }

         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      Direction $$3 = getConnectedDirection($$0).getOpposite();
      return $$3 == Direction.UP ? Block.canSupportCenter($$1, $$2.above(), Direction.DOWN) : FaceAttachedHorizontalDirectionalBlock.canAttach($$1, $$2, $$3);
   }

   private static Direction getConnectedDirection(BlockState $$0) {
      switch ((BellAttachType)$$0.getValue(ATTACHMENT)) {
         case FLOOR:
            return Direction.UP;
         case CEILING:
            return Direction.DOWN;
         default:
            return ((Direction)$$0.getValue(FACING)).getOpposite();
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, ATTACHMENT, POWERED);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new BellBlockEntity($$0, $$1);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return createTickerHelper($$2, BlockEntityType.BELL, $$0.isClientSide() ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   public BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }
}
