package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

class HealOrHarmMobEffect extends net.minecraft.world.effect.InstantenousMobEffect {
   private final boolean isHarm;

   public HealOrHarmMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1, boolean $$2) {
      super($$0, $$1);
      this.isHarm = $$2;
   }

   @Override
   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      if (this.isHarm == $$1.isInvertedHealAndHarm()) {
         $$1.heal(Math.max(4 << $$2, 0));
      } else {
         $$1.hurtServer($$0, $$1.damageSources().magic(), 6 << $$2);
      }

      return true;
   }

   @Override
   public void applyInstantenousEffect(ServerLevel $$0, Entity $$1, Entity $$2, LivingEntity $$3, int $$4, double $$5) {
      if (this.isHarm == $$3.isInvertedHealAndHarm()) {
         int $$6 = (int)($$5 * (4 << $$4) + 0.5);
         $$3.heal($$6);
      } else {
         int $$7 = (int)($$5 * (6 << $$4) + 0.5);
         if ($$1 == null) {
            $$3.hurtServer($$0, $$3.damageSources().magic(), $$7);
         } else {
            $$3.hurtServer($$0, $$3.damageSources().indirectMagic($$1, $$2), $$7);
         }
      }
   }
}
