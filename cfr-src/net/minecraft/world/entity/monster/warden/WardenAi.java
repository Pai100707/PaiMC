/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.warden.Digging;
import net.minecraft.world.entity.ai.behavior.warden.Emerging;
import net.minecraft.world.entity.ai.behavior.warden.ForceUnmount;
import net.minecraft.world.entity.ai.behavior.warden.Roar;
import net.minecraft.world.entity.ai.behavior.warden.SetRoarTarget;
import net.minecraft.world.entity.ai.behavior.warden.SetWardenLookTarget;
import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.behavior.warden.TryToSniff;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.schedule.Activity;

public class WardenAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5f;
    private static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7f;
    private static final float SPEED_MULTIPLIER_WHEN_FIGHTING = 1.2f;
    private static final int MELEE_ATTACK_COOLDOWN = 18;
    private static final int DIGGING_DURATION = Mth.ceil(100.0f);
    public static final int EMERGE_DURATION = Mth.ceil(133.59999f);
    public static final int ROAR_DURATION = Mth.ceil(84.0f);
    private static final int SNIFFING_DURATION = Mth.ceil(83.2f);
    public static final int DIGGING_COOLDOWN = 1200;
    private static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;
    private static final List<SensorType<? extends Sensor<? super Warden>>> SENSOR_TYPES = List.of(SensorType.NEAREST_PLAYERS, SensorType.WARDEN_ENTITY_SENSOR);
    private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.ROAR_TARGET, MemoryModuleType.DISTURBANCE_LOCATION, MemoryModuleType.RECENT_PROJECTILE, MemoryModuleType.IS_SNIFFING, MemoryModuleType.IS_EMERGING, MemoryModuleType.ROAR_SOUND_DELAY, MemoryModuleType.DIG_COOLDOWN, MemoryModuleType.ROAR_SOUND_COOLDOWN, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleType.TOUCH_COOLDOWN, MemoryModuleType.VIBRATION_COOLDOWN, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_DELAY);
    private static final BehaviorControl<Warden> DIG_COOLDOWN_SETTER = BehaviorBuilder.create($$0 -> $$0.group($$0.registered(MemoryModuleType.DIG_COOLDOWN)).apply((Applicative)$$0, $$1 -> ($$2, $$3, $$4) -> {
        if ($$0.tryGet($$1).isPresent()) {
            $$1.setWithExpiry(Unit.INSTANCE, 1200L);
        }
        return true;
    }));

    public static void updateActivity(Warden $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.EMERGE, (Object)Activity.DIG, (Object)Activity.ROAR, (Object)Activity.FIGHT, (Object)Activity.INVESTIGATE, (Object)Activity.SNIFF, (Object)Activity.IDLE));
    }

    protected static Brain<?> makeBrain(Warden $$0, Dynamic<?> $$1) {
        Brain.Provider $$2 = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
        Brain<Warden> $$3 = $$2.makeBrain($$1);
        WardenAi.initCoreActivity($$3);
        WardenAi.initEmergeActivity($$3);
        WardenAi.initDiggingActivity($$3);
        WardenAi.initIdleActivity($$3);
        WardenAi.initRoarActivity($$3);
        WardenAi.initFightActivity($$0, $$3);
        WardenAi.initInvestigateActivity($$3);
        WardenAi.initSniffingActivity($$3);
        $$3.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$3.setDefaultActivity(Activity.IDLE);
        $$3.useDefaultActivity();
        return $$3;
    }

    private static void initCoreActivity(Brain<Warden> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Warden>>)ImmutableList.of(new Swim(0.8f), SetWardenLookTarget.create(), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink()));
    }

    private static void initEmergeActivity(Brain<Warden> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.EMERGE, 5, (ImmutableList<BehaviorControl<Warden>>)ImmutableList.of(new Emerging(EMERGE_DURATION)), MemoryModuleType.IS_EMERGING);
    }

    private static void initDiggingActivity(Brain<Warden> $$0) {
        $$0.addActivityWithConditions(Activity.DIG, (ImmutableList<Pair<Integer, BehaviorControl<Warden>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new ForceUnmount()), (Object)Pair.of((Object)1, new Digging(DIGGING_DURATION))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.ROAR_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.DIG_COOLDOWN, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    private static void initIdleActivity(Brain<Warden> $$0) {
        $$0.addActivity(Activity.IDLE, 10, (ImmutableList<BehaviorControl<Warden>>)ImmutableList.of(SetRoarTarget.create(Warden::getEntityAngryAt), TryToSniff.create(), new RunOne((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.IS_SNIFFING, (Object)((Object)MemoryStatus.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(RandomStroll.stroll(0.5f), (Object)2), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1)))));
    }

    private static void initInvestigateActivity(Brain<Warden> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.INVESTIGATE, 5, (ImmutableList<BehaviorControl<Warden>>)ImmutableList.of(SetRoarTarget.create(Warden::getEntityAngryAt), GoToTargetLocation.create(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7f)), MemoryModuleType.DISTURBANCE_LOCATION);
    }

    private static void initSniffingActivity(Brain<Warden> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.SNIFF, 5, (ImmutableList<BehaviorControl<Warden>>)ImmutableList.of(SetRoarTarget.create(Warden::getEntityAngryAt), new Sniffing(SNIFFING_DURATION)), MemoryModuleType.IS_SNIFFING);
    }

    private static void initRoarActivity(Brain<Warden> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.ROAR, 10, (ImmutableList<BehaviorControl<Warden>>)ImmutableList.of((Object)new Roar()), MemoryModuleType.ROAR_TARGET);
    }

    private static void initFightActivity(Warden $$0, Brain<Warden> $$12) {
        $$12.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, (ImmutableList<BehaviorControl<Warden>>)ImmutableList.of(DIG_COOLDOWN_SETTER, StopAttackingIfTargetInvalid.create(($$1, $$2) -> !$$0.getAngerLevel().isAngry() || !$$0.canTargetEntity($$2), WardenAi::onTargetInvalid, false), SetEntityLookTarget.create($$1 -> WardenAi.isTarget($$0, $$1), (float)$$0.getAttributeValue(Attributes.FOLLOW_RANGE)), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2f), (Object)new SonicBoom(), MeleeAttack.create(18)), MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean isTarget(Warden $$0, LivingEntity $$12) {
        return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter($$1 -> $$1 == $$12).isPresent();
    }

    private static void onTargetInvalid(ServerLevel $$0, Warden $$1, LivingEntity $$2) {
        if (!$$1.canTargetEntity($$2)) {
            $$1.clearAnger($$2);
        }
        WardenAi.setDigCooldown($$1);
    }

    public static void setDigCooldown(LivingEntity $$0) {
        if ($$0.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN)) {
            $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        }
    }

    public static void setDisturbanceLocation(Warden $$0, BlockPos $$1) {
        if (!$$0.level().getWorldBorder().isWithinBounds($$1) || $$0.getEntityAngryAt().isPresent() || $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            return;
        }
        WardenAi.setDigCooldown($$0);
        $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
        $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$1), 100L);
        $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, $$1, 100L);
        $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}

