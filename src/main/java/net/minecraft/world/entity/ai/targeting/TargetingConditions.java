package net.minecraft.world.entity.ai.targeting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;

public class TargetingConditions {
   public static final TargetingConditions DEFAULT = forCombat();
   private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0;
   private final boolean isCombat;
   private double range = -1.0;
   private boolean checkLineOfSight = true;
   private boolean testInvisible = true;
   
   private TargetingConditions.Selector selector;

   private TargetingConditions(boolean $$0) {
      this.isCombat = $$0;
   }

   public static TargetingConditions forCombat() {
      return new TargetingConditions(true);
   }

   public static TargetingConditions forNonCombat() {
      return new TargetingConditions(false);
   }

   public TargetingConditions copy() {
      TargetingConditions $$0 = this.isCombat ? forCombat() : forNonCombat();
      $$0.range = this.range;
      $$0.checkLineOfSight = this.checkLineOfSight;
      $$0.testInvisible = this.testInvisible;
      $$0.selector = this.selector;
      return $$0;
   }

   public TargetingConditions range(double $$0) {
      this.range = $$0;
      return this;
   }

   public TargetingConditions ignoreLineOfSight() {
      this.checkLineOfSight = false;
      return this;
   }

   public TargetingConditions ignoreInvisibilityTesting() {
      this.testInvisible = false;
      return this;
   }

   public TargetingConditions selector(TargetingConditions.Selector $$0) {
      this.selector = $$0;
      return this;
   }

   public boolean test(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, net.minecraft.world.entity.LivingEntity $$2) {
      if ($$1 == $$2) {
         return false;
      } else if (!$$2.canBeSeenByAnyone()) {
         return false;
      } else if (this.selector != null && !this.selector.test($$2, $$0)) {
         return false;
      } else {
         if ($$1 == null) {
            if (this.isCombat && (!$$2.canBeSeenAsEnemy() || $$0.getDifficulty() == Difficulty.PEACEFUL)) {
               return false;
            }
         } else {
            if (this.isCombat && (!$$1.canAttack($$2) || !$$1.canAttackType($$2.getType()) || $$1.isAlliedTo($$2))) {
               return false;
            }

            if (this.range > 0.0) {
               double $$3 = this.testInvisible ? $$2.getVisibilityPercent($$1) : 1.0;
               double $$4 = Math.max(this.range * $$3, 2.0);
               double $$5 = $$1.distanceToSqr($$2.getX(), $$2.getY(), $$2.getZ());
               if ($$5 > $$4 * $$4) {
                  return false;
               }
            }

            if (this.checkLineOfSight && $$1 instanceof net.minecraft.world.entity.Mob $$6 && !$$6.getSensing().hasLineOfSight($$2)) {
               return false;
            }
         }

         return true;
      }
   }

   @FunctionalInterface
   public interface Selector {
      boolean test(net.minecraft.world.entity.LivingEntity var1, ServerLevel var2);
   }
}
