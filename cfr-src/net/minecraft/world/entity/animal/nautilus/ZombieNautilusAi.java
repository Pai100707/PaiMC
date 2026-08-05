/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.nautilus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.ChargeAttack;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.nautilus.NautilusAi;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.schedule.Activity;

public class ZombieNautilusAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 0.9f;
    private static final float SPEED_WHEN_ATTACKING = 0.5f;
    private static final float ATTACK_KNOCKBACK_FORCE = 2.0f;
    private static final int TIME_BETWEEN_ATTACKS = 80;
    private static final double MAX_CHARGE_DISTANCE = 12.0;
    private static final double MAX_TARGET_DETECTION_DISTANCE = 11.0;
    protected static final ImmutableList<SensorType<? extends Sensor<? super ZombieNautilus>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NAUTILUS_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING, MemoryModuleType.ATTACK_TARGET, (Object[])new MemoryModuleType[]{MemoryModuleType.CHARGE_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.ANGRY_AT, MemoryModuleType.ATTACK_TARGET_COOLDOWN});

    protected static Brain.Provider<ZombieNautilus> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<ZombieNautilus> $$0) {
        ZombieNautilusAi.initCoreActivity($$0);
        ZombieNautilusAi.initIdleActivity($$0);
        ZombieNautilusAi.initFightActivity($$0);
        $$0.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$0.setDefaultActivity(Activity.IDLE);
        $$0.useDefaultActivity();
        return $$0;
    }

    private static void initCoreActivity(Brain<ZombieNautilus> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<ZombieNautilus>>)ImmutableList.of((Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.CHARGE_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.ATTACK_TARGET_COOLDOWN)));
    }

    private static void initIdleActivity(Brain<ZombieNautilus> $$02) {
        $$02.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<ZombieNautilus>>>)ImmutableList.of((Object)Pair.of((Object)1, (Object)new FollowTemptation($$0 -> Float.valueOf(0.9f), $$0 -> $$0.isBaby() ? 2.5 : 3.5)), (Object)Pair.of((Object)2, StartAttacking.create(NautilusAi::findNearestValidAttackTarget)), (Object)Pair.of((Object)3, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Set<MemoryModuleType<?>>)ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of((Object)Pair.of(RandomStroll.swim(1.0f), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)3))))));
    }

    private static void initFightActivity(Brain<ZombieNautilus> $$0) {
        $$0.addActivityWithConditions(Activity.FIGHT, (ImmutableList<Pair<Integer, BehaviorControl<ZombieNautilus>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new ChargeAttack(80, NautilusAi.ATTACK_TARGET_CONDITIONS, 0.5f, 2.0f, 12.0, 11.0, SoundEvents.ZOMBIE_NAUTILUS_DASH))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), (Object)Pair.of(MemoryModuleType.TEMPTING_PLAYER, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.CHARGE_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    public static void updateActivity(ZombieNautilus $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.FIGHT, (Object)Activity.IDLE));
    }
}

