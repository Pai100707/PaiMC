package net.minecraft.world.entity.projectile.arrow;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SpectralArrow extends AbstractArrow {
   private static final int DEFAULT_DURATION = 200;
   private int duration = 200;

   public SpectralArrow(net.minecraft.world.entity.EntityType<? extends SpectralArrow> $$0, Level $$1) {
      super($$0, $$1);
   }

   public SpectralArrow(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2, ItemStack $$3) {
      super(net.minecraft.world.entity.EntityType.SPECTRAL_ARROW, $$1, $$0, $$2, $$3);
   }

   public SpectralArrow(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4, ItemStack $$5) {
      super(net.minecraft.world.entity.EntityType.SPECTRAL_ARROW, $$1, $$2, $$3, $$0, $$4, $$5);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide() && !this.isInGround()) {
         this.level().addParticle(SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0F), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
      }
   }

   @Override
   protected void doPostHurtEffects(net.minecraft.world.entity.LivingEntity $$0) {
      super.doPostHurtEffects($$0);
      MobEffectInstance $$1 = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
      $$0.addEffect($$1, this.getEffectSource());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.duration = $$0.getIntOr("Duration", 200);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("Duration", this.duration);
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(Items.SPECTRAL_ARROW);
   }
}
