package net.minecraft.world.entity.ai.goal.target;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jspecify.annotations.Nullable;

public class NonTameRandomTargetGoal<T extends net.minecraft.world.entity.LivingEntity> extends NearestAttackableTargetGoal<T> {
   private final net.minecraft.world.entity.TamableAnimal tamableMob;

   public NonTameRandomTargetGoal(net.minecraft.world.entity.TamableAnimal $$0, Class<T> $$1, boolean $$2, @Nullable TargetingConditions.Selector $$3) {
      super($$0, $$1, 10, $$2, false, $$3);
      this.tamableMob = $$0;
   }

   @Override
   public boolean canUse() {
      return !this.tamableMob.isTame() && super.canUse();
   }

   @Override
   public boolean canContinueToUse() {
      return this.targetConditions != null ? this.targetConditions.test(getServerLevel(this.mob), this.mob, this.target) : super.canContinueToUse();
   }
}
