package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HoverRandomPos {
   @Nullable
   public static Vec3 getPos(net.minecraft.world.entity.PathfinderMob $$0, int $$1, int $$2, double $$3, double $$4, float $$5, int $$6, int $$7) {
      boolean $$8 = GoalUtils.mobRestricted($$0, $$1);
      return RandomPos.generateRandomPos(
         $$0,
         () -> {
            BlockPos $$9 = RandomPos.generateRandomDirectionWithinRadians($$0.getRandom(), 0.0, $$1, $$2, 0, $$3, $$4, $$5);
            if ($$9 == null) {
               return null;
            } else {
               BlockPos $$10 = LandRandomPos.generateRandomPosTowardDirection($$0, $$1, $$8, $$9);
               if ($$10 == null) {
                  return null;
               } else {
                  $$10 = RandomPos.moveUpToAboveSolid(
                     $$10, $$0.getRandom().nextInt($$6 - $$7 + 1) + $$7, $$0.level().getMaxY(), $$1xx -> GoalUtils.isSolid($$0, $$1xx)
                  );
                  return !GoalUtils.isWater($$0, $$10) && !GoalUtils.hasMalus($$0, $$10) ? $$10 : null;
               }
            }
         }
      );
   }
}
