package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

class AbsorptionMobEffect extends net.minecraft.world.effect.MobEffect {
   protected AbsorptionMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      return $$1.getAbsorptionAmount() > 0.0F;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
      return true;
   }

   @Override
   public void onEffectStarted(LivingEntity $$0, int $$1) {
      super.onEffectStarted($$0, $$1);
      $$0.setAbsorptionAmount(Math.max($$0.getAbsorptionAmount(), (float)(4 * (1 + $$1))));
   }
}
