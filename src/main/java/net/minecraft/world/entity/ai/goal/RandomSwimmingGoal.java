package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.phys.Vec3;

public class RandomSwimmingGoal extends RandomStrollGoal {
   public RandomSwimmingGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   
   @Override
   protected Vec3 getPosition() {
      return BehaviorUtils.getRandomSwimmablePos(this.mob, 10, 7);
   }
}
