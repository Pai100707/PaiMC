package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RandomStrollGoal extends Goal {
   public static final int DEFAULT_INTERVAL = 120;
   protected final net.minecraft.world.entity.PathfinderMob mob;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;
   protected final double speedModifier;
   protected int interval;
   protected boolean forceTrigger;
   private final boolean checkNoActionTime;

   public RandomStrollGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
      this($$0, $$1, 120);
   }

   public RandomStrollGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, int $$2) {
      this($$0, $$1, $$2, true);
   }

   public RandomStrollGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, int $$2, boolean $$3) {
      this.mob = $$0;
      this.speedModifier = $$1;
      this.interval = $$2;
      this.checkNoActionTime = $$3;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.mob.hasControllingPassenger()) {
         return false;
      } else {
         if (!this.forceTrigger) {
            if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
               return false;
            }

            if (this.mob.getRandom().nextInt(reducedTickDelay(this.interval)) != 0) {
               return false;
            }
         }

         Vec3 $$0 = this.getPosition();
         if ($$0 == null) {
            return false;
         } else {
            this.wantedX = $$0.x;
            this.wantedY = $$0.y;
            this.wantedZ = $$0.z;
            this.forceTrigger = false;
            return true;
         }
      }
   }

   @Nullable
   protected Vec3 getPosition() {
      return DefaultRandomPos.getPos(this.mob, 10, 7);
   }

   @Override
   public boolean canContinueToUse() {
      return !this.mob.getNavigation().isDone() && !this.mob.hasControllingPassenger();
   }

   @Override
   public void start() {
      this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
   }

   @Override
   public void stop() {
      this.mob.getNavigation().stop();
      super.stop();
   }

   public void trigger() {
      this.forceTrigger = true;
   }

   public void setInterval(int $$0) {
      this.interval = $$0;
   }
}
