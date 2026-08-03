package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.monster.Creeper;
import org.jspecify.annotations.Nullable;

public class SwellGoal extends Goal {
   private final Creeper creeper;
   @Nullable
   private net.minecraft.world.entity.LivingEntity target;

   public SwellGoal(Creeper $$0) {
      this.creeper = $$0;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      net.minecraft.world.entity.LivingEntity $$0 = this.creeper.getTarget();
      return this.creeper.getSwellDir() > 0 || $$0 != null && this.creeper.distanceToSqr($$0) < 9.0;
   }

   @Override
   public void start() {
      this.creeper.getNavigation().stop();
      this.target = this.creeper.getTarget();
   }

   @Override
   public void stop() {
      this.target = null;
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.target == null) {
         this.creeper.setSwellDir(-1);
      } else if (this.creeper.distanceToSqr(this.target) > 49.0) {
         this.creeper.setSwellDir(-1);
      } else if (!this.creeper.getSensing().hasLineOfSight(this.target)) {
         this.creeper.setSwellDir(-1);
      } else {
         this.creeper.setSwellDir(1);
      }
   }
}
