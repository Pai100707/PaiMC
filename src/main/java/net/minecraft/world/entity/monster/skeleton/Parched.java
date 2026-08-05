package net.minecraft.world.entity.monster.skeleton;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Parched extends AbstractSkeleton {
   public Parched(net.minecraft.world.entity.EntityType<? extends AbstractSkeleton> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected AbstractArrow getArrow(ItemStack $$0, float $$1, ItemStack $$2) {
      AbstractArrow $$3 = super.getArrow($$0, $$1, $$2);
      if ($$3 instanceof Arrow) {
         ((Arrow)$$3).addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600));
      }

      return $$3;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.PARCHED_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.PARCHED_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.PARCHED_DEATH;
   }

   @Override
   SoundEvent getStepSound() {
      return SoundEvents.PARCHED_STEP;
   }

   @Override
   protected int getHardAttackInterval() {
      return 50;
   }

   @Override
   protected int getAttackInterval() {
      return 70;
   }

   @Override
   public boolean canBeAffected(MobEffectInstance $$0) {
      return $$0.getEffect() == MobEffects.WEAKNESS ? false : super.canBeAffected($$0);
   }
}
