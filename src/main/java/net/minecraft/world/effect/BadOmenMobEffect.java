package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;

class BadOmenMobEffect extends net.minecraft.world.effect.MobEffect {
   protected BadOmenMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
      return true;
   }

   @Override
   public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
      if ($$1 instanceof ServerPlayer $$3 && !$$3.isSpectator() && $$0.getDifficulty() != Difficulty.PEACEFUL && $$0.isVillage($$3.blockPosition())) {
         Raid $$4 = $$0.getRaidAt($$3.blockPosition());
         if ($$4 == null || $$4.getRaidOmenLevel() < $$4.getMaxRaidOmenLevel()) {
            $$3.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RAID_OMEN, 600, $$2));
            $$3.setRaidOmenPosition($$3.blockPosition());
            return false;
         }
      }

      return true;
   }
}
