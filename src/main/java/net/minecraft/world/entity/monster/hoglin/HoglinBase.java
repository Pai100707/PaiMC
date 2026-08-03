package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
   int ATTACK_ANIMATION_DURATION = 10;
   float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2F;

   int getAttackAnimationRemainingTicks();

   static boolean hurtAndThrowTarget(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, net.minecraft.world.entity.LivingEntity $$2) {
      float $$3 = (float)$$1.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float $$4;
      if (!$$1.isBaby() && (int)$$3 > 0) {
         $$4 = $$3 / 2.0F + $$0.random.nextInt((int)$$3);
      } else {
         $$4 = $$3;
      }

      DamageSource $$6 = $$1.damageSources().mobAttack($$1);
      boolean $$7 = $$2.hurtServer($$0, $$6, $$4);
      if ($$7) {
         EnchantmentHelper.doPostAttackEffects($$0, $$2, $$6);
         if (!$$1.isBaby()) {
            throwTarget($$1, $$2);
         }
      }

      return $$7;
   }

   static void throwTarget(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      double $$2 = $$0.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      double $$3 = $$1.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
      double $$4 = $$2 - $$3;
      if (!($$4 <= 0.0)) {
         double $$5 = $$1.getX() - $$0.getX();
         double $$6 = $$1.getZ() - $$0.getZ();
         float $$7 = $$0.level().random.nextInt(21) - 10;
         double $$8 = $$4 * ($$0.level().random.nextFloat() * 0.5F + 0.2F);
         Vec3 $$9 = new Vec3($$5, 0.0, $$6).normalize().scale($$8).yRot($$7);
         double $$10 = $$4 * $$0.level().random.nextFloat() * 0.5;
         $$1.push($$9.x, $$10, $$9.z);
         $$1.hurtMarked = true;
      }
   }
}
