package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class LadderBlock extends Block implements SimpleWaterloggedBlock {
   public static final MapCodec<LadderBlock> CODEC = simpleCodec(LadderBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.boxZ(16.0, 13.0, 16.0));

   @Override
   public MapCodec<LadderBlock> codec() {
      return CODEC;
   }

   protected LadderBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(FACING));
   }

   private boolean canAttachTo(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      return $$3.isFaceSturdy($$0, $$1, $$2);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      Direction $$3 = $$0.getValue(FACING);
      return this.canAttachTo($$1, $$2.relative($$3.getOpposite()), $$3);
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
      if ($$4.getOpposite() == $$0.getValue(FACING) && !$$0.canSurvive($$1, $$3)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if ($$0.getValue(WATERLOGGED)) {
            $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
         }

         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      if (!$$0.replacingClickedOnBlock()) {
         BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos().relative($$0.getClickedFace().getOpposite()));
         if ($$1.is(this) && $$1.getValue(FACING) == $$0.getClickedFace()) {
            return null;
         }
      }

      BlockState $$2 = this.defaultBlockState();
      net.minecraft.world.level.LevelReader $$3 = $$0.getLevel();
      BlockPos $$4 = $$0.getClickedPos();
      FluidState $$5 = $$0.getLevel().getFluidState($$0.getClickedPos());

      for (Direction $$6 : $$0.getNearestLookingDirections()) {
         if ($$6.getAxis().isHorizontal()) {
            $$2 = $$2.setValue(FACING, $$6.getOpposite());
            if ($$2.canSurvive($$3, $$4)) {
               return $$2.setValue(WATERLOGGED, $$5.getType() == Fluids.WATER);
            }
         }
      }

      return null;
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, WATERLOGGED);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }
}
