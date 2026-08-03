package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class WitherMobEffect extends net.minecraft.world.effect.MobEffect {
   public static final int DAMAGE_INTERVAL = 40;

   protected WitherMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      $$1.hurtServer($$0, $$1.damageSources().wither(), 1.0F);
      return true;
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
      int $$2 = 40 >> $$1;
      return $$2 > 0 ? $$0 % $$2 == 0 : true;
   }
}
