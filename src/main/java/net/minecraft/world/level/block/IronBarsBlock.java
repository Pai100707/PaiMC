package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IronBarsBlock extends CrossCollisionBlock {
   public static final MapCodec<IronBarsBlock> CODEC = simpleCodec(IronBarsBlock::new);

   @Override
   public MapCodec<? extends IronBarsBlock> codec() {
      return CODEC;
   }

   protected IronBarsBlock(BlockBehaviour.Properties $$0) {
      super(2.0F, 16.0F, 2.0F, 16.0F, 16.0F, $$0);
      this.registerDefaultState(
         this.stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(WATERLOGGED, false)
      );
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      net.minecraft.world.level.BlockGetter $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      FluidState $$3 = $$0.getLevel().getFluidState($$0.getClickedPos());
      BlockPos $$4 = $$2.north();
      BlockPos $$5 = $$2.south();
      BlockPos $$6 = $$2.west();
      BlockPos $$7 = $$2.east();
      BlockState $$8 = $$1.getBlockState($$4);
      BlockState $$9 = $$1.getBlockState($$5);
      BlockState $$10 = $$1.getBlockState($$6);
      BlockState $$11 = $$1.getBlockState($$7);
      return this.defaultBlockState()
         .setValue(NORTH, this.attachsTo($$8, $$8.isFaceSturdy($$1, $$4, Direction.SOUTH)))
         .setValue(SOUTH, this.attachsTo($$9, $$9.isFaceSturdy($$1, $$5, Direction.NORTH)))
         .setValue(WEST, this.attachsTo($$10, $$10.isFaceSturdy($$1, $$6, Direction.EAST)))
         .setValue(EAST, this.attachsTo($$11, $$11.isFaceSturdy($$1, $$7, Direction.WEST)))
         .setValue(WATERLOGGED, $$3.getType() == Fluids.WATER);
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

      return $$4.getAxis().isHorizontal()
         ? $$0.setValue(PROPERTY_BY_DIRECTION.get($$4), this.attachsTo($$6, $$6.isFaceSturdy($$1, $$5, $$4.getOpposite())))
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected VoxelShape getVisualShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return Shapes.empty();
   }

   @Override
   protected boolean skipRendering(BlockState $$0, BlockState $$1, Direction $$2) {
      if ($$1.is(this) || $$1.is(BlockTags.BARS) && $$0.is(BlockTags.BARS) && $$1.hasProperty(PROPERTY_BY_DIRECTION.get($$2.getOpposite()))) {
         if (!$$2.getAxis().isHorizontal()) {
            return true;
         }

         if ($$0.getValue(PROPERTY_BY_DIRECTION.get($$2)) && $$1.getValue(PROPERTY_BY_DIRECTION.get($$2.getOpposite()))) {
            return true;
         }
      }

      return super.skipRendering($$0, $$1, $$2);
   }

   public final boolean attachsTo(BlockState $$0, boolean $$1) {
      return !isExceptionForConnection($$0) && $$1 || $$0.getBlock() instanceof IronBarsBlock || $$0.is(BlockTags.WALLS);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
   }
}
