package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class AirAndWaterRandomPos {
   
   public static Vec3 getPos(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, int $$3, double $$4, double $$5, double $$6) {
      boolean $$7 = GoalUtils.mobRestricted($$0, $$1);
      return RandomPos.generateRandomPos($$0, () -> generateRandomPos($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7));
   }

   
   public static BlockPos generateRandomPos(
      net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, int $$3, double $$4, double $$5, double $$6, boolean $$7
   ) {
      BlockPos $$8 = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), 0.0, $$1, $$2, $$3, $$4, $$5, $$6);
      if ($$8 == null) {
         return null;
      } else {
         BlockPos $$9 = RandomPos.generateRandomPosTowardDirection($$0, $$1, $$0.getRandom(), $$8);
         if (!GoalUtils.isOutsideLimits($$9, $$0) && !GoalUtils.isRestricted($$7, $$0, $$9)) {
            $$9 = RandomPos.moveUpOutOfSolid($$9, $$0.level().getMaxY(), $$1x -> GoalUtils.isSolid($$0, $$1x));
            return GoalUtils.hasMalus($$0, $$9) ? null : $$9;
         } else {
            return null;
         }
      }
   }
}
