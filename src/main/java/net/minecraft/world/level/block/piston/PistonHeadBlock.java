package net.minecraft.world.level.block.piston;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PistonHeadBlock extends DirectionalBlock {
   public static final MapCodec<PistonHeadBlock> CODEC = simpleCodec(PistonHeadBlock::new);
   public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;
   public static final BooleanProperty SHORT = BlockStateProperties.SHORT;
   public static final int PLATFORM_THICKNESS = 4;
   private static final VoxelShape SHAPE_PLATFORM = Block.boxZ(16.0, 0.0, 4.0);
   private static final Map<Direction, VoxelShape> SHAPES_SHORT = Shapes.rotateAll(Shapes.or(SHAPE_PLATFORM, Block.boxZ(4.0, 4.0, 16.0)));
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateAll(Shapes.or(SHAPE_PLATFORM, Block.boxZ(4.0, 4.0, 20.0)));

   @Override
   protected MapCodec<PistonHeadBlock> codec() {
      return CODEC;
   }

   public PistonHeadBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT).setValue(SHORT, false));
   }

   @Override
   protected boolean useShapeForLightOcclusion(BlockState $$0) {
      return true;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return ($$0.getValue(SHORT) ? SHAPES_SHORT : SHAPES).get($$0.getValue(FACING));
   }

   private boolean isFittingBase(BlockState $$0, BlockState $$1) {
      Block $$2 = $$0.getValue(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
      return $$1.is($$2) && $$1.getValue(PistonBaseBlock.EXTENDED) && $$1.getValue(FACING) == $$0.getValue(FACING);
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      if (!$$0.isClientSide() && $$3.preventsBlockDrops()) {
         BlockPos $$4 = $$1.relative(((Direction)$$2.getValue(FACING)).getOpposite());
         if (this.isFittingBase($$2, $$0.getBlockState($$4))) {
            $$0.destroyBlock($$4, false);
         }
      }

      return super.playerWillDestroy($$0, $$1, $$2, $$3);
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      BlockPos $$4 = $$2.relative(((Direction)$$0.getValue(FACING)).getOpposite());
      if (this.isFittingBase($$0, $$1.getBlockState($$4))) {
         $$1.destroyBlock($$4, true);
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
      return $$4.getOpposite() == $$0.getValue(FACING) && !$$0.canSurvive($$1, $$3)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2.relative(((Direction)$$0.getValue(FACING)).getOpposite()));
      return this.isFittingBase($$0, $$3) || $$3.is(Blocks.MOVING_PISTON) && $$3.getValue(FACING) == $$0.getValue(FACING);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      if ($$0.canSurvive($$1, $$2)) {
         $$1.neighborChanged(
            $$2.relative(((Direction)$$0.getValue(FACING)).getOpposite()),
            $$3,
            ExperimentalRedstoneUtils.withFront($$4, ((Direction)$$0.getValue(FACING)).getOpposite())
         );
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack($$2.getValue(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
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
      $$0.add(FACING, TYPE, SHORT);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
