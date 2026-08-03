package net.minecraft.world.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Zoglin extends Monster implements HoglinBase {
   private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zoglin.class, EntityDataSerializers.BOOLEAN);
   private static final int MAX_HEALTH = 40;
   private static final int ATTACK_KNOCKBACK = 1;
   private static final float KNOCKBACK_RESISTANCE = 0.6F;
   private static final int ATTACK_DAMAGE = 6;
   private static final float BABY_ATTACK_DAMAGE = 0.5F;
   private static final int ATTACK_INTERVAL = 40;
   private static final int BABY_ATTACK_INTERVAL = 15;
   private static final int ATTACK_DURATION = 200;
   private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
   private static final boolean DEFAULT_BABY = false;
   private int attackAnimationRemainingTicks;
   protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Zoglin>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS
   );
   protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
      MemoryModuleType.NEAREST_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_PLAYER,
      MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.ATTACK_TARGET,
      MemoryModuleType.ATTACK_COOLING_DOWN
   );

   public Zoglin(net.minecraft.world.entity.EntityType<? extends Zoglin> $$0, Level $$1) {
      super($$0, $$1);
      this.xpReward = 5;
   }

   @Override
   protected Brain.Provider<Zoglin> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   @Override
   protected Brain<?> makeBrain(Dynamic<?> $$0) {
      Brain<Zoglin> $$1 = this.brainProvider().makeBrain($$0);
      initCoreActivity($$1);
      initIdleActivity($$1);
      initFightActivity($$1);
      $$1.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$1.setDefaultActivity(Activity.IDLE);
      $$1.useDefaultActivity();
      return $$1;
   }

   private static void initCoreActivity(Brain<Zoglin> $$0) {
      $$0.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
   }

   private static void initIdleActivity(Brain<Zoglin> $$0) {
      $$0.addActivity(
         Activity.IDLE,
         10,
         ImmutableList.of(
            StartAttacking.create(($$0x, $$1) -> $$1.findNearestValidAttackTarget($$0x)),
            SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
            new RunOne(
               ImmutableList.of(
                  Pair.of(RandomStroll.stroll(0.4F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1)
               )
            )
         )
      );
   }

   private static void initFightActivity(Brain<Zoglin> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.FIGHT,
         10,
         ImmutableList.of(
            SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
            BehaviorBuilder.triggerIf(Zoglin::isAdult, MeleeAttack.create(40)),
            BehaviorBuilder.triggerIf(Zoglin::isBaby, MeleeAttack.create(15)),
            StopAttackingIfTargetInvalid.create()
         ),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private Optional<? extends net.minecraft.world.entity.LivingEntity> findNearestValidAttackTarget(ServerLevel $$0) {
      return this.getBrain()
         .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
         .orElse(NearestVisibleLivingEntities.empty())
         .findClosest($$1 -> this.isTargetable($$0, $$1));
   }

   private boolean isTargetable(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      net.minecraft.world.entity.EntityType<?> $$2 = $$1.getType();
      return $$2 != net.minecraft.world.entity.EntityType.ZOGLIN
         && $$2 != net.minecraft.world.entity.EntityType.CREEPER
         && Sensor.isEntityAttackable($$0, this, $$1);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_BABY_ID, false);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      super.onSyncedDataUpdated($$0);
      if (DATA_BABY_ID.equals($$0)) {
         this.refreshDimensions();
      }
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if ($$0.getRandom().nextFloat() < 0.2F) {
         this.setBaby(true);
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3F)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.6F)
         .add(Attributes.ATTACK_KNOCKBACK, 1.0)
         .add(Attributes.ATTACK_DAMAGE, 6.0);
   }

   public boolean isAdult() {
      return !this.isBaby();
   }

   @Override
   public boolean doHurtTarget(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      if ($$1 instanceof net.minecraft.world.entity.LivingEntity $$2) {
         this.attackAnimationRemainingTicks = 10;
         $$0.broadcastEntityEvent(this, (byte)4);
         this.makeSound(SoundEvents.ZOGLIN_ATTACK);
         return HoglinBase.hurtAndThrowTarget($$0, this, $$2);
      } else {
         return false;
      }
   }

   @Override
   public boolean canBeLeashed() {
      return true;
   }

   @Override
   protected void blockedByItem(net.minecraft.world.entity.LivingEntity $$0) {
      if (!this.isBaby()) {
         HoglinBase.throwTarget(this, $$0);
      }
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      boolean $$3 = super.hurtServer($$0, $$1, $$2);
      if ($$3 && $$1.getEntity() instanceof net.minecraft.world.entity.LivingEntity $$4) {
         if (this.canAttack($$4) && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(this, $$4, 4.0)) {
            this.setAttackTarget($$4);
         }

         return true;
      } else {
         return $$3;
      }
   }

   private void setAttackTarget(net.minecraft.world.entity.LivingEntity $$0) {
      this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      this.brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, $$0, 200L);
   }

   @Override
   public Brain<Zoglin> getBrain() {
      return (Brain<Zoglin>)super.getBrain();
   }

   protected void updateActivity() {
      Activity $$0 = this.brain.getActiveNonCoreActivity().orElse(null);
      this.brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity $$1 = this.brain.getActiveNonCoreActivity().orElse(null);
      if ($$1 == Activity.FIGHT && $$0 != Activity.FIGHT) {
         this.playAngrySound();
      }

      this.setAggressive(this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      ProfilerFiller $$1 = Profiler.get();
      $$1.push("zoglinBrain");
      this.getBrain().tick($$0, this);
      $$1.pop();
      this.updateActivity();
   }

   @Override
   public void setBaby(boolean $$0) {
      this.getEntityData().set(DATA_BABY_ID, $$0);
      if (!this.level().isClientSide() && $$0) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5);
      }
   }

   @Override
   public boolean isBaby() {
      return (Boolean)this.getEntityData().get(DATA_BABY_ID);
   }

   @Override
   public void aiStep() {
      if (this.attackAnimationRemainingTicks > 0) {
         this.attackAnimationRemainingTicks--;
      }

      super.aiStep();
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 4) {
         this.attackAnimationRemainingTicks = 10;
         this.makeSound(SoundEvents.ZOGLIN_ATTACK);
      } else {
         super.handleEntityEvent($$0);
      }
   }

   @Override
   public int getAttackAnimationRemainingTicks() {
      return this.attackAnimationRemainingTicks;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      if (this.level().isClientSide()) {
         return null;
      } else {
         return this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? SoundEvents.ZOGLIN_ANGRY : SoundEvents.ZOGLIN_AMBIENT;
      }
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.ZOGLIN_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOGLIN_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.ZOGLIN_STEP, 0.15F, 1.0F);
   }

   protected void playAngrySound() {
      this.makeSound(SoundEvents.ZOGLIN_ANGRY);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.LivingEntity getTarget() {
      return this.getTargetFromBrain();
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("IsBaby", this.isBaby());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setBaby($$0.getBooleanOr("IsBaby", false));
   }
}
