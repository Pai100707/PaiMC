package net.minecraft.world.effect;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class MobEffectUtil {
   public static Component formatDuration(net.minecraft.world.effect.MobEffectInstance $$0, float $$1, float $$2) {
      if ($$0.isInfiniteDuration()) {
         return Component.translatable("effect.duration.infinite");
      } else {
         int $$3 = Mth.floor($$0.getDuration() * $$1);
         return Component.literal(StringUtil.formatTickDuration($$3, $$2));
      }
   }

   public static boolean hasDigSpeed(LivingEntity $$0) {
      return $$0.hasEffect(net.minecraft.world.effect.MobEffects.HASTE) || $$0.hasEffect(net.minecraft.world.effect.MobEffects.CONDUIT_POWER);
   }

   public static int getDigSpeedAmplification(LivingEntity $$0) {
      int $$1 = 0;
      int $$2 = 0;
      if ($$0.hasEffect(net.minecraft.world.effect.MobEffects.HASTE)) {
         $$1 = $$0.getEffect(net.minecraft.world.effect.MobEffects.HASTE).getAmplifier();
      }

      if ($$0.hasEffect(net.minecraft.world.effect.MobEffects.CONDUIT_POWER)) {
         $$2 = $$0.getEffect(net.minecraft.world.effect.MobEffects.CONDUIT_POWER).getAmplifier();
      }

      return Math.max($$1, $$2);
   }

   public static boolean hasWaterBreathing(LivingEntity $$0) {
      return $$0.hasEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING)
         || $$0.hasEffect(net.minecraft.world.effect.MobEffects.CONDUIT_POWER)
         || $$0.hasEffect(net.minecraft.world.effect.MobEffects.BREATH_OF_THE_NAUTILUS);
   }

   public static boolean shouldEffectsRefillAirsupply(LivingEntity $$0) {
      return !$$0.hasEffect(net.minecraft.world.effect.MobEffects.BREATH_OF_THE_NAUTILUS)
         || $$0.hasEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING)
         || $$0.hasEffect(net.minecraft.world.effect.MobEffects.CONDUIT_POWER);
   }

   public static List<ServerPlayer> addEffectToPlayersAround(
      ServerLevel $$0, @Nullable Entity $$1, Vec3 $$2, double $$3, net.minecraft.world.effect.MobEffectInstance $$4, int $$5
   ) {
      Holder<net.minecraft.world.effect.MobEffect> $$6 = $$4.getEffect();
      List<ServerPlayer> $$7 = $$0.getPlayers(
         $$6x -> $$6x.gameMode.isSurvival()
            && ($$1 == null || !$$1.isAlliedTo($$6x))
            && $$2.closerThan($$6x.position(), $$3)
            && (!$$6x.hasEffect($$6) || $$6x.getEffect($$6).getAmplifier() < $$4.getAmplifier() || $$6x.getEffect($$6).endsWithin($$5 - 1))
      );
      $$7.forEach($$2x -> $$2x.addEffect(new net.minecraft.world.effect.MobEffectInstance($$4), $$1));
      return $$7;
   }
}
