package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION
      .entrySet()
      .stream()
      .filter($$0 -> $$0.getKey().getAxis().isHorizontal())
      .collect(Util.toMap());
   private final Function<BlockState, VoxelShape> collisionShapes;
   private final Function<BlockState, VoxelShape> shapes;

   protected CrossCollisionBlock(float $$0, float $$1, float $$2, float $$3, float $$4, BlockBehaviour.Properties $$5) {
      super($$5);
      this.collisionShapes = this.makeShapes($$0, $$4, $$2, 0.0F, $$4);
      this.shapes = this.makeShapes($$0, $$1, $$2, 0.0F, $$3);
   }

   @Override
   protected abstract MapCodec<? extends CrossCollisionBlock> codec();

   protected Function<BlockState, VoxelShape> makeShapes(float $$0, float $$1, float $$2, float $$3, float $$4) {
      VoxelShape $$5 = Block.column($$0, 0.0, $$1);
      Map<Direction, VoxelShape> $$6 = Shapes.rotateHorizontal(Block.boxZ($$2, $$3, $$4, 0.0, 8.0));
      return this.getShapeForEachState($$2x -> {
         VoxelShape $$3x = $$5;

         for (Entry<Direction, BooleanProperty> $$4x : PROPERTY_BY_DIRECTION.entrySet()) {
            if ($$2x.getValue($$4x.getValue())) {
               $$3x = Shapes.or($$3x, $$6.get($$4x.getKey()));
            }
         }

         return $$3x;
      }, new Property[]{WATERLOGGED});
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return !$$0.getValue(WATERLOGGED);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.collisionShapes.apply($$0);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      switch ($$1) {
         case CLOCKWISE_180:
            return $$0.setValue(NORTH, $$0.getValue(SOUTH))
               .setValue(EAST, $$0.getValue(WEST))
               .setValue(SOUTH, $$0.getValue(NORTH))
               .setValue(WEST, $$0.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return $$0.setValue(NORTH, $$0.getValue(EAST))
               .setValue(EAST, $$0.getValue(SOUTH))
               .setValue(SOUTH, $$0.getValue(WEST))
               .setValue(WEST, $$0.getValue(NORTH));
         case CLOCKWISE_90:
            return $$0.setValue(NORTH, $$0.getValue(WEST))
               .setValue(EAST, $$0.getValue(NORTH))
               .setValue(SOUTH, $$0.getValue(EAST))
               .setValue(WEST, $$0.getValue(SOUTH));
         default:
            return $$0;
      }
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      switch ($$1) {
         case LEFT_RIGHT:
            return $$0.setValue(NORTH, $$0.getValue(SOUTH)).setValue(SOUTH, $$0.getValue(NORTH));
         case FRONT_BACK:
            return $$0.setValue(EAST, $$0.getValue(WEST)).setValue(WEST, $$0.getValue(EAST));
         default:
            return super.mirror($$0, $$1);
      }
   }
}
