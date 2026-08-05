/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Zoglin
extends Monster
implements HoglinBase {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zoglin.class, EntityDataSerializers.BOOLEAN);
    private static final int MAX_HEALTH = 40;
    private static final int ATTACK_KNOCKBACK = 1;
    private static final float KNOCKBACK_RESISTANCE = 0.6f;
    private static final int ATTACK_DAMAGE = 6;
    private static final float BABY_ATTACK_DAMAGE = 0.5f;
    private static final int ATTACK_INTERVAL = 40;
    private static final int BABY_ATTACK_INTERVAL = 15;
    private static final int ATTACK_DURATION = 200;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4f;
    private static final boolean DEFAULT_BABY = false;
    private int attackAnimationRemainingTicks;
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Zoglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

    public Zoglin(EntityType<? extends Zoglin> $$0, Level $$1) {
        super((EntityType<? extends Monster>)$$0, $$1);
        this.xpReward = 5;
    }

    protected Brain.Provider<Zoglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        Brain<Zoglin> $$1 = this.brainProvider().makeBrain($$0);
        Zoglin.initCoreActivity($$1);
        Zoglin.initIdleActivity($$1);
        Zoglin.initFightActivity($$1);
        $$1.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$1.setDefaultActivity(Activity.IDLE);
        $$1.useDefaultActivity();
        return $$1;
    }

    private static void initCoreActivity(Brain<Zoglin> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Zoglin>>)ImmutableList.of((Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink()));
    }

    private static void initIdleActivity(Brain<Zoglin> $$02) {
        $$02.addActivity(Activity.IDLE, 10, (ImmutableList<BehaviorControl<Zoglin>>)ImmutableList.of(StartAttacking.create(($$0, $$1) -> $$1.findNearestValidAttackTarget($$0)), SetEntityLookTargetSometimes.create(8.0f, UniformInt.of(30, 60)), new RunOne(ImmutableList.of((Object)Pair.of(RandomStroll.stroll(0.4f), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(0.4f, 3), (Object)2), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1)))));
    }

    private static void initFightActivity(Brain<Zoglin> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, (ImmutableList<BehaviorControl<Zoglin>>)ImmutableList.of(SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0f), BehaviorBuilder.triggerIf(Zoglin::isAdult, MeleeAttack.create(40)), BehaviorBuilder.triggerIf(Zoglin::isBaby, MeleeAttack.create(15)), StopAttackingIfTargetInvalid.create()), MemoryModuleType.ATTACK_TARGET);
    }

    private Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel $$0) {
        return this.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty()).findClosest($$1 -> this.isTargetable($$0, (LivingEntity)$$1));
    }

    private boolean isTargetable(ServerLevel $$0, LivingEntity $$1) {
        EntityType<?> $$2 = $$1.getType();
        return $$2 != EntityType.ZOGLIN && $$2 != EntityType.CREEPER && Sensor.isEntityAttackable($$0, this, $$1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
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

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        if ($$0.getRandom().nextFloat() < 0.2f) {
            this.setBaby(true);
        }
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.KNOCKBACK_RESISTANCE, 0.6f).add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        void $$3;
        if (!($$1 instanceof LivingEntity)) {
            return false;
        }
        LivingEntity $$2 = (LivingEntity)$$1;
        this.attackAnimationRemainingTicks = 10;
        $$0.broadcastEntityEvent(this, (byte)4);
        this.makeSound(SoundEvents.ZOGLIN_ATTACK);
        return HoglinBase.hurtAndThrowTarget($$0, this, (LivingEntity)$$3);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    protected void blockedByItem(LivingEntity $$0) {
        if (!this.isBaby()) {
            HoglinBase.throwTarget(this, $$0);
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        void $$5;
        Entity entity;
        boolean $$3 = super.hurtServer($$0, $$1, $$2);
        if (!$$3 || !((entity = $$1.getEntity()) instanceof LivingEntity)) {
            return $$3;
        }
        LivingEntity $$4 = (LivingEntity)entity;
        if (this.canAttack((LivingEntity)$$5) && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(this, (LivingEntity)$$5, 4.0)) {
            this.setAttackTarget((LivingEntity)$$5);
        }
        return true;
    }

    private void setAttackTarget(LivingEntity $$0) {
        this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        this.brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, $$0, 200L);
    }

    public Brain<Zoglin> getBrain() {
        return super.getBrain();
    }

    protected void updateActivity() {
        Activity $$0 = this.brain.getActiveNonCoreActivity().orElse(null);
        this.brain.setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.FIGHT, (Object)Activity.IDLE));
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
        return this.getEntityData().get(DATA_BABY_ID);
    }

    @Override
    public void aiStep() {
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
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
        }
        if (this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return SoundEvents.ZOGLIN_ANGRY;
        }
        return SoundEvents.ZOGLIN_AMBIENT;
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
        this.playSound(SoundEvents.ZOGLIN_STEP, 0.15f, 1.0f);
    }

    protected void playAngrySound() {
        this.makeSound(SoundEvents.ZOGLIN_ANGRY);
    }

    @Override
    public @Nullable LivingEntity getTarget() {
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

