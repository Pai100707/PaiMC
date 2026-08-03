package net.minecraft.world.entity.animal.sniffer;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.schedule.Activity;
import org.slf4j.Logger;

public class SnifferAi {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_LOOK_DISTANCE = 6;
   static final List<SensorType<? extends Sensor<? super Sniffer>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.FOOD_TEMPTATIONS
   );
   static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.IS_PANICKING,
      MemoryModuleType.SNIFFER_SNIFFING_TARGET,
      MemoryModuleType.SNIFFER_DIGGING,
      MemoryModuleType.SNIFFER_HAPPY,
      MemoryModuleType.SNIFF_COOLDOWN,
      MemoryModuleType.SNIFFER_EXPLORED_POSITIONS,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.BREED_TARGET,
      new MemoryModuleType[]{MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED}
   );
   private static final int SNIFFING_COOLDOWN_TICKS = 9600;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
   private static final float SPEED_MULTIPLIER_WHEN_SNIFFING = 1.25F;
   private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;

   protected static Brain<?> makeBrain(Brain<Sniffer> $$0) {
      initCoreActivity($$0);
      initIdleActivity($$0);
      initSniffingActivity($$0);
      initDigActivity($$0);
      $$0.setCoreActivities(Set.of(Activity.CORE));
      $$0.setDefaultActivity(Activity.IDLE);
      $$0.useDefaultActivity();
      return $$0;
   }

   static Sniffer resetSniffing(Sniffer $$0) {
      $$0.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
      $$0.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
      return $$0.transitionTo(Sniffer.State.IDLING);
   }

   private static void initCoreActivity(Brain<Sniffer> $$0) {
      $$0.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new AnimalPanic<Sniffer>(2.0F) {
         protected void start(ServerLevel $$0, Sniffer $$1, long $$2) {
            SnifferAi.resetSniffing($$1);
            super.start($$0, $$1, $$2);
         }
      }, new MoveToTargetSink(500, 700), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
   }

   private static void initSniffingActivity(Brain<Sniffer> $$0) {
      $$0.addActivityWithConditions(
         Activity.SNIFF,
         ImmutableList.of(Pair.of(0, new SnifferAi.Searching())),
         Set.of(
            Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_PRESENT),
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT)
         )
      );
   }

   private static void initDigActivity(Brain<Sniffer> $$0) {
      $$0.addActivityWithConditions(
         Activity.DIG,
         ImmutableList.of(Pair.of(0, new SnifferAi.Digging(160, 180)), Pair.of(0, new SnifferAi.FinishedDigging(40))),
         Set.of(
            Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_PRESENT)
         )
      );
   }

   private static void initIdleActivity(Brain<Sniffer> $$0) {
      $$0.addActivityWithConditions(
         Activity.IDLE,
         ImmutableList.of(
            Pair.of(0, new AnimalMakeLove(net.minecraft.world.entity.EntityType.SNIFFER) {
               @Override
               protected void start(ServerLevel $$0, Animal $$1, long $$2) {
                  SnifferAi.resetSniffing((Sniffer)$$1);
                  super.start($$0, $$1, $$2);
               }
            }),
            Pair.of(1, new FollowTemptation($$0x -> 1.25F, $$0x -> $$0x.isBaby() ? 2.5 : 3.5) {
               @Override
               protected void start(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
                  SnifferAi.resetSniffing((Sniffer)$$1);
                  super.start($$0, $$1, $$2);
               }
            }),
            Pair.of(2, new LookAtTargetSink(45, 90)),
            Pair.of(3, new SnifferAi.FeelingHappy(40, 100)),
            Pair.of(
               4,
               new RunOne(
                  ImmutableList.of(
                     Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                     Pair.of(new SnifferAi.Scenting(40, 80), 1),
                     Pair.of(new SnifferAi.Sniffing(40, 80), 1),
                     Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PLAYER, 6.0F), 1),
                     Pair.of(RandomStroll.stroll(1.0F), 1),
                     Pair.of(new DoNothing(5, 20), 2)
                  )
               )
            )
         ),
         Set.of(Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_ABSENT))
      );
   }

   static void updateActivity(Sniffer $$0) {
      $$0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.DIG, Activity.SNIFF, Activity.IDLE));
   }

   static class Digging extends Behavior<Sniffer> {
      Digging(int $$0, int $$1) {
         super(
            Map.of(
               MemoryModuleType.IS_PANICKING,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.WALK_TARGET,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFFER_DIGGING,
               MemoryStatus.VALUE_PRESENT,
               MemoryModuleType.SNIFF_COOLDOWN,
               MemoryStatus.VALUE_ABSENT
            ),
            $$0,
            $$1
         );
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Sniffer $$1) {
         return $$1.canSniff();
      }

      protected boolean canStillUse(ServerLevel $$0, Sniffer $$1, long $$2) {
         return $$1.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent() && $$1.canDig() && !$$1.isInLove();
      }

      protected void start(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.DIGGING);
      }

      protected void stop(ServerLevel $$0, Sniffer $$1, long $$2) {
         boolean $$3 = this.timedOut($$2);
         if ($$3) {
            $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 9600L);
         } else {
            SnifferAi.resetSniffing($$1);
         }
      }
   }

   static class FeelingHappy extends Behavior<Sniffer> {
      FeelingHappy(int $$0, int $$1) {
         super(Map.of(MemoryModuleType.SNIFFER_HAPPY, MemoryStatus.VALUE_PRESENT), $$0, $$1);
      }

      protected boolean canStillUse(ServerLevel $$0, Sniffer $$1, long $$2) {
         return true;
      }

      protected void start(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.FEELING_HAPPY);
      }

      protected void stop(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.IDLING);
         $$1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_HAPPY);
      }
   }

   static class FinishedDigging extends Behavior<Sniffer> {
      FinishedDigging(int $$0) {
         super(
            Map.of(
               MemoryModuleType.IS_PANICKING,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.WALK_TARGET,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFFER_DIGGING,
               MemoryStatus.VALUE_PRESENT,
               MemoryModuleType.SNIFF_COOLDOWN,
               MemoryStatus.VALUE_PRESENT
            ),
            $$0,
            $$0
         );
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Sniffer $$1) {
         return true;
      }

      protected boolean canStillUse(ServerLevel $$0, Sniffer $$1, long $$2) {
         return $$1.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent();
      }

      protected void start(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.RISING);
      }

      protected void stop(ServerLevel $$0, Sniffer $$1, long $$2) {
         boolean $$3 = this.timedOut($$2);
         $$1.transitionTo(Sniffer.State.IDLING).onDiggingComplete($$3);
         $$1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
         $$1.getBrain().setMemory(MemoryModuleType.SNIFFER_HAPPY, true);
      }
   }

   static class Scenting extends Behavior<Sniffer> {
      Scenting(int $$0, int $$1) {
         super(
            Map.of(
               MemoryModuleType.IS_PANICKING,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFFER_DIGGING,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFFER_SNIFFING_TARGET,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFFER_HAPPY,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.BREED_TARGET,
               MemoryStatus.VALUE_ABSENT
            ),
            $$0,
            $$1
         );
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Sniffer $$1) {
         return !$$1.isTempted();
      }

      protected boolean canStillUse(ServerLevel $$0, Sniffer $$1, long $$2) {
         return true;
      }

      protected void start(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.SCENTING);
      }

      protected void stop(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.IDLING);
      }
   }

   static class Searching extends Behavior<Sniffer> {
      Searching() {
         super(
            Map.of(
               MemoryModuleType.WALK_TARGET,
               MemoryStatus.VALUE_PRESENT,
               MemoryModuleType.IS_PANICKING,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFFER_SNIFFING_TARGET,
               MemoryStatus.VALUE_PRESENT
            ),
            600
         );
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Sniffer $$1) {
         return $$1.canSniff();
      }

      protected boolean canStillUse(ServerLevel $$0, Sniffer $$1, long $$2) {
         if (!$$1.canSniff()) {
            $$1.transitionTo(Sniffer.State.IDLING);
            return false;
         } else {
            Optional<BlockPos> $$3 = $$1.getBrain()
               .getMemory(MemoryModuleType.WALK_TARGET)
               .map(WalkTarget::getTarget)
               .map(PositionTracker::currentBlockPosition);
            Optional<BlockPos> $$4 = $$1.getBrain().getMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
            return !$$3.isEmpty() && !$$4.isEmpty() ? $$4.get().equals($$3.get()) : false;
         }
      }

      protected void start(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.SEARCHING);
      }

      protected void stop(ServerLevel $$0, Sniffer $$1, long $$2) {
         if ($$1.canDig() && $$1.canSniff()) {
            $$1.getBrain().setMemory(MemoryModuleType.SNIFFER_DIGGING, true);
         }

         $$1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
         $$1.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
      }
   }

   static class Sniffing extends Behavior<Sniffer> {
      Sniffing(int $$0, int $$1) {
         super(
            Map.of(
               MemoryModuleType.WALK_TARGET,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFFER_SNIFFING_TARGET,
               MemoryStatus.VALUE_ABSENT,
               MemoryModuleType.SNIFF_COOLDOWN,
               MemoryStatus.VALUE_ABSENT
            ),
            $$0,
            $$1
         );
      }

      protected boolean checkExtraStartConditions(ServerLevel $$0, Sniffer $$1) {
         return !$$1.isBaby() && $$1.canSniff();
      }

      protected boolean canStillUse(ServerLevel $$0, Sniffer $$1, long $$2) {
         return $$1.canSniff();
      }

      protected void start(ServerLevel $$0, Sniffer $$1, long $$2) {
         $$1.transitionTo(Sniffer.State.SNIFFING);
      }

      protected void stop(ServerLevel $$0, Sniffer $$1, long $$2) {
         boolean $$3 = this.timedOut($$2);
         $$1.transitionTo(Sniffer.State.IDLING);
         if ($$3) {
            $$1.calculateDigPosition().ifPresent($$1x -> {
               $$1.getBrain().setMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET, $$1x);
               $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$1x, 1.25F, 0));
            });
         }
      }
   }
}
