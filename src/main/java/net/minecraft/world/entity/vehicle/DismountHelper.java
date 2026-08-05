package net.minecraft.world.entity.vehicle;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DismountHelper {
   public static int[][] offsetsForDirection(Direction $$0) {
      Direction $$1 = $$0.getClockWise();
      Direction $$2 = $$1.getOpposite();
      Direction $$3 = $$0.getOpposite();
      return new int[][]{
         {$$1.getStepX(), $$1.getStepZ()},
         {$$2.getStepX(), $$2.getStepZ()},
         {$$3.getStepX() + $$1.getStepX(), $$3.getStepZ() + $$1.getStepZ()},
         {$$3.getStepX() + $$2.getStepX(), $$3.getStepZ() + $$2.getStepZ()},
         {$$0.getStepX() + $$1.getStepX(), $$0.getStepZ() + $$1.getStepZ()},
         {$$0.getStepX() + $$2.getStepX(), $$0.getStepZ() + $$2.getStepZ()},
         {$$3.getStepX(), $$3.getStepZ()},
         {$$0.getStepX(), $$0.getStepZ()}
      };
   }

   public static boolean isBlockFloorValid(double $$0) {
      return !Double.isInfinite($$0) && $$0 < 1.0;
   }

   public static boolean canDismountTo(CollisionGetter $$0, net.minecraft.world.entity.LivingEntity $$1, AABB $$2) {
      for (VoxelShape $$4 : $$0.getBlockCollisions($$1, $$2)) {
         if (!$$4.isEmpty()) {
            return false;
         }
      }

      return $$0.getWorldBorder().isWithinBounds($$2);
   }

   public static boolean canDismountTo(CollisionGetter $$0, Vec3 $$1, net.minecraft.world.entity.LivingEntity $$2, net.minecraft.world.entity.Pose $$3) {
      return canDismountTo($$0, $$2, $$2.getLocalBoundsForPose($$3).move($$1));
   }

   public static VoxelShape nonClimbableShape(BlockGetter $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      return !$$2.is(BlockTags.CLIMBABLE) && (!($$2.getBlock() instanceof TrapDoorBlock) || !$$2.getValue(TrapDoorBlock.OPEN))
         ? $$2.getCollisionShape($$0, $$1)
         : Shapes.empty();
   }

   public static double findCeilingFrom(BlockPos $$0, int $$1, Function<BlockPos, VoxelShape> $$2) {
      MutableBlockPos $$3 = $$0.mutable();
      int $$4 = 0;

      while ($$4 < $$1) {
         VoxelShape $$5 = $$2.apply($$3);
         if (!$$5.isEmpty()) {
            return $$0.getY() + $$4 + $$5.min(Axis.Y);
         }

         $$4++;
         $$3.move(Direction.UP);
      }

      return Double.POSITIVE_INFINITY;
   }

   
   public static Vec3 findSafeDismountLocation(net.minecraft.world.entity.EntityType<?> $$0, CollisionGetter $$1, BlockPos $$2, boolean $$3) {
      if ($$3 && $$0.isBlockDangerous($$1.getBlockState($$2))) {
         return null;
      } else {
         double $$4 = $$1.getBlockFloorHeight(nonClimbableShape($$1, $$2), () -> nonClimbableShape($$1, $$2.below()));
         if (!isBlockFloorValid($$4)) {
            return null;
         } else if ($$3 && $$4 <= 0.0 && $$0.isBlockDangerous($$1.getBlockState($$2.below()))) {
            return null;
         } else {
            Vec3 $$5 = Vec3.upFromBottomCenterOf($$2, $$4);
            AABB $$6 = $$0.getDimensions().makeBoundingBox($$5);

            for (VoxelShape $$8 : $$1.getBlockCollisions(null, $$6)) {
               if (!$$8.isEmpty()) {
                  return null;
               }
            }

            if ($$0 != net.minecraft.world.entity.EntityType.PLAYER
               || !$$1.getBlockState($$2).is(BlockTags.INVALID_SPAWN_INSIDE) && !$$1.getBlockState($$2.above()).is(BlockTags.INVALID_SPAWN_INSIDE)) {
               return !$$1.getWorldBorder().isWithinBounds($$6) ? null : $$5;
            } else {
               return null;
            }
         }
      }
   }
}
