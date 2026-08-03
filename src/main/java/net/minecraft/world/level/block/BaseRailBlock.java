package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class BaseRailBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE_FLAT = Block.column(16.0, 0.0, 2.0);
   private static final VoxelShape SHAPE_SLOPE = Block.column(16.0, 0.0, 8.0);
   private final boolean isStraight;

   public static boolean isRail(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return isRail($$0.getBlockState($$1));
   }

   public static boolean isRail(BlockState $$0) {
      return $$0.is(BlockTags.RAILS) && $$0.getBlock() instanceof BaseRailBlock;
   }

   protected BaseRailBlock(boolean $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.isStraight = $$0;
   }

   @Override
   protected abstract MapCodec<? extends BaseRailBlock> codec();

   public boolean isStraight() {
      return this.isStraight;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return $$0.getValue(this.getShapeProperty()).isSlope() ? SHAPE_SLOPE : SHAPE_FLAT;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return canSupportRigidBlock($$1, $$2.below());
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$3.is($$0.getBlock())) {
         this.updateState($$0, $$1, $$2, $$4);
      }
   }

   protected BlockState updateState(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, boolean $$3) {
      $$0 = this.updateDir($$1, $$2, $$0, true);
      if (this.isStraight) {
         $$1.neighborChanged($$0, $$2, this, null, $$3);
      }

      return $$0;
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      if (!$$1.isClientSide() && $$1.getBlockState($$2).is(this)) {
         RailShape $$6 = $$0.getValue(this.getShapeProperty());
         if (shouldBeRemoved($$2, $$1, $$6)) {
            dropResources($$0, $$1, $$2);
            $$1.removeBlock($$2, $$5);
         } else {
            this.updateState($$0, $$1, $$2, $$3);
         }
      }
   }

   private static boolean shouldBeRemoved(BlockPos $$0, net.minecraft.world.level.Level $$1, RailShape $$2) {
      if (!canSupportRigidBlock($$1, $$0.below())) {
         return true;
      } else {
         switch ($$2) {
            case ASCENDING_EAST:
               return !canSupportRigidBlock($$1, $$0.east());
            case ASCENDING_WEST:
               return !canSupportRigidBlock($$1, $$0.west());
            case ASCENDING_NORTH:
               return !canSupportRigidBlock($$1, $$0.north());
            case ASCENDING_SOUTH:
               return !canSupportRigidBlock($$1, $$0.south());
            default:
               return false;
         }
      }
   }

   protected void updateState(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3) {
   }

   protected BlockState updateDir(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      if ($$0.isClientSide()) {
         return $$2;
      } else {
         RailShape $$4 = $$2.getValue(this.getShapeProperty());
         return new RailState($$0, $$1, $$2).place($$0.hasNeighborSignal($$1), $$3, $$4).getState();
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (!$$3) {
         if ($$0.getValue(this.getShapeProperty()).isSlope()) {
            $$1.updateNeighborsAt($$2.above(), this);
         }

         if (this.isStraight) {
            $$1.updateNeighborsAt($$2, this);
            $$1.updateNeighborsAt($$2.below(), this);
         }
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      boolean $$2 = $$1.getType() == Fluids.WATER;
      BlockState $$3 = super.defaultBlockState();
      Direction $$4 = $$0.getHorizontalDirection();
      boolean $$5 = $$4 == Direction.EAST || $$4 == Direction.WEST;
      return $$3.setValue(this.getShapeProperty(), $$5 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(WATERLOGGED, $$2);
   }

   public abstract Property<RailShape> getShapeProperty();

   protected RailShape rotate(RailShape $$0, Rotation $$1) {
      return switch ($$1) {
         case CLOCKWISE_180 -> {
            switch ($$0) {
               case ASCENDING_EAST:
                  yield RailShape.ASCENDING_WEST;
               case ASCENDING_WEST:
                  yield RailShape.ASCENDING_EAST;
               case ASCENDING_NORTH:
                  yield RailShape.ASCENDING_SOUTH;
               case ASCENDING_SOUTH:
                  yield RailShape.ASCENDING_NORTH;
               case NORTH_SOUTH:
                  yield RailShape.NORTH_SOUTH;
               case EAST_WEST:
                  yield RailShape.EAST_WEST;
               case SOUTH_EAST:
                  yield RailShape.NORTH_WEST;
               case SOUTH_WEST:
                  yield RailShape.NORTH_EAST;
               case NORTH_WEST:
                  yield RailShape.SOUTH_EAST;
               case NORTH_EAST:
                  yield RailShape.SOUTH_WEST;
               default:
                  throw new MatchException(null, null);
            }
         }
         case COUNTERCLOCKWISE_90 -> {
            switch ($$0) {
               case ASCENDING_EAST:
                  yield RailShape.ASCENDING_NORTH;
               case ASCENDING_WEST:
                  yield RailShape.ASCENDING_SOUTH;
               case ASCENDING_NORTH:
                  yield RailShape.ASCENDING_WEST;
               case ASCENDING_SOUTH:
                  yield RailShape.ASCENDING_EAST;
               case NORTH_SOUTH:
                  yield RailShape.EAST_WEST;
               case EAST_WEST:
                  yield RailShape.NORTH_SOUTH;
               case SOUTH_EAST:
                  yield RailShape.NORTH_EAST;
               case SOUTH_WEST:
                  yield RailShape.SOUTH_EAST;
               case NORTH_WEST:
                  yield RailShape.SOUTH_WEST;
               case NORTH_EAST:
                  yield RailShape.NORTH_WEST;
               default:
                  throw new MatchException(null, null);
            }
         }
         case CLOCKWISE_90 -> {
            switch ($$0) {
               case ASCENDING_EAST:
                  yield RailShape.ASCENDING_SOUTH;
               case ASCENDING_WEST:
                  yield RailShape.ASCENDING_NORTH;
               case ASCENDING_NORTH:
                  yield RailShape.ASCENDING_EAST;
               case ASCENDING_SOUTH:
                  yield RailShape.ASCENDING_WEST;
               case NORTH_SOUTH:
                  yield RailShape.EAST_WEST;
               case EAST_WEST:
                  yield RailShape.NORTH_SOUTH;
               case SOUTH_EAST:
                  yield RailShape.SOUTH_WEST;
               case SOUTH_WEST:
                  yield RailShape.NORTH_WEST;
               case NORTH_WEST:
                  yield RailShape.NORTH_EAST;
               case NORTH_EAST:
                  yield RailShape.SOUTH_EAST;
               default:
                  throw new MatchException(null, null);
            }
         }
         default -> $$0;
      };
   }

   protected RailShape mirror(RailShape $$0, Mirror $$1) {
      return switch ($$1) {
         case LEFT_RIGHT -> {
            switch ($$0) {
               case ASCENDING_NORTH:
                  yield RailShape.ASCENDING_SOUTH;
               case ASCENDING_SOUTH:
                  yield RailShape.ASCENDING_NORTH;
               case NORTH_SOUTH:
               case EAST_WEST:
               default:
                  yield $$0;
               case SOUTH_EAST:
                  yield RailShape.NORTH_EAST;
               case SOUTH_WEST:
                  yield RailShape.NORTH_WEST;
               case NORTH_WEST:
                  yield RailShape.SOUTH_WEST;
               case NORTH_EAST:
                  yield RailShape.SOUTH_EAST;
            }
         }
         case FRONT_BACK -> {
            switch ($$0) {
               case ASCENDING_EAST:
                  yield RailShape.ASCENDING_WEST;
               case ASCENDING_WEST:
                  yield RailShape.ASCENDING_EAST;
               case ASCENDING_NORTH:
               case ASCENDING_SOUTH:
               case NORTH_SOUTH:
               case EAST_WEST:
               default:
                  yield $$0;
               case SOUTH_EAST:
                  yield RailShape.SOUTH_WEST;
               case SOUTH_WEST:
                  yield RailShape.SOUTH_EAST;
               case NORTH_WEST:
                  yield RailShape.NORTH_EAST;
               case NORTH_EAST:
                  yield RailShape.NORTH_WEST;
            }
         }
         default -> $$0;
      };
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
}
