package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;

public class RestrictSunGoal extends Goal {
   private final net.minecraft.world.entity.PathfinderMob mob;

   public RestrictSunGoal(net.minecraft.world.entity.PathfinderMob $$0) {
      this.mob = $$0;
   }

   @Override
   public boolean canUse() {
      return this.mob.level().isBrightOutside()
         && this.mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()
         && GoalUtils.hasGroundPathNavigation(this.mob);
   }

   @Override
   public void start() {
      if (this.mob.getNavigation() instanceof GroundPathNavigation $$0) {
         $$0.setAvoidSun(true);
      }
   }

   @Override
   public void stop() {
      if (GoalUtils.hasGroundPathNavigation(this.mob) && this.mob.getNavigation() instanceof GroundPathNavigation $$0) {
         $$0.setAvoidSun(false);
      }
   }
}
