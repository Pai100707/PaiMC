package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedstoneWallTorchBlock extends RedstoneTorchBlock {
   public static final MapCodec<RedstoneWallTorchBlock> CODEC = simpleCodec(RedstoneWallTorchBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   @Override
   public MapCodec<RedstoneWallTorchBlock> codec() {
      return CODEC;
   }

   protected RedstoneWallTorchBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, true));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return WallTorchBlock.getShape($$0);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return WallTorchBlock.canSurvive($$1, $$2, $$0.getValue(FACING));
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
      return $$4.getOpposite() == $$0.getValue(FACING) && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : $$0;
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = Blocks.WALL_TORCH.getStateForPlacement($$0);
      return $$1 == null ? null : this.defaultBlockState().setValue(FACING, (Direction)$$1.getValue(FACING));
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LIT)) {
         Direction $$4 = ((Direction)$$0.getValue(FACING)).getOpposite();
         double $$5 = 0.27;
         double $$6 = $$2.getX() + 0.5 + ($$3.nextDouble() - 0.5) * 0.2 + 0.27 * $$4.getStepX();
         double $$7 = $$2.getY() + 0.7 + ($$3.nextDouble() - 0.5) * 0.2 + 0.22;
         double $$8 = $$2.getZ() + 0.5 + ($$3.nextDouble() - 0.5) * 0.2 + 0.27 * $$4.getStepZ();
         $$1.addParticle(DustParticleOptions.REDSTONE, $$6, $$7, $$8, 0.0, 0.0, 0.0);
      }
   }

   @Override
   protected boolean hasNeighborSignal(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      Direction $$3 = ((Direction)$$2.getValue(FACING)).getOpposite();
      return $$0.hasSignal($$1.relative($$3), $$3);
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(LIT) && $$0.getValue(FACING) != $$3 ? 15 : 0;
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
      $$0.add(FACING, LIT);
   }

   
   @Override
   protected Orientation randomOrientation(net.minecraft.world.level.Level $$0, BlockState $$1) {
      return ExperimentalRedstoneUtils.initialOrientation($$0, ((Direction)$$1.getValue(FACING)).getOpposite(), Direction.UP);
   }
}
