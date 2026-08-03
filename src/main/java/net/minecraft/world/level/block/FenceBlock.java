package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceBlock extends CrossCollisionBlock {
   public static final MapCodec<FenceBlock> CODEC = simpleCodec(FenceBlock::new);
   private final Function<BlockState, VoxelShape> occlusionShapes;

   @Override
   public MapCodec<FenceBlock> codec() {
      return CODEC;
   }

   public FenceBlock(BlockBehaviour.Properties $$0) {
      super(4.0F, 16.0F, 4.0F, 16.0F, 24.0F, $$0);
      this.registerDefaultState(
         this.stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(WATERLOGGED, false)
      );
      this.occlusionShapes = this.makeShapes(4.0F, 16.0F, 2.0F, 6.0F, 15.0F);
   }

   @Override
   protected VoxelShape getOcclusionShape(BlockState $$0) {
      return this.occlusionShapes.apply($$0);
   }

   @Override
   protected VoxelShape getVisualShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getShape($$0, $$1, $$2, $$3);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   public boolean connectsTo(BlockState $$0, boolean $$1, Direction $$2) {
      Block $$3 = $$0.getBlock();
      boolean $$4 = this.isSameFence($$0);
      boolean $$5 = $$3 instanceof FenceGateBlock && FenceGateBlock.connectsToDirection($$0, $$2);
      return !isExceptionForConnection($$0) && $$1 || $$4 || $$5;
   }

   private boolean isSameFence(BlockState $$0) {
      return $$0.is(BlockTags.FENCES) && $$0.is(BlockTags.WOODEN_FENCES) == this.defaultBlockState().is(BlockTags.WOODEN_FENCES);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      return (InteractionResult)(!$$1.isClientSide() ? LeadItem.bindPlayerMobs($$3, $$1, $$2) : InteractionResult.PASS);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      net.minecraft.world.level.BlockGetter $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      FluidState $$3 = $$0.getLevel().getFluidState($$0.getClickedPos());
      BlockPos $$4 = $$2.north();
      BlockPos $$5 = $$2.east();
      BlockPos $$6 = $$2.south();
      BlockPos $$7 = $$2.west();
      BlockState $$8 = $$1.getBlockState($$4);
      BlockState $$9 = $$1.getBlockState($$5);
      BlockState $$10 = $$1.getBlockState($$6);
      BlockState $$11 = $$1.getBlockState($$7);
      return super.getStateForPlacement($$0)
         .setValue(NORTH, this.connectsTo($$8, $$8.isFaceSturdy($$1, $$4, Direction.SOUTH), Direction.SOUTH))
         .setValue(EAST, this.connectsTo($$9, $$9.isFaceSturdy($$1, $$5, Direction.WEST), Direction.WEST))
         .setValue(SOUTH, this.connectsTo($$10, $$10.isFaceSturdy($$1, $$6, Direction.NORTH), Direction.NORTH))
         .setValue(WEST, this.connectsTo($$11, $$11.isFaceSturdy($$1, $$7, Direction.EAST), Direction.EAST))
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
         ? $$0.setValue(PROPERTY_BY_DIRECTION.get($$4), this.connectsTo($$6, $$6.isFaceSturdy($$1, $$5, $$4.getOpposite()), $$4.getOpposite()))
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
   }
}
