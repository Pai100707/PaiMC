package net.minecraft.world.effect;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;

class OozingMobEffect extends net.minecraft.world.effect.MobEffect {
   private static final int RADIUS_TO_CHECK_SLIMES = 2;
   public static final int SLIME_SIZE = 2;
   private final ToIntFunction<RandomSource> spawnedCount;

   protected OozingMobEffect(net.minecraft.world.effect.MobEffectCategory $$0, int $$1, ToIntFunction<RandomSource> $$2) {
      super($$0, $$1, ParticleTypes.ITEM_SLIME);
      this.spawnedCount = $$2;
   }

   @VisibleForTesting
   protected static int numberOfSlimesToSpawn(int $$0, net.minecraft.world.effect.OozingMobEffect.NearbySlimes $$1, int $$2) {
      return $$0 < 1 ? $$2 : Mth.clamp(0, $$0 - $$1.count($$0), $$2);
   }

   @Override
   public void onMobRemoved(ServerLevel $$0, LivingEntity $$1, int $$2, RemovalReason $$3) {
      if ($$3 == RemovalReason.KILLED) {
         int $$4 = this.spawnedCount.applyAsInt($$1.getRandom());
         int $$5 = (Integer)$$0.getGameRules().get(GameRules.MAX_ENTITY_CRAMMING);
         int $$6 = numberOfSlimesToSpawn($$5, net.minecraft.world.effect.OozingMobEffect.NearbySlimes.closeTo($$1), $$4);

         for (int $$7 = 0; $$7 < $$6; $$7++) {
            this.spawnSlimeOffspring($$1.level(), $$1.getX(), $$1.getY() + 0.5, $$1.getZ());
         }
      }
   }

   private void spawnSlimeOffspring(Level $$0, double $$1, double $$2, double $$3) {
      Slime $$4 = (Slime)EntityType.SLIME.create($$0, EntitySpawnReason.TRIGGERED);
      if ($$4 != null) {
         $$4.setSize(2, true);
         $$4.snapTo($$1, $$2, $$3, $$0.getRandom().nextFloat() * 360.0F, 0.0F);
         $$0.addFreshEntity($$4);
      }
   }

   @FunctionalInterface
   protected interface NearbySlimes {
      int count(int var1);

      static net.minecraft.world.effect.OozingMobEffect.NearbySlimes closeTo(LivingEntity $$0) {
         return $$1 -> {
            List<Slime> $$2 = new ArrayList<>();
            $$0.level().getEntities(EntityType.SLIME, $$0.getBoundingBox().inflate(2.0), $$1x -> $$1x != $$0, $$2, $$1);
            return $$2.size();
         };
      }
   }
}
