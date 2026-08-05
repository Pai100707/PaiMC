package net.minecraft.world.entity.ai.goal;

import java.util.List;
import net.minecraft.world.entity.animal.Animal;

public class FollowParentGoal extends Goal {
   public static final int HORIZONTAL_SCAN_RANGE = 8;
   public static final int VERTICAL_SCAN_RANGE = 4;
   public static final int DONT_FOLLOW_IF_CLOSER_THAN = 3;
   private final Animal animal;
   
   private Animal parent;
   private final double speedModifier;
   private int timeToRecalcPath;

   public FollowParentGoal(Animal $$0, double $$1) {
      this.animal = $$0;
      this.speedModifier = $$1;
   }

   @Override
   public boolean canUse() {
      if (this.animal.getAge() >= 0) {
         return false;
      } else {
         List<? extends Animal> $$0 = this.animal.level().getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(8.0, 4.0, 8.0));
         Animal $$1 = null;
         double $$2 = Double.MAX_VALUE;

         for (Animal $$3 : $$0) {
            if ($$3.getAge() >= 0) {
               double $$4 = this.animal.distanceToSqr($$3);
               if (!($$4 > $$2)) {
                  $$2 = $$4;
                  $$1 = $$3;
               }
            }
         }

         if ($$1 == null) {
            return false;
         } else if ($$2 < 9.0) {
            return false;
         } else {
            this.parent = $$1;
            return true;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (this.animal.getAge() >= 0) {
         return false;
      } else if (!this.parent.isAlive()) {
         return false;
      } else {
         double $$0 = this.animal.distanceToSqr(this.parent);
         return !($$0 < 9.0) && !($$0 > 256.0);
      }
   }

   @Override
   public void start() {
      this.timeToRecalcPath = 0;
   }

   @Override
   public void stop() {
      this.parent = null;
   }

   @Override
   public void tick() {
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = this.adjustedTickDelay(10);
         this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
      }
   }
}
