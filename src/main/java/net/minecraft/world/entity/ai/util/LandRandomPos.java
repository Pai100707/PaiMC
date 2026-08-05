package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class LandRandomPos {
   
   public static Vec3 getPos(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2) {
      return getPos($$0, $$1, $$2, $$0::getWalkTargetValue);
   }

   
   public static Vec3 getPos(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, ToDoubleFunction<BlockPos> $$3) {
      boolean $$4 = GoalUtils.mobRestricted($$0, $$1);
      return RandomPos.generateRandomPos(() -> {
         BlockPos $$4x = RandomPos.generateRandomDirection($$0.getRandom(), $$1, $$2);
         BlockPos $$5 = generateRandomPosTowardDirection($$0, $$1, $$4, $$4x);
         return $$5 == null ? null : movePosUpOutOfSolid($$0, $$5);
      }, $$3);
   }

   
   public static Vec3 getPosTowards(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, Vec3 $$3) {
      Vec3 $$4 = $$3.subtract($$0.getX(), $$0.getY(), $$0.getZ());
      boolean $$5 = GoalUtils.mobRestricted($$0, $$1);
      return getPosInDirection($$0, 0.0, $$1, $$2, $$4, $$5);
   }

   
   public static Vec3 getPosAway(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, Vec3 $$3) {
      return getPosAway($$0, 0.0, $$1, $$2, $$3);
   }

   
   public static Vec3 getPosAway(net.minecraft.world.entity.PathfinderMob $$0, double $$1, double $$2, int $$3, Vec3 $$4) {
      Vec3 $$5 = $$0.position().subtract($$4);
      if ($$5.length() == 0.0) {
         $$5 = new Vec3($$0.getRandom().nextDouble() - 0.5, 0.0, $$0.getRandom().nextDouble() - 0.5);
      }

      boolean $$6 = GoalUtils.mobRestricted($$0, $$2);
      return getPosInDirection($$0, $$1, $$2, $$3, $$5, $$6);
   }

   
   private static Vec3 getPosInDirection(net.minecraft.world.entity.PathfinderMob $$0, double $$1, double $$2, int $$3, Vec3 $$4, boolean $$5) {
      return RandomPos.generateRandomPos($$0, () -> {
         BlockPos $$6 = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), $$1, $$2, $$3, 0, $$4.x, $$4.z, (float) (Math.PI / 2));
         if ($$6 == null) {
            return null;
         } else {
            BlockPos $$7 = generateRandomPosTowardDirection($$0, $$2, $$5, $$6);
            return $$7 == null ? null : movePosUpOutOfSolid($$0, $$7);
         }
      });
   }

   
   public static BlockPos movePosUpOutOfSolid(net.minecraft.world.entity.PathfinderMob $$0, BlockPos $$1) {
      $$1 = RandomPos.moveUpOutOfSolid($$1, $$0.level().getMaxY(), $$1x -> GoalUtils.isSolid($$0, $$1x));
      return !GoalUtils.isWater($$0, $$1) && !GoalUtils.hasMalus($$0, $$1) ? $$1 : null;
   }

   
   public static BlockPos generateRandomPosTowardDirection(net.minecraft.world.entity.PathfinderMob $$0, double $$1, boolean $$2, BlockPos $$3) {
      BlockPos $$4 = RandomPos.generateRandomPosTowardDirection($$0, $$1, $$0.getRandom(), $$3);
      return !GoalUtils.isOutsideLimits($$4, $$0) && !GoalUtils.isRestricted($$2, $$0, $$4) && !GoalUtils.isNotStable($$0.getNavigation(), $$4) ? $$4 : null;
   }
}
