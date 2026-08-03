package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class HungerMobEffect extends net.minecraft.world.effect.MobEffect {
   protected HungerMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      if ($$1 instanceof Player $$3) {
         $$3.causeFoodExhaustion(0.005F * ($$2 + 1));
      }

      return true;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
      return true;
   }
}
