package net.minecraft.world.entity.ai.goal.target;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.raid.Raider;
import org.jspecify.annotations.Nullable;

public class NearestHealableRaiderTargetGoal<T extends net.minecraft.world.entity.LivingEntity> extends NearestAttackableTargetGoal<T> {
   private static final int DEFAULT_COOLDOWN = 200;
   private int cooldown = 0;

   public NearestHealableRaiderTargetGoal(Raider $$0, Class<T> $$1, boolean $$2, @Nullable TargetingConditions.Selector $$3) {
      super($$0, $$1, 500, $$2, false, $$3);
   }

   public int getCooldown() {
      return this.cooldown;
   }

   public void decrementCooldown() {
      this.cooldown--;
   }

   @Override
   public boolean canUse() {
      if (this.cooldown > 0 || !this.mob.getRandom().nextBoolean()) {
         return false;
      } else if (!((Raider)this.mob).hasActiveRaid()) {
         return false;
      } else {
         this.findTarget();
         return this.target != null;
      }
   }

   @Override
   public void start() {
      this.cooldown = reducedTickDelay(200);
      super.start();
   }
}
