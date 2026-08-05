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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
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
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

public class NautilusAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.3f;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.4f;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 1.6f;
    private static final UniformInt TIME_BETWEEN_NON_PLAYER_ATTACKS = UniformInt.of(2400, 3600);
    private static final float SPEED_WHEN_ATTACKING = 0.6f;
    private static final float ATTACK_KNOCKBACK_FORCE = 2.0f;
    private static final int ANGER_DURATION = 400;
    private static final int TIME_BETWEEN_ATTACKS = 80;
    private static final double MAX_CHARGE_DISTANCE = 12.0;
    private static final double MAX_TARGET_DETECTION_DISTANCE = 11.0;
    protected static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().selector(($$0, $$1) -> ($$1.getGameRules().get(GameRules.MOB_GRIEFING) != false || !$$0.getType().equals(EntityType.ARMOR_STAND)) && $$1.getWorldBorder().isWithinBounds($$0.getBoundingBox()));
    protected static final ImmutableList<SensorType<? extends Sensor<? super Nautilus>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NAUTILUS_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING, MemoryModuleType.ATTACK_TARGET, (Object[])new MemoryModuleType[]{MemoryModuleType.CHARGE_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.ANGRY_AT, MemoryModuleType.ATTACK_TARGET_COOLDOWN});

    protected static void initMemories(AbstractNautilus $$0, RandomSource $$1) {
        $$0.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET_COOLDOWN, TIME_BETWEEN_NON_PLAYER_ATTACKS.sample($$1));
    }

    protected static Brain.Provider<Nautilus> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Nautilus> $$0) {
        NautilusAi.initCoreActivity($$0);
        NautilusAi.initIdleActivity($$0);
        NautilusAi.initFightActivity($$0);
        $$0.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$0.setDefaultActivity(Activity.IDLE);
        $$0.useDefaultActivity();
        return $$0;
    }

    private static void initCoreActivity(Brain<Nautilus> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Nautilus>>)ImmutableList.of(new AnimalPanic(1.6f), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.CHARGE_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.ATTACK_TARGET_COOLDOWN)));
    }

    private static void initIdleActivity(Brain<Nautilus> $$02) {
        $$02.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Nautilus>>>)ImmutableList.of((Object)Pair.of((Object)1, (Object)new AnimalMakeLove(EntityType.NAUTILUS, 0.4f, 2)), (Object)Pair.of((Object)2, (Object)new FollowTemptation($$0 -> Float.valueOf(1.3f), $$0 -> $$0.isBaby() ? 2.5 : 3.5)), (Object)Pair.of((Object)3, StartAttacking.create(NautilusAi::findNearestValidAttackTarget)), (Object)Pair.of((Object)4, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Set<MemoryModuleType<?>>)ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of((Object)Pair.of(RandomStroll.swim(1.0f), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)3))))));
    }

    private static void initFightActivity(Brain<Nautilus> $$0) {
        $$0.addActivityWithConditions(Activity.FIGHT, (ImmutableList<Pair<Integer, BehaviorControl<Nautilus>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new ChargeAttack(80, ATTACK_TARGET_CONDITIONS, 0.6f, 2.0f, 12.0, 11.0, SoundEvents.NAUTILUS_DASH))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), (Object)Pair.of(MemoryModuleType.TEMPTING_PLAYER, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.CHARGE_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT))));
    }

    protected static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel $$0, AbstractNautilus $$1) {
        if (BehaviorUtils.isBreeding($$1) || !$$1.isInWater() || $$1.isBaby() || $$1.isTame()) {
            return Optional.empty();
        }
        Optional<LivingEntity> $$22 = BehaviorUtils.getLivingEntityFromUUIDMemory($$1, MemoryModuleType.ANGRY_AT).filter($$2 -> $$2.isInWater() && Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2));
        if ($$22.isPresent()) {
            return $$22;
        }
        if ($$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET_COOLDOWN)) {
            return Optional.empty();
        }
        $$1.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET_COOLDOWN, TIME_BETWEEN_NON_PLAYER_ATTACKS.sample($$0.random));
        if ($$0.random.nextFloat() < 0.5f) {
            return Optional.empty();
        }
        Optional<LivingEntity> $$3 = $$1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty()).findClosest(NautilusAi::isHostileTarget);
        return $$3;
    }

    protected static void setAngerTarget(ServerLevel $$0, AbstractNautilus $$1, LivingEntity $$2) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2)) {
            $$1.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, $$2.getUUID(), 400L);
        }
    }

    private static boolean isHostileTarget(LivingEntity $$0) {
        return $$0.isInWater() && $$0.getType().is(EntityTypeTags.NAUTILUS_HOSTILES);
    }

    public static void updateActivity(Nautilus $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.FIGHT, (Object)Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return $$0 -> $$0.is(ItemTags.NAUTILUS_FOOD);
    }
}

