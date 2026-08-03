package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

public class InteractGoal extends LookAtPlayerGoal {
   public InteractGoal(net.minecraft.world.entity.Mob $$0, Class<? extends net.minecraft.world.entity.LivingEntity> $$1, float $$2) {
      super($$0, $$1, $$2);
      this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
   }

   public InteractGoal(net.minecraft.world.entity.Mob $$0, Class<? extends net.minecraft.world.entity.LivingEntity> $$1, float $$2, float $$3) {
      super($$0, $$1, $$2, $$3);
      this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
   }
}
