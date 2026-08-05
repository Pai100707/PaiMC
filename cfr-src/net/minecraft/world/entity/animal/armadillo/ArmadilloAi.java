/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.armadillo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomLookAround;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.schedule.Activity;

public class ArmadilloAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0f;
    private static final double DEFAULT_CLOSE_ENOUGH_DIST = 2.0;
    private static final double BABY_CLOSE_ENOUGH_DIST = 1.0;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super Armadillo>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS, SensorType.NEAREST_ADULT, SensorType.ARMADILLO_SCARE_DETECTED);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, (Object[])new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.DANGER_DETECTED_RECENTLY});
    private static final OneShot<Armadillo> ARMADILLO_ROLLING_OUT = BehaviorBuilder.create($$0 -> $$0.group($$0.absent(MemoryModuleType.DANGER_DETECTED_RECENTLY)).apply((Applicative)$$0, $$02 -> ($$0, $$1, $$2) -> {
        if ($$1.isScared()) {
            $$1.rollOut();
            return true;
        }
        return false;
    }));

    public static Brain.Provider<Armadillo> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Armadillo> $$0) {
        ArmadilloAi.initCoreActivity($$0);
        ArmadilloAi.initIdleActivity($$0);
        ArmadilloAi.initScaredActivity($$0);
        $$0.setCoreActivities(Set.of(Activity.CORE));
        $$0.setDefaultActivity(Activity.IDLE);
        $$0.useDefaultActivity();
        return $$0;
    }

    private static void initCoreActivity(Brain<Armadillo> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Armadillo>>)ImmutableList.of(new Swim(0.8f), (Object)new ArmadilloPanic(2.0f), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(){

            @Override
            protected boolean checkExtraStartConditions(ServerLevel $$0, Mob $$1) {
                Armadillo $$2;
                if ($$1 instanceof Armadillo && ($$2 = (Armadillo)$$1).isScared()) {
                    return false;
                }
                return super.checkExtraStartConditions($$0, $$1);
            }
        }, (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS), ARMADILLO_ROLLING_OUT));
    }

    private static void initIdleActivity(Brain<Armadillo> $$02) {
        $$02.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Armadillo>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)1, (Object)new AnimalMakeLove(EntityType.ARMADILLO, 1.0f, 1)), (Object)Pair.of((Object)2, new RunOne(ImmutableList.of((Object)Pair.of((Object)new FollowTemptation($$0 -> Float.valueOf(1.25f), $$0 -> $$0.isBaby() ? 1.0 : 2.0), (Object)1), (Object)Pair.of(BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25f), (Object)1)))), (Object)Pair.of((Object)3, (Object)new RandomLookAround(UniformInt.of(150, 250), 30.0f, 0.0f, 0.0f)), (Object)Pair.of((Object)4, new RunOne((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(RandomStroll.stroll(1.0f), (Object)1), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)1), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1))))));
    }

    private static void initScaredActivity(Brain<Armadillo> $$0) {
        $$0.addActivityWithConditions(Activity.PANIC, (ImmutableList<Pair<Integer, BehaviorControl<Armadillo>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new ArmadilloBallUp())), Set.of(Pair.of(MemoryModuleType.DANGER_DETECTED_RECENTLY, (Object)((Object)MemoryStatus.VALUE_PRESENT)), Pair.of(MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    public static void updateActivity(Armadillo $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.PANIC, (Object)Activity.IDLE));
    }

    public static class ArmadilloPanic
    extends AnimalPanic<Armadillo> {
        public ArmadilloPanic(float $$02) {
            super($$02, $$0 -> DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES);
        }

        @Override
        protected void start(ServerLevel $$0, Armadillo $$1, long $$2) {
            $$1.rollOut();
            super.start($$0, $$1, $$2);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
            this.start(serverLevel, (Armadillo)pathfinderMob, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Armadillo)livingEntity, l);
        }
    }

    public static class ArmadilloBallUp
    extends Behavior<Armadillo> {
        static final int BALL_UP_STAY_IN_STATE = 5 * TimeUtil.SECONDS_PER_MINUTE * 20;
        static final int TICKS_DELAY_TO_DETERMINE_IF_DANGER_IS_STILL_AROUND = 5;
        static final int DANGER_DETECTED_RECENTLY_DANGER_THRESHOLD = 75;
        int nextPeekTimer = 0;
        boolean dangerWasAround;

        public ArmadilloBallUp() {
            super(Map.of(), BALL_UP_STAY_IN_STATE);
        }

        @Override
        protected void tick(ServerLevel $$0, Armadillo $$1, long $$2) {
            boolean $$5;
            super.tick($$0, $$1, $$2);
            if (this.nextPeekTimer > 0) {
                --this.nextPeekTimer;
            }
            if ($$1.shouldSwitchToScaredState()) {
                $$1.switchToState(Armadillo.ArmadilloState.SCARED);
                if ($$1.onGround()) {
                    $$1.playSound(SoundEvents.ARMADILLO_LAND);
                }
                return;
            }
            Armadillo.ArmadilloState $$3 = $$1.getState();
            long $$4 = $$1.getBrain().getTimeUntilExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY);
            boolean bl = $$5 = $$4 > 75L;
            if ($$5 != this.dangerWasAround) {
                this.nextPeekTimer = this.pickNextPeekTimer($$1);
            }
            this.dangerWasAround = $$5;
            if ($$3 == Armadillo.ArmadilloState.SCARED) {
                if (this.nextPeekTimer == 0 && $$1.onGround() && $$5) {
                    $$0.broadcastEntityEvent($$1, (byte)64);
                    this.nextPeekTimer = this.pickNextPeekTimer($$1);
                }
                if ($$4 < (long)Armadillo.ArmadilloState.UNROLLING.animationDuration()) {
                    $$1.playSound(SoundEvents.ARMADILLO_UNROLL_START);
                    $$1.switchToState(Armadillo.ArmadilloState.UNROLLING);
                }
            } else if ($$3 == Armadillo.ArmadilloState.UNROLLING && $$4 > (long)Armadillo.ArmadilloState.UNROLLING.animationDuration()) {
                $$1.switchToState(Armadillo.ArmadilloState.SCARED);
            }
        }

        private int pickNextPeekTimer(Armadillo $$0) {
            return Armadillo.ArmadilloState.SCARED.animationDuration() + $$0.getRandom().nextIntBetweenInclusive(100, 400);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel $$0, Armadillo $$1) {
            return $$1.onGround();
        }

        @Override
        protected boolean canStillUse(ServerLevel $$0, Armadillo $$1, long $$2) {
            return $$1.getState().isThreatened();
        }

        @Override
        protected void start(ServerLevel $$0, Armadillo $$1, long $$2) {
            $$1.rollUp();
        }

        @Override
        protected void stop(ServerLevel $$0, Armadillo $$1, long $$2) {
            if (!$$1.canStayRolledUp()) {
                $$1.rollOut();
            }
        }

        @Override
        protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            return this.canStillUse(serverLevel, (Armadillo)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.stop(serverLevel, (Armadillo)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.tick(serverLevel, (Armadillo)livingEntity, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Armadillo)livingEntity, l);
        }
    }
}

