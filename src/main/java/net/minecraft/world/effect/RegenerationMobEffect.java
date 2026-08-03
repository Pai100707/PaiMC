package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

class RegenerationMobEffect extends net.minecraft.world.effect.MobEffect {
   protected RegenerationMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      if ($$1.getHealth() < $$1.getMaxHealth()) {
         $$1.heal(1.0F);
      }

      return true;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
      int $$2 = 50 >> $$1;
      return $$2 > 0 ? $$0 % $$2 == 0 : true;
   }
}
