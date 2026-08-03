package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public abstract class FaceAttachedHorizontalDirectionalBlock extends HorizontalDirectionalBlock {
   public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

   protected FaceAttachedHorizontalDirectionalBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected abstract MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec();

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return canAttach($$1, $$2, getConnectedDirection($$0).getOpposite());
   }

   public static boolean canAttach(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, Direction $$2) {
      BlockPos $$3 = $$1.relative($$2);
      return $$0.getBlockState($$3).isFaceSturdy($$0, $$3, $$2.getOpposite());
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      for (Direction $$1 : $$0.getNearestLookingDirections()) {
         BlockState $$2;
         if ($$1.getAxis() == Axis.Y) {
            $$2 = this.defaultBlockState()
               .setValue(FACE, $$1 == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
               .setValue(FACING, $$0.getHorizontalDirection());
         } else {
            $$2 = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, $$1.getOpposite());
         }

         if ($$2.canSurvive($$0.getLevel(), $$0.getClickedPos())) {
            return $$2;
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
      return getConnectedDirection($$0).getOpposite() == $$4 && !$$0.canSurvive($$1, $$3)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   protected static Direction getConnectedDirection(BlockState $$0) {
      switch ((AttachFace)$$0.getValue(FACE)) {
         case CEILING:
            return Direction.DOWN;
         case FLOOR:
            return Direction.UP;
         default:
            return $$0.getValue(FACING);
      }
   }
}
