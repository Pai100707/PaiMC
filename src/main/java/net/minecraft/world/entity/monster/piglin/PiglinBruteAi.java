package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinBruteAi {
   private static final int ANGER_DURATION = 600;
   private static final int MELEE_ATTACK_COOLDOWN = 20;
   private static final double ACTIVITY_SOUND_LIKELIHOOD_PER_TICK = 0.0125;
   private static final int MAX_LOOK_DIST = 8;
   private static final int INTERACTION_RANGE = 8;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;
   private static final int HOME_CLOSE_ENOUGH_DISTANCE = 2;
   private static final int HOME_TOO_FAR_DISTANCE = 100;
   private static final int HOME_STROLL_AROUND_DISTANCE = 5;

   protected static Brain<?> makeBrain(PiglinBrute $$0, Brain<PiglinBrute> $$1) {
      initCoreActivity($$0, $$1);
      initIdleActivity($$0, $$1);
      initFightActivity($$0, $$1);
      $$1.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$1.setDefaultActivity(Activity.IDLE);
      $$1.useDefaultActivity();
      return $$1;
   }

   protected static void initMemories(PiglinBrute $$0) {
      GlobalPos $$1 = GlobalPos.of($$0.level().dimension(), $$0.blockPosition());
      $$0.getBrain().setMemory(MemoryModuleType.HOME, $$1);
   }

   private static void initCoreActivity(PiglinBrute $$0, Brain<PiglinBrute> $$1) {
      $$1.addActivity(
         Activity.CORE,
         0,
         ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), InteractWithDoor.create(), StopBeingAngryIfTargetDead.create())
      );
   }

   private static void initIdleActivity(PiglinBrute $$0, Brain<PiglinBrute> $$1) {
      $$1.addActivity(
         Activity.IDLE,
         10,
         ImmutableList.of(
            StartAttacking.create(PiglinBruteAi::findNearestValidAttackTarget),
            createIdleLookBehaviors(),
            createIdleMovementBehaviors(),
            SetLookAndInteract.create(net.minecraft.world.entity.EntityType.PLAYER, 4)
         )
      );
   }

   private static void initFightActivity(PiglinBrute $$0, Brain<PiglinBrute> $$1) {
      $$1.addActivityAndRemoveMemoryWhenStopped(
         Activity.FIGHT,
         10,
         ImmutableList.of(
            StopAttackingIfTargetInvalid.create(($$1x, $$2) -> !isNearestValidAttackTarget($$1x, $$0, $$2)),
            SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
            MeleeAttack.create(20)
         ),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private static RunOne<PiglinBrute> createIdleLookBehaviors() {
      return new RunOne<>(
         ImmutableList.of(
            Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PLAYER, 8.0F), 1),
            Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PIGLIN, 8.0F), 1),
            Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PIGLIN_BRUTE, 8.0F), 1),
            Pair.of(SetEntityLookTarget.create(8.0F), 1),
            Pair.of(new DoNothing(30, 60), 1)
         )
      );
   }

   private static RunOne<PiglinBrute> createIdleMovementBehaviors() {
      return new RunOne<>(
         ImmutableList.of(
            Pair.of(RandomStroll.stroll(0.6F), 2),
            Pair.of(InteractWith.of(net.minecraft.world.entity.EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
            Pair.of(InteractWith.of(net.minecraft.world.entity.EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
            Pair.of(StrollToPoi.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2),
            Pair.of(StrollAroundPoi.create(MemoryModuleType.HOME, 0.6F, 5), 2),
            Pair.of(new DoNothing(30, 60), 1)
         )
      );
   }

   protected static void updateActivity(PiglinBrute $$0) {
      Brain<PiglinBrute> $$1 = $$0.getBrain();
      Activity $$2 = $$1.getActiveNonCoreActivity().orElse(null);
      $$1.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity $$3 = $$1.getActiveNonCoreActivity().orElse(null);
      if ($$2 != $$3) {
         playActivitySound($$0);
      }

      $$0.setAggressive($$1.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   private static boolean isNearestValidAttackTarget(ServerLevel $$0, AbstractPiglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      return findNearestValidAttackTarget($$0, $$1).filter($$1x -> $$1x == $$2).isPresent();
   }

   private static Optional<? extends net.minecraft.world.entity.LivingEntity> findNearestValidAttackTarget(ServerLevel $$0, AbstractPiglin $$1) {
      Optional<net.minecraft.world.entity.LivingEntity> $$2 = BehaviorUtils.getLivingEntityFromUUIDMemory($$1, MemoryModuleType.ANGRY_AT);
      if ($$2.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2.get())) {
         return $$2;
      } else {
         Optional<? extends net.minecraft.world.entity.LivingEntity> $$3 = $$1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
         return $$3.isPresent() ? $$3 : $$1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
      }
   }

   protected static void wasHurtBy(ServerLevel $$0, PiglinBrute $$1, net.minecraft.world.entity.LivingEntity $$2) {
      if (!($$2 instanceof AbstractPiglin)) {
         PiglinAi.maybeRetaliate($$0, $$1, $$2);
      }
   }

   protected static void setAngerTarget(PiglinBrute $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$0.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, $$1.getUUID(), 600L);
   }

   protected static void maybePlayActivitySound(PiglinBrute $$0) {
      if ($$0.level().random.nextFloat() < 0.0125) {
         playActivitySound($$0);
      }
   }

   private static void playActivitySound(PiglinBrute $$0) {
      $$0.getBrain().getActiveNonCoreActivity().ifPresent($$1 -> {
         if ($$1 == Activity.FIGHT) {
            $$0.playAngrySound();
         }
      });
   }
}
