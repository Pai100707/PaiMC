/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindWater;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.PlayDead;
import net.minecraft.world.entity.animal.axolotl.ValidatePlayDead;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;

public class AxolotlAi {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.2f;
    private static final float SPEED_MULTIPLIER_ON_LAND = 0.15f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 0.5f;
    private static final float SPEED_MULTIPLIER_WHEN_CHASING_IN_WATER = 0.6f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT_IN_WATER = 0.6f;

    protected static Brain<?> makeBrain(Brain<Axolotl> $$0) {
        AxolotlAi.initCoreActivity($$0);
        AxolotlAi.initIdleActivity($$0);
        AxolotlAi.initFightActivity($$0);
        AxolotlAi.initPlayDeadActivity($$0);
        $$0.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$0.setDefaultActivity(Activity.IDLE);
        $$0.useDefaultActivity();
        return $$0;
    }

    private static void initPlayDeadActivity(Brain<Axolotl> $$0) {
        $$0.addActivityAndRemoveMemoriesWhenStopped(Activity.PLAY_DEAD, (ImmutableList<Pair<Integer, BehaviorControl<Axolotl>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new PlayDead()), (Object)Pair.of((Object)1, EraseMemoryIf.create(BehaviorUtils::isBreeding, MemoryModuleType.PLAY_DEAD_TICKS))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.PLAY_DEAD_TICKS, (Object)((Object)MemoryStatus.VALUE_PRESENT))), (Set<MemoryModuleType<?>>)ImmutableSet.of(MemoryModuleType.PLAY_DEAD_TICKS));
    }

    private static void initFightActivity(Brain<Axolotl> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, (ImmutableList<BehaviorControl<Axolotl>>)ImmutableList.of(StopAttackingIfTargetInvalid.create(Axolotl::onStopAttacking), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(AxolotlAi::getSpeedModifierChasing), MeleeAttack.create(20), EraseMemoryIf.create(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCoreActivity(Brain<Axolotl> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Axolotl>>)ImmutableList.of((Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), ValidatePlayDead.create(), (Object)new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Axolotl> $$0) {
        $$0.addActivity(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Axolotl>>>)ImmutableList.of((Object)Pair.of((Object)0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)1, (Object)new AnimalMakeLove(EntityType.AXOLOTL, 0.2f, 2)), (Object)Pair.of((Object)2, new RunOne(ImmutableList.of((Object)Pair.of((Object)new FollowTemptation(AxolotlAi::getSpeedModifier), (Object)1), (Object)Pair.of(BabyFollowAdult.create(ADULT_FOLLOW_RANGE, AxolotlAi::getSpeedModifierFollowingAdult, MemoryModuleType.NEAREST_VISIBLE_ADULT, false), (Object)1)))), (Object)Pair.of((Object)3, StartAttacking.create(AxolotlAi::findNearestValidAttackTarget)), (Object)Pair.of((Object)3, TryFindWater.create(6, 0.15f)), (Object)Pair.of((Object)4, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), (Set<MemoryModuleType<?>>)ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of((Object)Pair.of(RandomStroll.swim(0.5f), (Object)2), (Object)Pair.of(RandomStroll.stroll(0.15f, false), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(AxolotlAi::canSetWalkTargetFromLookTarget, AxolotlAi::getSpeedModifier, 3), (Object)3), (Object)Pair.of(BehaviorBuilder.triggerIf(Entity::isInWater), (Object)5), (Object)Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), (Object)5))))));
    }

    private static boolean canSetWalkTargetFromLookTarget(LivingEntity $$0) {
        Level $$1 = $$0.level();
        Optional<PositionTracker> $$2 = $$0.getBrain().getMemory(MemoryModuleType.LOOK_TARGET);
        if ($$2.isPresent()) {
            BlockPos $$3 = $$2.get().currentBlockPosition();
            return $$1.isWaterAt($$3) == $$0.isInWater();
        }
        return false;
    }

    public static void updateActivity(Axolotl $$0) {
        Brain<Axolotl> $$1 = $$0.getBrain();
        Activity $$2 = $$1.getActiveNonCoreActivity().orElse(null);
        if ($$2 != Activity.PLAY_DEAD) {
            $$1.setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.PLAY_DEAD, (Object)Activity.FIGHT, (Object)Activity.IDLE));
            if ($$2 == Activity.FIGHT && $$1.getActiveNonCoreActivity().orElse(null) != Activity.FIGHT) {
                $$1.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
            }
        }
    }

    private static float getSpeedModifierChasing(LivingEntity $$0) {
        return $$0.isInWater() ? 0.6f : 0.15f;
    }

    private static float getSpeedModifierFollowingAdult(LivingEntity $$0) {
        return $$0.isInWater() ? 0.6f : 0.15f;
    }

    private static float getSpeedModifier(LivingEntity $$0) {
        return $$0.isInWater() ? 0.5f : 0.15f;
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel $$0, Axolotl $$1) {
        if (BehaviorUtils.isBreeding($$1)) {
            return Optional.empty();
        }
        return $$1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }
}

