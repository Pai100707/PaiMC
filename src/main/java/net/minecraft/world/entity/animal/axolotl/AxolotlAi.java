package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
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
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;

public class AxolotlAi {
   private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
   private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.2F;
   private static final float SPEED_MULTIPLIER_ON_LAND = 0.15F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 0.5F;
   private static final float SPEED_MULTIPLIER_WHEN_CHASING_IN_WATER = 0.6F;
   private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT_IN_WATER = 0.6F;

   protected static Brain<?> makeBrain(Brain<Axolotl> $$0) {
      initCoreActivity($$0);
      initIdleActivity($$0);
      initFightActivity($$0);
      initPlayDeadActivity($$0);
      $$0.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$0.setDefaultActivity(Activity.IDLE);
      $$0.useDefaultActivity();
      return $$0;
   }

   private static void initPlayDeadActivity(Brain<Axolotl> $$0) {
      $$0.addActivityAndRemoveMemoriesWhenStopped(
         Activity.PLAY_DEAD,
         ImmutableList.of(Pair.of(0, new PlayDead()), Pair.of(1, EraseMemoryIf.create(BehaviorUtils::isBreeding, MemoryModuleType.PLAY_DEAD_TICKS))),
         ImmutableSet.of(Pair.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT)),
         ImmutableSet.of(MemoryModuleType.PLAY_DEAD_TICKS)
      );
   }

   private static void initFightActivity(Brain<Axolotl> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.FIGHT,
         0,
         ImmutableList.of(
            StopAttackingIfTargetInvalid.create(Axolotl::onStopAttacking),
            SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(AxolotlAi::getSpeedModifierChasing),
            MeleeAttack.create(20),
            EraseMemoryIf.create(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)
         ),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private static void initCoreActivity(Brain<Axolotl> $$0) {
      $$0.addActivity(
         Activity.CORE,
         0,
         ImmutableList.of(
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink(),
            ValidatePlayDead.create(),
            new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)
         )
      );
   }

   private static void initIdleActivity(Brain<Axolotl> $$0) {
      $$0.addActivity(
         Activity.IDLE,
         ImmutableList.of(
            Pair.of(0, SetEntityLookTargetSometimes.create(net.minecraft.world.entity.EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
            Pair.of(1, new AnimalMakeLove(net.minecraft.world.entity.EntityType.AXOLOTL, 0.2F, 2)),
            Pair.of(
               2,
               new RunOne(
                  ImmutableList.of(
                     Pair.of(new FollowTemptation(AxolotlAi::getSpeedModifier), 1),
                     Pair.of(
                        BabyFollowAdult.create(ADULT_FOLLOW_RANGE, AxolotlAi::getSpeedModifierFollowingAdult, MemoryModuleType.NEAREST_VISIBLE_ADULT, false), 1
                     )
                  )
               )
            ),
            Pair.of(3, StartAttacking.create(AxolotlAi::findNearestValidAttackTarget)),
            Pair.of(3, TryFindWater.create(6, 0.15F)),
            Pair.of(
               4,
               new GateBehavior(
                  ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                  ImmutableSet.of(),
                  GateBehavior.OrderPolicy.ORDERED,
                  GateBehavior.RunningPolicy.TRY_ALL,
                  ImmutableList.of(
                     Pair.of(RandomStroll.swim(0.5F), 2),
                     Pair.of(RandomStroll.stroll(0.15F, false), 2),
                     Pair.of(SetWalkTargetFromLookTarget.create(AxolotlAi::canSetWalkTargetFromLookTarget, AxolotlAi::getSpeedModifier, 3), 3),
                     Pair.of(BehaviorBuilder.triggerIf(net.minecraft.world.entity.Entity::isInWater), 5),
                     Pair.of(BehaviorBuilder.triggerIf(net.minecraft.world.entity.Entity::onGround), 5)
                  )
               )
            )
         )
      );
   }

   private static boolean canSetWalkTargetFromLookTarget(net.minecraft.world.entity.LivingEntity $$0) {
      Level $$1 = $$0.level();
      Optional<PositionTracker> $$2 = $$0.getBrain().getMemory(MemoryModuleType.LOOK_TARGET);
      if ($$2.isPresent()) {
         BlockPos $$3 = $$2.get().currentBlockPosition();
         return $$1.isWaterAt($$3) == $$0.isInWater();
      } else {
         return false;
      }
   }

   public static void updateActivity(Axolotl $$0) {
      Brain<Axolotl> $$1 = $$0.getBrain();
      Activity $$2 = $$1.getActiveNonCoreActivity().orElse(null);
      if ($$2 != Activity.PLAY_DEAD) {
         $$1.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, Activity.IDLE));
         if ($$2 == Activity.FIGHT && $$1.getActiveNonCoreActivity().orElse(null) != Activity.FIGHT) {
            $$1.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
         }
      }
   }

   private static float getSpeedModifierChasing(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.isInWater() ? 0.6F : 0.15F;
   }

   private static float getSpeedModifierFollowingAdult(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.isInWater() ? 0.6F : 0.15F;
   }

   private static float getSpeedModifier(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.isInWater() ? 0.5F : 0.15F;
   }

   private static Optional<? extends net.minecraft.world.entity.LivingEntity> findNearestValidAttackTarget(ServerLevel $$0, Axolotl $$1) {
      return BehaviorUtils.isBreeding($$1) ? Optional.empty() : $$1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
   }
}
