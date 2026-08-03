package net.minecraft.world.entity.animal.squid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class GlowSquid extends Squid {
   private static final EntityDataAccessor<Integer> DATA_DARK_TICKS_REMAINING = SynchedEntityData.defineId(GlowSquid.class, EntityDataSerializers.INT);
   private static final int DEFAULT_DARK_TICKS_REMAINING = 0;

   public GlowSquid(net.minecraft.world.entity.EntityType<? extends GlowSquid> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected ParticleOptions getInkParticle() {
      return ParticleTypes.GLOW_SQUID_INK;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_DARK_TICKS_REMAINING, 0);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return net.minecraft.world.entity.EntityType.GLOW_SQUID.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
   }

   @Override
   protected SoundEvent getSquirtSound() {
      return SoundEvents.GLOW_SQUID_SQUIRT;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.GLOW_SQUID_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.GLOW_SQUID_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.GLOW_SQUID_DEATH;
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("DarkTicksRemaining", this.getDarkTicksRemaining());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setDarkTicks($$0.getIntOr("DarkTicksRemaining", 0));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      int $$0 = this.getDarkTicksRemaining();
      if ($$0 > 0) {
         this.setDarkTicks($$0 - 1);
      }

      this.level().addParticle(ParticleTypes.GLOW, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.0, 0.0);
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      boolean $$3 = super.hurtServer($$0, $$1, $$2);
      if ($$3) {
         this.setDarkTicks(100);
      }

      return $$3;
   }

   private void setDarkTicks(int $$0) {
      this.entityData.set(DATA_DARK_TICKS_REMAINING, $$0);
   }

   public int getDarkTicksRemaining() {
      return (Integer)this.entityData.get(DATA_DARK_TICKS_REMAINING);
   }

   public static boolean checkGlowSquidSpawnRules(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> $$0,
      ServerLevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return $$3.getY() <= $$1.getSeaLevel() - 33 && $$1.getRawBrightness($$3, 0) == 0 && $$1.getBlockState($$3).is(Blocks.WATER);
   }
}
