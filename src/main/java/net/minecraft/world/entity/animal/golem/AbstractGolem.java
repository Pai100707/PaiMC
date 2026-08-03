package net.minecraft.world.entity.animal.golem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class AbstractGolem extends net.minecraft.world.entity.PathfinderMob {
   protected AbstractGolem(net.minecraft.world.entity.EntityType<? extends AbstractGolem> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return null;
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return null;
   }

   @Override
   public int getAmbientSoundInterval() {
      return 120;
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return false;
   }
}
