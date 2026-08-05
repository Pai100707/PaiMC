/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.camel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.schedule.Activity;

public class CamelAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 4.0f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 2.0f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 2.5f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 2.5f;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0f;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super Camel>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS, SensorType.NEAREST_ADULT);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, (Object[])new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT});

    protected static void initMemories(Camel $$0, RandomSource $$1) {
    }

    public static Brain.Provider<Camel> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Camel> $$0) {
        CamelAi.initCoreActivity($$0);
        CamelAi.initIdleActivity($$0);
        $$0.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$0.setDefaultActivity(Activity.IDLE);
        $$0.useDefaultActivity();
        return $$0;
    }

    private static void initCoreActivity(Brain<Camel> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Camel>>)ImmutableList.of(new Swim(0.8f), (Object)new CamelPanic(4.0f), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Camel> $$02) {
        $$02.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Camel>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)1, (Object)new AnimalMakeLove(EntityType.CAMEL)), (Object)Pair.of((Object)2, new RunOne(ImmutableList.of((Object)Pair.of((Object)new FollowTemptation($$0 -> Float.valueOf(2.5f), $$0 -> $$0.isBaby() ? 2.5 : 3.5), (Object)1), (Object)Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 2.5f)), (Object)1)))), (Object)Pair.of((Object)3, (Object)new RandomLookAround(UniformInt.of(150, 250), 30.0f, 0.0f, 0.0f)), (Object)Pair.of((Object)4, new RunOne((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), RandomStroll.stroll(2.0f)), (Object)1), (Object)Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), SetWalkTargetFromLookTarget.create(2.0f, 3)), (Object)1), (Object)Pair.of((Object)new RandomSitting(20), (Object)1), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1))))));
    }

    public static void updateActivity(Camel $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.IDLE));
    }

    public static class CamelPanic
    extends AnimalPanic<Camel> {
        public CamelPanic(float $$0) {
            super($$0);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel $$0, Camel $$1) {
            return super.checkExtraStartConditions($$0, $$1) && !$$1.isMobControlled();
        }

        @Override
        protected void start(ServerLevel $$0, Camel $$1, long $$2) {
            $$1.standUpInstantly();
            super.start($$0, $$1, $$2);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
            this.start(serverLevel, (Camel)pathfinderMob, l);
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Camel)livingEntity, l);
        }
    }

    public static class RandomSitting
    extends Behavior<Camel> {
        private final int minimalPoseTicks;

        public RandomSitting(int $$0) {
            super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of());
            this.minimalPoseTicks = $$0 * 20;
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel $$0, Camel $$1) {
            return !$$1.isInWater() && $$1.getPoseTime() >= (long)this.minimalPoseTicks && !$$1.isLeashed() && $$1.onGround() && !$$1.hasControllingPassenger() && $$1.canCamelChangePose();
        }

        @Override
        protected void start(ServerLevel $$0, Camel $$1, long $$2) {
            if ($$1.isCamelSitting()) {
                $$1.standUp();
            } else if (!$$1.isPanicking()) {
                $$1.sitDown();
            }
        }

        @Override
        protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
            this.start(serverLevel, (Camel)livingEntity, l);
        }
    }
}

