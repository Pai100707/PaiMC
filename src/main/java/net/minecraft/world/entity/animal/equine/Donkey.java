package net.minecraft.world.entity.animal.equine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class Donkey extends AbstractChestedHorse {
   public Donkey(net.minecraft.world.entity.EntityType<? extends Donkey> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.DONKEY_AMBIENT;
   }

   @Override
   protected SoundEvent getAngrySound() {
      return SoundEvents.DONKEY_ANGRY;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.DONKEY_DEATH;
   }

   @Override
   protected SoundEvent getEatingSound() {
      return SoundEvents.DONKEY_EAT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.DONKEY_HURT;
   }

   @Override
   public boolean canMate(Animal $$0) {
      if ($$0 == this) {
         return false;
      } else {
         return !($$0 instanceof Donkey) && !($$0 instanceof Horse) ? false : this.canParent() && ((AbstractHorse)$$0).canParent();
      }
   }

   @Override
   protected void playJumpSound() {
      this.playSound(SoundEvents.DONKEY_JUMP, 0.4F, 1.0F);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      net.minecraft.world.entity.EntityType<? extends AbstractHorse> $$2 = $$1 instanceof Horse
         ? net.minecraft.world.entity.EntityType.MULE
         : net.minecraft.world.entity.EntityType.DONKEY;
      AbstractHorse $$3 = $$2.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$3 != null) {
         this.setOffspringAttributes($$1, $$3);
      }

      return $$3;
   }
}
