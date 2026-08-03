package net.minecraft.world.entity.animal.camel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.Behavior;
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
import net.minecraft.world.entity.schedule.Activity;

public class CamelAi {
   private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 4.0F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 2.0F;
   private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 2.5F;
   private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 2.5F;
   private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0F;
   private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
   private static final ImmutableList<SensorType<? extends Sensor<? super Camel>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS, SensorType.NEAREST_ADULT
   );
   private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
      MemoryModuleType.IS_PANICKING,
      MemoryModuleType.HURT_BY,
      MemoryModuleType.HURT_BY_ENTITY,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.TEMPTING_PLAYER,
      MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
      MemoryModuleType.GAZE_COOLDOWN_TICKS,
      MemoryModuleType.IS_TEMPTED,
      new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT}
   );

   protected static void initMemories(Camel $$0, RandomSource $$1) {
   }

   public static Brain.Provider<Camel> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected static Brain<?> makeBrain(Brain<Camel> $$0) {
      initCoreActivity($$0);
      initIdleActivity($$0);
      $$0.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$0.setDefaultActivity(Activity.IDLE);
      $$0.useDefaultActivity();
      return $$0;
   }

   private static void initCoreActivity(Brain<Camel> $$0) {
      $$0.addActivity(
         Activity.CORE,
         0,
         ImmutableList.of(
            new Swim(0.8F),
            new CamelAi.CamelPanic(4.0F),
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink(),
            new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
            new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS)
         )
      );
   }

   private static void initIdleActivity(Brain<Camel> $$0) {
      $$0.addActivity(
         Activity.IDLE,
         ImmutableList.of(
            Pair.of(0, SetEntityLookTargetSometimes.create(net.minecraft.world.entity.EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
            Pair.of(1, new AnimalMakeLove(net.minecraft.world.entity.EntityType.CAMEL)),
            Pair.of(
               2,
               new RunOne(
                  ImmutableList.of(
                     Pair.of(new FollowTemptation($$0x -> 2.5F, $$0x -> $$0x.isBaby() ? 2.5 : 3.5), 1),
                     Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 2.5F)), 1)
                  )
               )
            ),
            Pair.of(3, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
            Pair.of(
               4,
               new RunOne(
                  ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                  ImmutableList.of(
                     Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), RandomStroll.stroll(2.0F)), 1),
                     Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), SetWalkTargetFromLookTarget.create(2.0F, 3)), 1),
                     Pair.of(new CamelAi.RandomSitting(20), 1),
                     Pair.of(new DoNothing(30, 60), 1)
                  )
               )
            )
         )
      );
   }

   public static void updateActivity(Camel $$0) {
      $$0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
   }

   public static class CamelPanic extends AnimalPanic<Camel> {
      public CamelPanic(float $$0) {
         super($$0);
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Camel $$1) {
         return super.checkExtraStartConditions($$0, $$1) && !$$1.isMobControlled();
      }

      protected void start(ServerLevel $$0, Camel $$1, long $$2) {
         $$1.standUpInstantly();
         super.start($$0, $$1, $$2);
      }
   }

   public static class RandomSitting extends Behavior<Camel> {
      private final int minimalPoseTicks;

      public RandomSitting(int $$0) {
         super(ImmutableMap.of());
         this.minimalPoseTicks = $$0 * 20;
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Camel $$1) {
         return !$$1.isInWater()
            && $$1.getPoseTime() >= this.minimalPoseTicks
            && !$$1.isLeashed()
            && $$1.onGround()
            && !$$1.hasControllingPassenger()
            && $$1.canCamelChangePose();
      }

      protected void start(ServerLevel $$0, Camel $$1, long $$2) {
         if ($$1.isCamelSitting()) {
            $$1.standUp();
         } else if (!$$1.isPanicking()) {
            $$1.sitDown();
         }
      }
   }
}
