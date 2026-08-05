package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Guardian extends Monster {
   protected static final int ATTACK_TIME = 80;
   private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.INT);
   private float clientSideTailAnimation;
   private float clientSideTailAnimationO;
   private float clientSideTailAnimationSpeed;
   private float clientSideSpikesAnimation;
   private float clientSideSpikesAnimationO;
   
   private net.minecraft.world.entity.LivingEntity clientSideCachedAttackTarget;
   private int clientSideAttackTime;
   private boolean clientSideTouchedGround;
   
   protected RandomStrollGoal randomStrollGoal;

   public Guardian(net.minecraft.world.entity.EntityType<? extends Guardian> $$0, Level $$1) {
      super($$0, $$1);
      this.xpReward = 10;
      this.setPathfindingMalus(PathType.WATER, 0.0F);
      this.moveControl = new Guardian.GuardianMoveControl(this);
      this.clientSideTailAnimation = this.random.nextFloat();
      this.clientSideTailAnimationO = this.clientSideTailAnimation;
   }

   @Override
   protected void registerGoals() {
      MoveTowardsRestrictionGoal $$0 = new MoveTowardsRestrictionGoal(this, 1.0);
      this.randomStrollGoal = new RandomStrollGoal(this, 1.0, 80);
      this.goalSelector.addGoal(4, new Guardian.GuardianAttackGoal(this));
      this.goalSelector.addGoal(5, $$0);
      this.goalSelector.addGoal(7, this.randomStrollGoal);
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Guardian.class, 12.0F, 0.01F));
      this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
      this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      $$0.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      this.targetSelector
         .addGoal(
            1,
            new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.LivingEntity.class, 10, true, false, new Guardian.GuardianAttackSelector(this))
         );
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.MAX_HEALTH, 30.0);
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      return new WaterBoundPathNavigation(this, $$0);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_ID_MOVING, false);
      $$0.define(DATA_ID_ATTACK_TARGET, 0);
   }

   public boolean isMoving() {
      return (Boolean)this.entityData.get(DATA_ID_MOVING);
   }

   void setMoving(boolean $$0) {
      this.entityData.set(DATA_ID_MOVING, $$0);
   }

   public int getAttackDuration() {
      return 80;
   }

   void setActiveAttackTarget(int $$0) {
      this.entityData.set(DATA_ID_ATTACK_TARGET, $$0);
   }

   public boolean hasActiveAttackTarget() {
      return (Integer)this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
   }

   
   public net.minecraft.world.entity.LivingEntity getActiveAttackTarget() {
      if (!this.hasActiveAttackTarget()) {
         return null;
      } else if (this.level().isClientSide()) {
         if (this.clientSideCachedAttackTarget != null) {
            return this.clientSideCachedAttackTarget;
         } else {
            net.minecraft.world.entity.Entity $$0 = this.level().getEntity((Integer)this.entityData.get(DATA_ID_ATTACK_TARGET));
            if ($$0 instanceof net.minecraft.world.entity.LivingEntity) {
               this.clientSideCachedAttackTarget = (net.minecraft.world.entity.LivingEntity)$$0;
               return this.clientSideCachedAttackTarget;
            } else {
               return null;
            }
         }
      } else {
         return this.getTarget();
      }
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      super.onSyncedDataUpdated($$0);
      if (DATA_ID_ATTACK_TARGET.equals($$0)) {
         this.clientSideAttackTime = 0;
         this.clientSideCachedAttackTarget = null;
      }
   }

   @Override
   public int getAmbientSoundInterval() {
      return 160;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.isInWater() ? SoundEvents.GUARDIAN_AMBIENT : SoundEvents.GUARDIAN_AMBIENT_LAND;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return this.isInWater() ? SoundEvents.GUARDIAN_HURT : SoundEvents.GUARDIAN_HURT_LAND;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return this.isInWater() ? SoundEvents.GUARDIAN_DEATH : SoundEvents.GUARDIAN_DEATH_LAND;
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.EVENTS;
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return $$1.getFluidState($$0).is(FluidTags.WATER) ? 10.0F + $$1.getPathfindingCostFromLightLevels($$0) : super.getWalkTargetValue($$0, $$1);
   }

   @Override
   public void aiStep() {
      if (this.isAlive()) {
         if (this.level().isClientSide()) {
            this.clientSideTailAnimationO = this.clientSideTailAnimation;
            if (!this.isInWater()) {
               this.clientSideTailAnimationSpeed = 2.0F;
               Vec3 $$0 = this.getDeltaMovement();
               if ($$0.y > 0.0 && this.clientSideTouchedGround && !this.isSilent()) {
                  this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundSource(), 1.0F, 1.0F, false);
               }

               this.clientSideTouchedGround = $$0.y < 0.0 && this.level().loadedAndEntityCanStandOn(this.blockPosition().below(), this);
            } else if (this.isMoving()) {
               if (this.clientSideTailAnimationSpeed < 0.5F) {
                  this.clientSideTailAnimationSpeed = 4.0F;
               } else {
                  this.clientSideTailAnimationSpeed = this.clientSideTailAnimationSpeed + (0.5F - this.clientSideTailAnimationSpeed) * 0.1F;
               }
            } else {
               this.clientSideTailAnimationSpeed = this.clientSideTailAnimationSpeed + (0.125F - this.clientSideTailAnimationSpeed) * 0.2F;
            }

            this.clientSideTailAnimation = this.clientSideTailAnimation + this.clientSideTailAnimationSpeed;
            this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
            if (!this.isInWater()) {
               this.clientSideSpikesAnimation = this.random.nextFloat();
            } else if (this.isMoving()) {
               this.clientSideSpikesAnimation = this.clientSideSpikesAnimation + (0.0F - this.clientSideSpikesAnimation) * 0.25F;
            } else {
               this.clientSideSpikesAnimation = this.clientSideSpikesAnimation + (1.0F - this.clientSideSpikesAnimation) * 0.06F;
            }

            if (this.isMoving() && this.isInWater()) {
               Vec3 $$1 = this.getViewVector(0.0F);

               for (int $$2 = 0; $$2 < 2; $$2++) {
                  this.level()
                     .addParticle(
                        ParticleTypes.BUBBLE,
                        this.getRandomX(0.5) - $$1.x * 1.5,
                        this.getRandomY() - $$1.y * 1.5,
                        this.getRandomZ(0.5) - $$1.z * 1.5,
                        0.0,
                        0.0,
                        0.0
                     );
               }
            }

            if (this.hasActiveAttackTarget()) {
               if (this.clientSideAttackTime < this.getAttackDuration()) {
                  this.clientSideAttackTime++;
               }

               net.minecraft.world.entity.LivingEntity $$3 = this.getActiveAttackTarget();
               if ($$3 != null) {
                  this.getLookControl().setLookAt($$3, 90.0F, 90.0F);
                  this.getLookControl().tick();
                  double $$4 = this.getAttackAnimationScale(0.0F);
                  double $$5 = $$3.getX() - this.getX();
                  double $$6 = $$3.getY(0.5) - this.getEyeY();
                  double $$7 = $$3.getZ() - this.getZ();
                  double $$8 = Math.sqrt($$5 * $$5 + $$6 * $$6 + $$7 * $$7);
                  $$5 /= $$8;
                  $$6 /= $$8;
                  $$7 /= $$8;
                  double $$9 = this.random.nextDouble();

                  while ($$9 < $$8) {
                     $$9 += 1.8 - $$4 + this.random.nextDouble() * (1.7 - $$4);
                     this.level()
                        .addParticle(ParticleTypes.BUBBLE, this.getX() + $$5 * $$9, this.getEyeY() + $$6 * $$9, this.getZ() + $$7 * $$9, 0.0, 0.0, 0.0);
                  }
               }
            }
         }

         if (this.isInWater()) {
            this.setAirSupply(300);
         } else if (this.onGround()) {
            this.setDeltaMovement(
               this.getDeltaMovement().add((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F, 0.5, (this.random.nextFloat() * 2.0F - 1.0F) * 0.4F)
            );
            this.setYRot(this.random.nextFloat() * 360.0F);
            this.setOnGround(false);
            this.needsSync = true;
         }

         if (this.hasActiveAttackTarget()) {
            this.setYRot(this.yHeadRot);
         }
      }

      super.aiStep();
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.GUARDIAN_FLOP;
   }

   public float getTailAnimation(float $$0) {
      return Mth.lerp($$0, this.clientSideTailAnimationO, this.clientSideTailAnimation);
   }

   public float getSpikesAnimation(float $$0) {
      return Mth.lerp($$0, this.clientSideSpikesAnimationO, this.clientSideSpikesAnimation);
   }

   public float getAttackAnimationScale(float $$0) {
      return (this.clientSideAttackTime + $$0) / this.getAttackDuration();
   }

   public float getClientSideAttackTime() {
      return this.clientSideAttackTime;
   }

   @Override
   public boolean checkSpawnObstruction(LevelReader $$0) {
      return $$0.isUnobstructed(this);
   }

   public static boolean checkGuardianSpawnRules(
      net.minecraft.world.entity.EntityType<? extends Guardian> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return ($$4.nextInt(20) == 0 || !$$1.canSeeSkyFromBelowWater($$3))
         && $$1.getDifficulty() != Difficulty.PEACEFUL
         && (net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2) || $$1.getFluidState($$3).is(FluidTags.WATER))
         && $$1.getFluidState($$3.below()).is(FluidTags.WATER);
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (!this.isMoving()
         && !$$1.is(DamageTypeTags.AVOIDS_GUARDIAN_THORNS)
         && !$$1.is(DamageTypes.THORNS)
         && $$1.getDirectEntity() instanceof net.minecraft.world.entity.LivingEntity $$3) {
         $$3.hurtServer($$0, this.damageSources().thorns(this), 2.0F);
      }

      if (this.randomStrollGoal != null) {
         this.randomStrollGoal.trigger();
      }

      return super.hurtServer($$0, $$1, $$2);
   }

   @Override
   public int getMaxHeadXRot() {
      return 180;
   }

   @Override
   protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
      this.moveRelative(0.1F, $$0);
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      if (!this.isMoving() && this.getTarget() == null) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
      }
   }

   static class GuardianAttackGoal extends Goal {
      private final Guardian guardian;
      private int attackTime;
      private final boolean elder;

      public GuardianAttackGoal(Guardian $$0) {
         this.guardian = $$0;
         this.elder = $$0 instanceof ElderGuardian;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         net.minecraft.world.entity.LivingEntity $$0 = this.guardian.getTarget();
         return $$0 != null && $$0.isAlive();
      }

      @Override
      public boolean canContinueToUse() {
         return super.canContinueToUse() && (this.elder || this.guardian.getTarget() != null && this.guardian.distanceToSqr(this.guardian.getTarget()) > 9.0);
      }

      @Override
      public void start() {
         this.attackTime = -10;
         this.guardian.getNavigation().stop();
         net.minecraft.world.entity.LivingEntity $$0 = this.guardian.getTarget();
         if ($$0 != null) {
            this.guardian.getLookControl().setLookAt($$0, 90.0F, 90.0F);
         }

         this.guardian.needsSync = true;
      }

      @Override
      public void stop() {
         this.guardian.setActiveAttackTarget(0);
         this.guardian.setTarget(null);
         this.guardian.randomStrollGoal.trigger();
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         net.minecraft.world.entity.LivingEntity $$0 = this.guardian.getTarget();
         if ($$0 != null) {
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt($$0, 90.0F, 90.0F);
            if (!this.guardian.hasLineOfSight($$0)) {
               this.guardian.setTarget(null);
            } else {
               this.attackTime++;
               if (this.attackTime == 0) {
                  this.guardian.setActiveAttackTarget($$0.getId());
                  if (!this.guardian.isSilent()) {
                     this.guardian.level().broadcastEntityEvent(this.guardian, (byte)21);
                  }
               } else if (this.attackTime >= this.guardian.getAttackDuration()) {
                  float $$1 = 1.0F;
                  if (this.guardian.level().getDifficulty() == Difficulty.HARD) {
                     $$1 += 2.0F;
                  }

                  if (this.elder) {
                     $$1 += 2.0F;
                  }

                  ServerLevel $$2 = getServerLevel(this.guardian);
                  $$0.hurtServer($$2, this.guardian.damageSources().indirectMagic(this.guardian, this.guardian), $$1);
                  this.guardian.doHurtTarget($$2, $$0);
                  this.guardian.setTarget(null);
               }

               super.tick();
            }
         }
      }
   }

   static class GuardianAttackSelector implements TargetingConditions.Selector {
      private final Guardian guardian;

      public GuardianAttackSelector(Guardian $$0) {
         this.guardian = $$0;
      }

      @Override
      public boolean test(net.minecraft.world.entity.LivingEntity $$0, ServerLevel $$1) {
         return ($$0 instanceof Player || $$0 instanceof Squid || $$0 instanceof Axolotl) && $$0.distanceToSqr(this.guardian) > 9.0;
      }
   }

   static class GuardianMoveControl extends MoveControl {
      private final Guardian guardian;

      public GuardianMoveControl(Guardian $$0) {
         super($$0);
         this.guardian = $$0;
      }

      @Override
      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO && !this.guardian.getNavigation().isDone()) {
            Vec3 $$0 = new Vec3(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
            double $$1 = $$0.length();
            double $$2 = $$0.x / $$1;
            double $$3 = $$0.y / $$1;
            double $$4 = $$0.z / $$1;
            float $$5 = (float)(Mth.atan2($$0.z, $$0.x) * 180.0F / (float)Math.PI) - 90.0F;
            this.guardian.setYRot(this.rotlerp(this.guardian.getYRot(), $$5, 90.0F));
            this.guardian.yBodyRot = this.guardian.getYRot();
            float $$6 = (float)(this.speedModifier * this.guardian.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float $$7 = Mth.lerp(0.125F, this.guardian.getSpeed(), $$6);
            this.guardian.setSpeed($$7);
            double $$8 = Math.sin((this.guardian.tickCount + this.guardian.getId()) * 0.5) * 0.05;
            double $$9 = Math.cos(this.guardian.getYRot() * (float) (Math.PI / 180.0));
            double $$10 = Math.sin(this.guardian.getYRot() * (float) (Math.PI / 180.0));
            double $$11 = Math.sin((this.guardian.tickCount + this.guardian.getId()) * 0.75) * 0.05;
            this.guardian.setDeltaMovement(this.guardian.getDeltaMovement().add($$8 * $$9, $$11 * ($$10 + $$9) * 0.25 + $$7 * $$3 * 0.1, $$8 * $$10));
            LookControl $$12 = this.guardian.getLookControl();
            double $$13 = this.guardian.getX() + $$2 * 2.0;
            double $$14 = this.guardian.getEyeY() + $$3 / $$1;
            double $$15 = this.guardian.getZ() + $$4 * 2.0;
            double $$16 = $$12.getWantedX();
            double $$17 = $$12.getWantedY();
            double $$18 = $$12.getWantedZ();
            if (!$$12.isLookingAtTarget()) {
               $$16 = $$13;
               $$17 = $$14;
               $$18 = $$15;
            }

            this.guardian.getLookControl().setLookAt(Mth.lerp(0.125, $$16, $$13), Mth.lerp(0.125, $$17, $$14), Mth.lerp(0.125, $$18, $$15), 10.0F, 40.0F);
            this.guardian.setMoving(true);
         } else {
            this.guardian.setSpeed(0.0F);
            this.guardian.setMoving(false);
         }
      }
   }
}
