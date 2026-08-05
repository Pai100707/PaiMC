package net.minecraft.world.entity.monster.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Spider extends Monster {
   private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);
   private static final float SPIDER_SPECIAL_EFFECT_CHANCE = 0.1F;

   public Spider(net.minecraft.world.entity.EntityType<? extends Spider> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Armadillo.class, 6.0F, 1.0, 1.2, $$0 -> !((Armadillo)$$0).isScared()));
      this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(4, new Spider.SpiderAttackGoal(this));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new Spider.SpiderTargetGoal<>(this, Player.class));
      this.targetSelector.addGoal(3, new Spider.SpiderTargetGoal<>(this, IronGolem.class));
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new WallClimberNavigation(this, $$0);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_FLAGS_ID, (byte)0);
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         this.setClimbing(this.horizontalCollision);
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.3F);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.SPIDER_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.SPIDER_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.SPIDER_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
   }

   @Override
   public boolean onClimbable() {
      return this.isClimbing();
   }

   @Override
   public void makeStuckInBlock(BlockState $$0, Vec3 $$1) {
      if (!$$0.is(Blocks.COBWEB)) {
         super.makeStuckInBlock($$0, $$1);
      }
   }

   @Override
   public boolean canBeAffected(MobEffectInstance $$0) {
      return $$0.is(MobEffects.POISON) ? false : super.canBeAffected($$0);
   }

   public boolean isClimbing() {
      return ((Byte)this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
   }

   public void setClimbing(boolean $$0) {
      byte $$1 = (Byte)this.entityData.get(DATA_FLAGS_ID);
      if ($$0) {
         $$1 = (byte)($$1 | 1);
      } else {
         $$1 = (byte)($$1 & -2);
      }

      this.entityData.set(DATA_FLAGS_ID, $$1);
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
      RandomSource $$4 = $$0.getRandom();
      if ($$4.nextInt(100) == 0) {
         Skeleton $$5 = net.minecraft.world.entity.EntityType.SKELETON.create(this.level(), net.minecraft.world.entity.EntitySpawnReason.JOCKEY);
         if ($$5 != null) {
            $$5.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            $$5.finalizeSpawn($$0, $$1, $$2, null);
            $$5.startRiding(this, false, false);
         }
      }

      if ($$3 == null) {
         $$3 = new Spider.SpiderEffectsGroupData();
         if ($$0.getDifficulty() == Difficulty.HARD && $$4.nextFloat() < 0.1F * $$1.getSpecialMultiplier()) {
            ((Spider.SpiderEffectsGroupData)$$3).setRandomEffect($$4);
         }
      }

      if ($$3 instanceof Spider.SpiderEffectsGroupData $$6) {
         Holder<MobEffect> $$7 = $$6.effect;
         if ($$7 != null) {
            this.addEffect(new MobEffectInstance($$7, -1));
         }
      }

      return $$3;
   }

   @Override
   public Vec3 getVehicleAttachmentPoint(net.minecraft.world.entity.Entity $$0) {
      return $$0.getBbWidth() <= this.getBbWidth() ? new Vec3(0.0, 0.3125 * this.getScale(), 0.0) : super.getVehicleAttachmentPoint($$0);
   }

   static class SpiderAttackGoal extends MeleeAttackGoal {
      public SpiderAttackGoal(Spider $$0) {
         super($$0, 1.0, true);
      }

      @Override
      public boolean canUse() {
         return super.canUse() && !this.mob.isVehicle();
      }

      @Override
      public boolean canContinueToUse() {
         float $$0 = this.mob.getLightLevelDependentMagicValue();
         if ($$0 >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
            this.mob.setTarget(null);
            return false;
         } else {
            return super.canContinueToUse();
         }
      }
   }

   public static class SpiderEffectsGroupData implements net.minecraft.world.entity.SpawnGroupData {
      
      public Holder<MobEffect> effect;

      public void setRandomEffect(RandomSource $$0) {
         int $$1 = $$0.nextInt(5);
         if ($$1 <= 1) {
            this.effect = MobEffects.SPEED;
         } else if ($$1 <= 2) {
            this.effect = MobEffects.STRENGTH;
         } else if ($$1 <= 3) {
            this.effect = MobEffects.REGENERATION;
         } else if ($$1 <= 4) {
            this.effect = MobEffects.INVISIBILITY;
         }
      }
   }

   static class SpiderTargetGoal<T extends net.minecraft.world.entity.LivingEntity> extends NearestAttackableTargetGoal<T> {
      public SpiderTargetGoal(Spider $$0, Class<T> $$1) {
         super($$0, $$1, true);
      }

      @Override
      public boolean canUse() {
         float $$0 = this.mob.getLightLevelDependentMagicValue();
         return $$0 >= 0.5F ? false : super.canUse();
      }
   }
}
