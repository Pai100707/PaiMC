package net.minecraft.world.entity.animal.armadillo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.TimeUtil;
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
import net.minecraft.world.entity.ai.behavior.OneShot;
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

public class ArmadilloAi {
   private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;
   private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.25F;
   private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 1.0F;
   private static final double DEFAULT_CLOSE_ENOUGH_DIST = 2.0;
   private static final double BABY_CLOSE_ENOUGH_DIST = 1.0;
   private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
   private static final ImmutableList<SensorType<? extends Sensor<? super Armadillo>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS, SensorType.NEAREST_ADULT, SensorType.ARMADILLO_SCARE_DETECTED
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
      new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.DANGER_DETECTED_RECENTLY}
   );
   private static final OneShot<Armadillo> ARMADILLO_ROLLING_OUT = BehaviorBuilder.create(
      $$0 -> $$0.group($$0.absent(MemoryModuleType.DANGER_DETECTED_RECENTLY)).apply($$0, $$0x -> ($$0xx, $$1, $$2) -> {
         if ($$1.isScared()) {
            $$1.rollOut();
            return true;
         } else {
            return false;
         }
      })
   );

   public static Brain.Provider<Armadillo> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected static Brain<?> makeBrain(Brain<Armadillo> $$0) {
      initCoreActivity($$0);
      initIdleActivity($$0);
      initScaredActivity($$0);
      $$0.setCoreActivities(Set.of(Activity.CORE));
      $$0.setDefaultActivity(Activity.IDLE);
      $$0.useDefaultActivity();
      return $$0;
   }

   private static void initCoreActivity(Brain<Armadillo> $$0) {
      $$0.addActivity(
         Activity.CORE,
         0,
         ImmutableList.of(
            new Swim(0.8F),
            new ArmadilloAi.ArmadilloPanic(2.0F),
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink() {
               @Override
               protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.Mob $$1) {
                  return $$1 instanceof Armadillo $$2 && $$2.isScared() ? false : super.checkExtraStartConditions($$0, $$1);
               }
            },
            new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
            new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS),
            ARMADILLO_ROLLING_OUT
         )
      );
   }

   private static void initIdleActivity(Brain<Armadillo> $$0) {
      $$0.addActivity(
         Activity.IDLE,
         ImmutableList.of(
            Pair.of(0, SetEntityLookTargetSometimes.create(net.minecraft.world.entity.EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
            Pair.of(1, new AnimalMakeLove(net.minecraft.world.entity.EntityType.ARMADILLO, 1.0F, 1)),
            Pair.of(
               2,
               new RunOne(
                  ImmutableList.of(
                     Pair.of(new FollowTemptation($$0x -> 1.25F, $$0x -> $$0x.isBaby() ? 1.0 : 2.0), 1),
                     Pair.of(BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25F), 1)
                  )
               )
            ),
            Pair.of(3, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
            Pair.of(
               4,
               new RunOne(
                  ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                  ImmutableList.of(
                     Pair.of(RandomStroll.stroll(1.0F), 1), Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1), Pair.of(new DoNothing(30, 60), 1)
                  )
               )
            )
         )
      );
   }

   private static void initScaredActivity(Brain<Armadillo> $$0) {
      $$0.addActivityWithConditions(
         Activity.PANIC,
         ImmutableList.of(Pair.of(0, new ArmadilloAi.ArmadilloBallUp())),
         Set.of(
            Pair.of(MemoryModuleType.DANGER_DETECTED_RECENTLY, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT)
         )
      );
   }

   public static void updateActivity(Armadillo $$0) {
      $$0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.PANIC, Activity.IDLE));
   }

   public static class ArmadilloBallUp extends Behavior<Armadillo> {
      static final int BALL_UP_STAY_IN_STATE = 5 * TimeUtil.SECONDS_PER_MINUTE * 20;
      static final int TICKS_DELAY_TO_DETERMINE_IF_DANGER_IS_STILL_AROUND = 5;
      static final int DANGER_DETECTED_RECENTLY_DANGER_THRESHOLD = 75;
      int nextPeekTimer = 0;
      boolean dangerWasAround;

      public ArmadilloBallUp() {
         super(Map.of(), BALL_UP_STAY_IN_STATE);
      }

      protected void tick(ServerLevel $$0, Armadillo $$1, long $$2) {
         super.tick($$0, $$1, $$2);
         if (this.nextPeekTimer > 0) {
            this.nextPeekTimer--;
         }

         if ($$1.shouldSwitchToScaredState()) {
            $$1.switchToState(Armadillo.ArmadilloState.SCARED);
            if ($$1.onGround()) {
               $$1.playSound(SoundEvents.ARMADILLO_LAND);
            }
         } else {
            Armadillo.ArmadilloState $$3 = $$1.getState();
            long $$4 = $$1.getBrain().getTimeUntilExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY);
            boolean $$5 = $$4 > 75L;
            if ($$5 != this.dangerWasAround) {
               this.nextPeekTimer = this.pickNextPeekTimer($$1);
            }

            this.dangerWasAround = $$5;
            if ($$3 == Armadillo.ArmadilloState.SCARED) {
               if (this.nextPeekTimer == 0 && $$1.onGround() && $$5) {
                  $$0.broadcastEntityEvent($$1, (byte)64);
                  this.nextPeekTimer = this.pickNextPeekTimer($$1);
               }

               if ($$4 < Armadillo.ArmadilloState.UNROLLING.animationDuration()) {
                  $$1.playSound(SoundEvents.ARMADILLO_UNROLL_START);
                  $$1.switchToState(Armadillo.ArmadilloState.UNROLLING);
               }
            } else if ($$3 == Armadillo.ArmadilloState.UNROLLING && $$4 > Armadillo.ArmadilloState.UNROLLING.animationDuration()) {
               $$1.switchToState(Armadillo.ArmadilloState.SCARED);
            }
         }
      }

      private int pickNextPeekTimer(Armadillo $$0) {
         return Armadillo.ArmadilloState.SCARED.animationDuration() + $$0.getRandom().nextIntBetweenInclusive(100, 400);
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Armadillo $$1) {
         return $$1.onGround();
      }

      protected boolean canStillUse(ServerLevel $$0, Armadillo $$1, long $$2) {
         return $$1.getState().isThreatened();
      }

      protected void start(ServerLevel $$0, Armadillo $$1, long $$2) {
         $$1.rollUp();
      }

      protected void stop(ServerLevel $$0, Armadillo $$1, long $$2) {
         if (!$$1.canStayRolledUp()) {
            $$1.rollOut();
         }
      }
   }

   public static class ArmadilloPanic extends AnimalPanic<Armadillo> {
      public ArmadilloPanic(float $$0) {
         super($$0, $$0x -> DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES);
      }

      protected void start(ServerLevel $$0, Armadillo $$1, long $$2) {
         $$1.rollOut();
         super.start($$0, $$1, $$2);
      }
   }
}
