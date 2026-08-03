package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StairBlock extends Block implements SimpleWaterloggedBlock {
   public static final MapCodec<StairBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BlockState.CODEC.fieldOf("base_state").forGetter($$0x -> $$0x.baseState), propertiesCodec()).apply($$0, StairBlock::new)
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
   public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE_OUTER = Shapes.or(Block.column(16.0, 0.0, 8.0), Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 8.0));
   private static final VoxelShape SHAPE_STRAIGHT = Shapes.or(SHAPE_OUTER, Shapes.rotate(SHAPE_OUTER, OctahedralGroup.BLOCK_ROT_Y_90));
   private static final VoxelShape SHAPE_INNER = Shapes.or(SHAPE_STRAIGHT, Shapes.rotate(SHAPE_STRAIGHT, OctahedralGroup.BLOCK_ROT_Y_90));
   private static final Map<Direction, VoxelShape> SHAPE_BOTTOM_OUTER = Shapes.rotateHorizontal(SHAPE_OUTER);
   private static final Map<Direction, VoxelShape> SHAPE_BOTTOM_STRAIGHT = Shapes.rotateHorizontal(SHAPE_STRAIGHT);
   private static final Map<Direction, VoxelShape> SHAPE_BOTTOM_INNER = Shapes.rotateHorizontal(SHAPE_INNER);
   private static final Map<Direction, VoxelShape> SHAPE_TOP_OUTER = Shapes.rotateHorizontal(SHAPE_OUTER, OctahedralGroup.INVERT_Y);
   private static final Map<Direction, VoxelShape> SHAPE_TOP_STRAIGHT = Shapes.rotateHorizontal(SHAPE_STRAIGHT, OctahedralGroup.INVERT_Y);
   private static final Map<Direction, VoxelShape> SHAPE_TOP_INNER = Shapes.rotateHorizontal(SHAPE_INNER, OctahedralGroup.INVERT_Y);
   private final Block base;
   protected final BlockState baseState;

   @Override
   public MapCodec<? extends StairBlock> codec() {
      return CODEC;
   }

   protected StairBlock(BlockState $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.registerDefaultState(
         this.stateDefinition
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(HALF, Half.BOTTOM)
            .setValue(SHAPE, StairsShape.STRAIGHT)
            .setValue(WATERLOGGED, false)
      );
      this.base = $$0.getBlock();
      this.baseState = $$0;
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return true;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      boolean $$4 = $$0.getValue(HALF) == Half.BOTTOM;
      Direction $$5 = $$0.getValue(FACING);

      Map var10000 = switch ((StairsShape)$$0.getValue(SHAPE)) {
         case STRAIGHT -> $$4 ? SHAPE_BOTTOM_STRAIGHT : SHAPE_TOP_STRAIGHT;
         case OUTER_LEFT, OUTER_RIGHT -> $$4 ? SHAPE_BOTTOM_OUTER : SHAPE_TOP_OUTER;
         case INNER_RIGHT, INNER_LEFT -> $$4 ? SHAPE_BOTTOM_INNER : SHAPE_TOP_INNER;
      };

      return (VoxelShape)var10000.get(switch ((StairsShape)$$0.getValue(SHAPE)) {
         case STRAIGHT, OUTER_LEFT, INNER_RIGHT -> $$5;
         case INNER_LEFT -> $$5.getCounterClockWise();
         case OUTER_RIGHT -> $$5.getClockWise();
      });
   }

   @Override
   public float getExplosionResistance() {
      return this.base.getExplosionResistance();
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      Direction $$1 = $$0.getClickedFace();
      BlockPos $$2 = $$0.getClickedPos();
      FluidState $$3 = $$0.getLevel().getFluidState($$2);
      BlockState $$4 = this.defaultBlockState()
         .setValue(FACING, $$0.getHorizontalDirection())
         .setValue(HALF, $$1 != Direction.DOWN && ($$1 == Direction.UP || !($$0.getClickLocation().y - $$2.getY() > 0.5)) ? Half.BOTTOM : Half.TOP)
         .setValue(WATERLOGGED, $$3.getType() == Fluids.WATER);
      return $$4.setValue(SHAPE, getStairsShape($$4, $$0.getLevel(), $$2));
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

      return $$4.getAxis().isHorizontal() ? $$0.setValue(SHAPE, getStairsShape($$0, $$1, $$3)) : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   private static StairsShape getStairsShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      Direction $$3 = $$0.getValue(FACING);
      BlockState $$4 = $$1.getBlockState($$2.relative($$3));
      if (isStairs($$4) && $$0.getValue(HALF) == $$4.getValue(HALF)) {
         Direction $$5 = $$4.getValue(FACING);
         if ($$5.getAxis() != ((Direction)$$0.getValue(FACING)).getAxis() && canTakeShape($$0, $$1, $$2, $$5.getOpposite())) {
            if ($$5 == $$3.getCounterClockWise()) {
               return StairsShape.OUTER_LEFT;
            }

            return StairsShape.OUTER_RIGHT;
         }
      }

      BlockState $$6 = $$1.getBlockState($$2.relative($$3.getOpposite()));
      if (isStairs($$6) && $$0.getValue(HALF) == $$6.getValue(HALF)) {
         Direction $$7 = $$6.getValue(FACING);
         if ($$7.getAxis() != ((Direction)$$0.getValue(FACING)).getAxis() && canTakeShape($$0, $$1, $$2, $$7)) {
            if ($$7 == $$3.getCounterClockWise()) {
               return StairsShape.INNER_LEFT;
            }

            return StairsShape.INNER_RIGHT;
         }
      }

      return StairsShape.STRAIGHT;
   }

   private static boolean canTakeShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      BlockState $$4 = $$1.getBlockState($$2.relative($$3));
      return !isStairs($$4) || $$4.getValue(FACING) != $$0.getValue(FACING) || $$4.getValue(HALF) != $$0.getValue(HALF);
   }

   public static boolean isStairs(BlockState $$0) {
      return $$0.getBlock() instanceof StairBlock;
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      Direction $$2 = $$0.getValue(FACING);
      StairsShape $$3 = $$0.getValue(SHAPE);
      switch ($$1) {
         case LEFT_RIGHT:
            if ($$2.getAxis() == Axis.Z) {
               switch ($$3) {
                  case OUTER_LEFT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case INNER_RIGHT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case INNER_LEFT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case OUTER_RIGHT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                  default:
                     return $$0.rotate(Rotation.CLOCKWISE_180);
               }
            }
            break;
         case FRONT_BACK:
            if ($$2.getAxis() == Axis.X) {
               switch ($$3) {
                  case STRAIGHT:
                     return $$0.rotate(Rotation.CLOCKWISE_180);
                  case OUTER_LEFT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case INNER_RIGHT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case INNER_LEFT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case OUTER_RIGHT:
                     return $$0.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
               }
            }
      }

      return super.mirror($$0, $$1);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, HALF, SHAPE, WATERLOGGED);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
