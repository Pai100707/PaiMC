package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class DefaultRandomPos {
   
   public static Vec3 getPos(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2) {
      boolean $$3 = GoalUtils.mobRestricted($$0, $$1);
      return RandomPos.generateRandomPos($$0, () -> {
         BlockPos $$4 = RandomPos.generateRandomDirection($$0.getRandom(), $$1, $$2);
         return generateRandomPosTowardDirection($$0, $$1, $$3, $$4);
      });
   }

   
   public static Vec3 getPosTowards(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, Vec3 $$3, double $$4) {
      Vec3 $$5 = $$3.subtract($$0.getX(), $$0.getY(), $$0.getZ());
      boolean $$6 = GoalUtils.mobRestricted($$0, $$1);
      return RandomPos.generateRandomPos($$0, () -> {
         BlockPos $$6x = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), 0.0, $$1, $$2, 0, $$5.x, $$5.z, $$4);
         return $$6x == null ? null : generateRandomPosTowardDirection($$0, $$1, $$6, $$6x);
      });
   }

   
   public static Vec3 getPosAway(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, Vec3 $$3) {
      Vec3 $$4 = $$0.position().subtract($$3);
      boolean $$5 = GoalUtils.mobRestricted($$0, $$1);
      return RandomPos.generateRandomPos($$0, () -> {
         BlockPos $$5x = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), 0.0, $$1, $$2, 0, $$4.x, $$4.z, (float) (Math.PI / 2));
         return $$5x == null ? null : generateRandomPosTowardDirection($$0, $$1, $$5, $$5x);
      });
   }

   
   private static BlockPos generateRandomPosTowardDirection(net.minecraft.world.entity.PathfinderMob $$0, int $$1, boolean $$2, BlockPos $$3) {
      BlockPos $$4 = RandomPos.generateRandomPosTowardDirection($$0, $$1, $$0.getRandom(), $$3);
      return !GoalUtils.isOutsideLimits($$4, $$0)
            && !GoalUtils.isRestricted($$2, $$0, $$4)
            && !GoalUtils.isNotStable($$0.getNavigation(), $$4)
            && !GoalUtils.hasMalus($$0, $$4)
         ? $$4
         : null;
   }
}
