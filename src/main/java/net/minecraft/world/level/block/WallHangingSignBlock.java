package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class WallHangingSignBlock extends SignBlock {
   public static final MapCodec<WallHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WoodType.CODEC.fieldOf("wood_type").forGetter(SignBlock::type), propertiesCodec()).apply($$0, WallHangingSignBlock::new)
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   private static final Map<Axis, VoxelShape> SHAPES_PLANK = Shapes.rotateHorizontalAxis(Block.column(16.0, 4.0, 14.0, 16.0));
   private static final Map<Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(Shapes.or(SHAPES_PLANK.get(Axis.Z), Block.column(14.0, 2.0, 0.0, 10.0)));

   @Override
   public MapCodec<WallHangingSignBlock> codec() {
      return CODEC;
   }

   public WallHangingSignBlock(WoodType $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1.sound($$0.hangingSignSoundType()));
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      return (InteractionResult)($$2.getBlockEntity($$3) instanceof SignBlockEntity $$7 && this.shouldTryToChainAnotherHangingSign($$1, $$4, $$6, $$7, $$0)
         ? InteractionResult.PASS
         : super.useItemOn($$0, $$1, $$2, $$3, $$4, $$5, $$6));
   }

   private boolean shouldTryToChainAnotherHangingSign(BlockState $$0, Player $$1, BlockHitResult $$2, SignBlockEntity $$3, ItemStack $$4) {
      return !$$3.canExecuteClickCommands($$3.isFacingFrontText($$1), $$1) && $$4.getItem() instanceof HangingSignItem && !this.isHittingEditableSide($$2, $$0);
   }

   private boolean isHittingEditableSide(BlockHitResult $$0, BlockState $$1) {
      return $$0.getDirection().getAxis() == ((Direction)$$1.getValue(FACING)).getAxis();
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get(((Direction)$$0.getValue(FACING)).getAxis());
   }

   @Override
   protected VoxelShape getBlockSupportShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return this.getShape($$0, $$1, $$2, CollisionContext.empty());
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES_PLANK.get(((Direction)$$0.getValue(FACING)).getAxis());
   }

   public boolean canPlace(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      Direction $$3 = ((Direction)$$0.getValue(FACING)).getClockWise();
      Direction $$4 = ((Direction)$$0.getValue(FACING)).getCounterClockWise();
      return this.canAttachTo($$1, $$0, $$2.relative($$3), $$4) || this.canAttachTo($$1, $$0, $$2.relative($$4), $$3);
   }

   public boolean canAttachTo(net.minecraft.world.level.LevelReader $$0, BlockState $$1, BlockPos $$2, Direction $$3) {
      BlockState $$4 = $$0.getBlockState($$2);
      return $$4.is(BlockTags.WALL_HANGING_SIGNS)
         ? ((Direction)$$4.getValue(FACING)).getAxis().test($$1.getValue(FACING))
         : $$4.isFaceSturdy($$0, $$2, $$3, SupportType.FULL);
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = this.defaultBlockState();
      FluidState $$2 = $$0.getLevel().getFluidState($$0.getClickedPos());
      net.minecraft.world.level.LevelReader $$3 = $$0.getLevel();
      BlockPos $$4 = $$0.getClickedPos();

      for (Direction $$5 : $$0.getNearestLookingDirections()) {
         if ($$5.getAxis().isHorizontal() && !$$5.getAxis().test($$0.getClickedFace())) {
            Direction $$6 = $$5.getOpposite();
            $$1 = $$1.setValue(FACING, $$6);
            if ($$1.canSurvive($$3, $$4) && this.canPlace($$1, $$3, $$4)) {
               return $$1.setValue(WATERLOGGED, $$2.getType() == Fluids.WATER);
            }
         }
      }

      return null;
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
      return $$4.getAxis() == ((Direction)$$0.getValue(FACING)).getClockWise().getAxis() && !$$0.canSurvive($$1, $$3)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   public float getYRotationDegrees(BlockState $$0) {
      return ((Direction)$$0.getValue(FACING)).toYRot();
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
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new HangingSignBlockEntity($$0, $$1);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return createTickerHelper($$2, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
   }
}
