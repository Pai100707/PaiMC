/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.happyghast;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.schedule.Activity;

public class HappyGhastAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.1f;
    private static final double BABY_GHAST_CLOSE_ENOUGH_DIST = 3.0;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(3, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super HappyGhast>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS, SensorType.NEAREST_ADULT_ANY_TYPE, SensorType.NEAREST_PLAYERS);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_ADULT, (Object[])new MemoryModuleType[]{MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS});

    public static Brain.Provider<HappyGhast> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<HappyGhast> $$0) {
        HappyGhastAi.initCoreActivity($$0);
        HappyGhastAi.initIdleActivity($$0);
        HappyGhastAi.initPanicActivity($$0);
        $$0.setCoreActivities(Set.of(Activity.CORE));
        $$0.setDefaultActivity(Activity.IDLE);
        $$0.useDefaultActivity();
        return $$0;
    }

    private static void initCoreActivity(Brain<HappyGhast> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<HappyGhast>>)ImmutableList.of(new Swim(0.8f), new AnimalPanic(2.0f, 0), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<HappyGhast> $$02) {
        $$02.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<HappyGhast>>>)ImmutableList.of((Object)Pair.of((Object)1, (Object)new FollowTemptation($$0 -> Float.valueOf(1.25f), $$0 -> 3.0, true)), (Object)Pair.of((Object)2, BabyFollowAdult.create(ADULT_FOLLOW_RANGE, $$0 -> Float.valueOf(1.1f), MemoryModuleType.NEAREST_VISIBLE_PLAYER, true)), (Object)Pair.of((Object)3, BabyFollowAdult.create(ADULT_FOLLOW_RANGE, $$0 -> Float.valueOf(1.1f), MemoryModuleType.NEAREST_VISIBLE_ADULT, true)), (Object)Pair.of((Object)4, new RunOne(ImmutableList.of((Object)Pair.of(RandomStroll.fly(1.0f), (Object)1), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)1))))));
    }

    private static void initPanicActivity(Brain<HappyGhast> $$0) {
        $$0.addActivityWithConditions(Activity.PANIC, (ImmutableList<Pair<Integer, BehaviorControl<HappyGhast>>>)ImmutableList.of(), Set.of(Pair.of(MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryStatus.VALUE_PRESENT))));
    }

    public static void updateActivity(HappyGhast $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.PANIC, (Object)Activity.IDLE));
    }
}

