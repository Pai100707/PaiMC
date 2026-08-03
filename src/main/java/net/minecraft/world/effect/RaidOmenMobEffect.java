package net.minecraft.world.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

class RaidOmenMobEffect extends net.minecraft.world.effect.MobEffect {
   protected RaidOmenMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1, ParticleOptions $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
      return $$0 == 1;
   }

   @Override
   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      if ($$1 instanceof ServerPlayer $$3 && !$$1.isSpectator()) {
         BlockPos $$4 = $$3.getRaidOmenPosition();
         if ($$4 != null) {
            $$0.getRaids().createOrExtendRaid($$3, $$4);
            $$3.clearRaidOmenPosition();
            return false;
         }
      }

      return true;
   }
}
