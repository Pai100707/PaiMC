package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

public class HoglinAi {
   public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
   public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
   private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
   private static final int ATTACK_DURATION = 200;
   private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_IDLING = 8;
   private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_RETREATING = 15;
   private static final int ATTACK_INTERVAL = 40;
   private static final int BABY_ATTACK_INTERVAL = 15;
   private static final int REPELLENT_PACIFY_TIME = 200;
   private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
   private static final float SPEED_MULTIPLIER_WHEN_AVOIDING_REPELLENT = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.3F;
   private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.6F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
   private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 0.6F;

   protected static Brain<?> makeBrain(Brain<Hoglin> $$0) {
      initCoreActivity($$0);
      initIdleActivity($$0);
      initFightActivity($$0);
      initRetreatActivity($$0);
      $$0.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$0.setDefaultActivity(Activity.IDLE);
      $$0.useDefaultActivity();
      return $$0;
   }

   private static void initCoreActivity(Brain<Hoglin> $$0) {
      $$0.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
   }

   private static void initIdleActivity(Brain<Hoglin> $$0) {
      $$0.addActivity(
         Activity.IDLE,
         10,
         ImmutableList.of(
            BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200),
            new AnimalMakeLove(net.minecraft.world.entity.EntityType.HOGLIN, 0.6F, 2),
            SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true),
            StartAttacking.create(HoglinAi::findNearestValidAttackTarget),
            BehaviorBuilder.triggerIf(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)),
            SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
            BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 0.6F),
            createIdleMovementBehaviors()
         )
      );
   }

   private static void initFightActivity(Brain<Hoglin> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.FIGHT,
         10,
         ImmutableList.of(
            BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200),
            new AnimalMakeLove(net.minecraft.world.entity.EntityType.HOGLIN, 0.6F, 2),
            SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
            BehaviorBuilder.triggerIf(Hoglin::isAdult, MeleeAttack.create(40)),
            BehaviorBuilder.triggerIf(net.minecraft.world.entity.AgeableMob::isBaby, MeleeAttack.create(15)),
            StopAttackingIfTargetInvalid.create(),
            EraseMemoryIf.create(HoglinAi::isBreeding, MemoryModuleType.ATTACK_TARGET)
         ),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private static void initRetreatActivity(Brain<Hoglin> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.AVOID,
         10,
         ImmutableList.of(
            SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false),
            createIdleMovementBehaviors(),
            SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
            EraseMemoryIf.create(HoglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
         ),
         MemoryModuleType.AVOID_TARGET
      );
   }

   private static RunOne<Hoglin> createIdleMovementBehaviors() {
      return new RunOne<>(
         ImmutableList.of(Pair.of(RandomStroll.stroll(0.4F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1))
      );
   }

   protected static void updateActivity(Hoglin $$0) {
      Brain<Hoglin> $$1 = $$0.getBrain();
      Activity $$2 = $$1.getActiveNonCoreActivity().orElse(null);
      $$1.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
      Activity $$3 = $$1.getActiveNonCoreActivity().orElse(null);
      if ($$2 != $$3) {
         getSoundForCurrentActivity($$0).ifPresent($$0::makeSound);
      }

      $$0.setAggressive($$1.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   protected static void onHitTarget(Hoglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      if (!$$0.isBaby()) {
         if ($$1.getType() == net.minecraft.world.entity.EntityType.PIGLIN && piglinsOutnumberHoglins($$0)) {
            setAvoidTarget($$0, $$1);
            broadcastRetreat($$0, $$1);
         } else {
            broadcastAttackTarget($$0, $$1);
         }
      }
   }

   private static void broadcastRetreat(Hoglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      getVisibleAdultHoglins($$0).forEach($$1x -> retreatFromNearestTarget($$1x, $$1));
   }

   private static void retreatFromNearestTarget(Hoglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Brain<Hoglin> $$3 = $$0.getBrain();
      net.minecraft.world.entity.LivingEntity $$2 = BehaviorUtils.getNearestTarget($$0, $$3.getMemory(MemoryModuleType.AVOID_TARGET), $$1);
      $$2 = BehaviorUtils.getNearestTarget($$0, $$3.getMemory(MemoryModuleType.ATTACK_TARGET), $$2);
      setAvoidTarget($$0, $$2);
   }

   private static void setAvoidTarget(Hoglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$0.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, $$1, RETREAT_DURATION.sample($$0.level().random));
   }

   private static Optional<? extends net.minecraft.world.entity.LivingEntity> findNearestValidAttackTarget(ServerLevel $$0, Hoglin $$1) {
      return !isPacified($$1) && !isBreeding($$1) ? $$1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
   }

   static boolean isPosNearNearestRepellent(Hoglin $$0, BlockPos $$1) {
      Optional<BlockPos> $$2 = $$0.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT);
      return $$2.isPresent() && $$2.get().closerThan($$1, 8.0);
   }

   private static boolean wantsToStopFleeing(Hoglin $$0) {
      return $$0.isAdult() && !piglinsOutnumberHoglins($$0);
   }

   private static boolean piglinsOutnumberHoglins(Hoglin $$0) {
      if ($$0.isBaby()) {
         return false;
      } else {
         int $$1 = $$0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
         int $$2 = $$0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
         return $$1 > $$2;
      }
   }

   protected static void wasHurtBy(ServerLevel $$0, Hoglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      Brain<Hoglin> $$3 = $$1.getBrain();
      $$3.eraseMemory(MemoryModuleType.PACIFIED);
      $$3.eraseMemory(MemoryModuleType.BREED_TARGET);
      if ($$1.isBaby()) {
         retreatFromNearestTarget($$1, $$2);
      } else {
         maybeRetaliate($$0, $$1, $$2);
      }
   }

   private static void maybeRetaliate(ServerLevel $$0, Hoglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      if (!$$1.getBrain().isActive(Activity.AVOID) || $$2.getType() != net.minecraft.world.entity.EntityType.PIGLIN) {
         if ($$2.getType() != net.minecraft.world.entity.EntityType.HOGLIN) {
            if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget($$1, $$2, 4.0)) {
               if (Sensor.isEntityAttackable($$0, $$1, $$2)) {
                  setAttackTarget($$1, $$2);
                  broadcastAttackTarget($$1, $$2);
               }
            }
         }
      }
   }

   private static void setAttackTarget(Hoglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Brain<Hoglin> $$2 = $$0.getBrain();
      $$2.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      $$2.eraseMemory(MemoryModuleType.BREED_TARGET);
      $$2.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, $$1, 200L);
   }

   private static void broadcastAttackTarget(Hoglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      getVisibleAdultHoglins($$0).forEach($$1x -> setAttackTargetIfCloserThanCurrent($$1x, $$1));
   }

   private static void setAttackTargetIfCloserThanCurrent(Hoglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      if (!isPacified($$0)) {
         Optional<net.minecraft.world.entity.LivingEntity> $$2 = $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
         net.minecraft.world.entity.LivingEntity $$3 = BehaviorUtils.getNearestTarget($$0, $$2, $$1);
         setAttackTarget($$0, $$3);
      }
   }

   public static Optional<SoundEvent> getSoundForCurrentActivity(Hoglin $$0) {
      return $$0.getBrain().getActiveNonCoreActivity().map($$1 -> getSoundForActivity($$0, $$1));
   }

   private static SoundEvent getSoundForActivity(Hoglin $$0, Activity $$1) {
      if ($$1 == Activity.AVOID || $$0.isConverting()) {
         return SoundEvents.HOGLIN_RETREAT;
      } else if ($$1 == Activity.FIGHT) {
         return SoundEvents.HOGLIN_ANGRY;
      } else {
         return isNearRepellent($$0) ? SoundEvents.HOGLIN_RETREAT : SoundEvents.HOGLIN_AMBIENT;
      }
   }

   private static List<Hoglin> getVisibleAdultHoglins(Hoglin $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
   }

   private static boolean isNearRepellent(Hoglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean isBreeding(Hoglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
   }

   protected static boolean isPacified(Hoglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
   }
}
