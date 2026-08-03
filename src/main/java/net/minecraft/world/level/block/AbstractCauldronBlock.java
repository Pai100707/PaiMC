package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteraction.InteractionMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractCauldronBlock extends Block {
   protected static final int FLOOR_LEVEL = 4;
   private static final VoxelShape SHAPE_INSIDE = Block.column(12.0, 4.0, 16.0);
   protected static final VoxelShape SHAPE = (VoxelShape)Util.make(
      () -> {
         int $$0 = 4;
         int $$1 = 3;
         int $$2 = 2;
         return Shapes.join(
            Shapes.block(),
            Shapes.or(Block.column(16.0, 8.0, 0.0, 3.0), new VoxelShape[]{Block.column(8.0, 16.0, 0.0, 3.0), Block.column(12.0, 0.0, 3.0), SHAPE_INSIDE}),
            BooleanOp.ONLY_FIRST
         );
      }
   );
   protected final InteractionMap interactions;

   @Override
   protected abstract MapCodec<? extends AbstractCauldronBlock> codec();

   public AbstractCauldronBlock(BlockBehaviour.Properties $$0, InteractionMap $$1) {
      super($$0);
      this.interactions = $$1;
   }

   protected double getContentHeight(BlockState $$0) {
      return 0.0;
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      CauldronInteraction $$7 = (CauldronInteraction)this.interactions.map().get($$0.getItem());
      return $$7.interact($$1, $$2, $$3, $$4, $$5, $$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected VoxelShape getInteractionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return SHAPE_INSIDE;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   public abstract boolean isFull(BlockState var1);

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      BlockPos $$4 = PointedDripstoneBlock.findStalactiteTipAboveCauldron($$1, $$2);
      if ($$4 != null) {
         Fluid $$5 = PointedDripstoneBlock.getCauldronFillFluidType($$1, $$4);
         if ($$5 != Fluids.EMPTY && this.canReceiveStalactiteDrip($$5)) {
            this.receiveStalactiteDrip($$0, $$1, $$2, $$5);
         }
      }
   }

   protected boolean canReceiveStalactiteDrip(Fluid $$0) {
      return false;
   }

   protected void receiveStalactiteDrip(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Fluid $$3) {
   }
}
