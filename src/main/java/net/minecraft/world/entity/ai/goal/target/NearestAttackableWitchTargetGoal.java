package net.minecraft.world.entity.ai.goal.target;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.raid.Raider;
import org.jspecify.annotations.Nullable;

public class NearestAttackableWitchTargetGoal<T extends net.minecraft.world.entity.LivingEntity> extends NearestAttackableTargetGoal<T> {
   private boolean canAttack = true;

   public NearestAttackableWitchTargetGoal(Raider $$0, Class<T> $$1, int $$2, boolean $$3, boolean $$4, @Nullable TargetingConditions.Selector $$5) {
      super($$0, $$1, $$2, $$3, $$4, $$5);
   }

   public void setCanAttack(boolean $$0) {
      this.canAttack = $$0;
   }

   @Override
   public boolean canUse() {
      return this.canAttack && super.canUse();
   }
}
