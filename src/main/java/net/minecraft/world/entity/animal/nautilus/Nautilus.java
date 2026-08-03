package net.minecraft.world.entity.animal.nautilus;

import com.mojang.serialization.Dynamic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class Nautilus extends AbstractNautilus {
   private static final int NAUTILUS_TOTAL_AIR_SUPPLY = 300;

   public Nautilus(net.minecraft.world.entity.EntityType<? extends Nautilus> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected Brain.Provider<Nautilus> brainProvider() {
      return NautilusAi.brainProvider();
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      return NautilusAi.makeBrain(this.brainProvider().makeBrain($$0));
   }

   @Override
   public Brain<Nautilus> getBrain() {
      return (Brain<Nautilus>)super.getBrain();
   }

   @Nullable
   public Nautilus getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Nautilus $$2 = net.minecraft.world.entity.EntityType.NAUTILUS.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null && this.isTame()) {
         $$2.setOwnerReference(this.getOwnerReference());
         $$2.setTame(true, true);
      }

      return $$2;
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("nautilusBrain");
      this.getBrain().tick($$0, this);
      $$1.pop();
      $$1.push("nautilusActivityUpdate");
      NautilusAi.updateActivity(this);
      $$1.pop();
      super.customServerAiStep($$0);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      if (this.isBaby()) {
         return this.isUnderWater() ? SoundEvents.BABY_NAUTILUS_AMBIENT : SoundEvents.BABY_NAUTILUS_AMBIENT_ON_LAND;
      } else {
         return this.isUnderWater() ? SoundEvents.NAUTILUS_AMBIENT : SoundEvents.NAUTILUS_AMBIENT_ON_LAND;
      }
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      if (this.isBaby()) {
         return this.isUnderWater() ? SoundEvents.BABY_NAUTILUS_HURT : SoundEvents.BABY_NAUTILUS_HURT_ON_LAND;
      } else {
         return this.isUnderWater() ? SoundEvents.NAUTILUS_HURT : SoundEvents.NAUTILUS_HURT_ON_LAND;
      }
   }

   @Override
   protected SoundEvent getDeathSound() {
      if (this.isBaby()) {
         return this.isUnderWater() ? SoundEvents.BABY_NAUTILUS_DEATH : SoundEvents.BABY_NAUTILUS_DEATH_ON_LAND;
      } else {
         return this.isUnderWater() ? SoundEvents.NAUTILUS_DEATH : SoundEvents.NAUTILUS_DEATH_ON_LAND;
      }
   }

   @Override
   protected SoundEvent getDashSound() {
      return this.isUnderWater() ? SoundEvents.NAUTILUS_DASH : SoundEvents.NAUTILUS_DASH_ON_LAND;
   }

   @Override
   protected SoundEvent getDashReadySound() {
      return this.isUnderWater() ? SoundEvents.NAUTILUS_DASH_READY : SoundEvents.NAUTILUS_DASH_READY_ON_LAND;
   }

   @Override
   protected void playEatingSound() {
      SoundEvent $$0 = this.isBaby() ? SoundEvents.BABY_NAUTILUS_EAT : SoundEvents.NAUTILUS_EAT;
      this.makeSound($$0);
   }

   @Override
   protected SoundEvent getSwimSound() {
      return this.isBaby() ? SoundEvents.BABY_NAUTILUS_SWIM : SoundEvents.NAUTILUS_SWIM;
   }

   @Override
   public int getMaxAirSupply() {
      return 300;
   }

   protected void handleAirSupply(ServerLevel $$0, int $$1) {
      if (this.isAlive() && !this.isInWater()) {
         this.setAirSupply($$1 - 1);
         if (this.getAirSupply() <= -20) {
            this.setAirSupply(0);
            this.hurtServer($$0, this.damageSources().dryOut(), 2.0F);
         }
      } else {
         this.setAirSupply(300);
      }
   }

   @Override
   public void baseTick() {
      int $$0 = this.getAirSupply();
      super.baseTick();
      if (!this.isNoAi() && this.level() instanceof ServerLevel $$1) {
         this.handleAirSupply($$1, $$0);
      }
   }

   @Override
   public boolean canBeLeashed() {
      return !this.isAggravated();
   }
}
