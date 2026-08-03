package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
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
import net.minecraft.world.entity.schedule.Activity;

public class WardenAi {
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.5F;
   private static final float SPEED_MULTIPLIER_WHEN_INVESTIGATING = 0.7F;
   private static final float SPEED_MULTIPLIER_WHEN_FIGHTING = 1.2F;
   private static final int MELEE_ATTACK_COOLDOWN = 18;
   private static final int DIGGING_DURATION = Mth.ceil(100.0F);
   public static final int EMERGE_DURATION = Mth.ceil(133.59999F);
   public static final int ROAR_DURATION = Mth.ceil(84.0F);
   private static final int SNIFFING_DURATION = Mth.ceil(83.2F);
   public static final int DIGGING_COOLDOWN = 1200;
   private static final int DISTURBANCE_LOCATION_EXPIRY_TIME = 100;
   private static final List<SensorType<? extends Sensor<? super Warden>>> SENSOR_TYPES = List.of(SensorType.NEAREST_PLAYERS, SensorType.WARDEN_ENTITY_SENSOR);
   private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
      MemoryModuleType.NEAREST_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
      MemoryModuleType.NEAREST_VISIBLE_PLAYER,
      MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
      MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
      MemoryModuleType.LOOK_TARGET,
      MemoryModuleType.WALK_TARGET,
      MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
      MemoryModuleType.PATH,
      MemoryModuleType.ATTACK_TARGET,
      MemoryModuleType.ATTACK_COOLING_DOWN,
      MemoryModuleType.NEAREST_ATTACKABLE,
      MemoryModuleType.ROAR_TARGET,
      MemoryModuleType.DISTURBANCE_LOCATION,
      MemoryModuleType.RECENT_PROJECTILE,
      MemoryModuleType.IS_SNIFFING,
      MemoryModuleType.IS_EMERGING,
      MemoryModuleType.ROAR_SOUND_DELAY,
      MemoryModuleType.DIG_COOLDOWN,
      MemoryModuleType.ROAR_SOUND_COOLDOWN,
      MemoryModuleType.SNIFF_COOLDOWN,
      MemoryModuleType.TOUCH_COOLDOWN,
      MemoryModuleType.VIBRATION_COOLDOWN,
      MemoryModuleType.SONIC_BOOM_COOLDOWN,
      MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,
      MemoryModuleType.SONIC_BOOM_SOUND_DELAY
   );
   private static final BehaviorControl<Warden> DIG_COOLDOWN_SETTER = BehaviorBuilder.create(
      $$0 -> $$0.group($$0.registered(MemoryModuleType.DIG_COOLDOWN)).apply($$0, $$1 -> ($$2, $$3, $$4) -> {
         if ($$0.tryGet($$1).isPresent()) {
            $$1.setWithExpiry(Unit.INSTANCE, 1200L);
         }

         return true;
      })
   );

   public static void updateActivity(Warden $$0) {
      $$0.getBrain()
         .setActiveActivityToFirstValid(
            ImmutableList.of(Activity.EMERGE, Activity.DIG, Activity.ROAR, Activity.FIGHT, Activity.INVESTIGATE, Activity.SNIFF, Activity.IDLE)
         );
   }

   protected static Brain<?> makeBrain(Warden $$0, Dynamic<?> $$1) {
      Brain.Provider<Warden> $$2 = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
      Brain<Warden> $$3 = $$2.makeBrain($$1);
      initCoreActivity($$3);
      initEmergeActivity($$3);
      initDiggingActivity($$3);
      initIdleActivity($$3);
      initRoarActivity($$3);
      initFightActivity($$0, $$3);
      initInvestigateActivity($$3);
      initSniffingActivity($$3);
      $$3.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$3.setDefaultActivity(Activity.IDLE);
      $$3.useDefaultActivity();
      return $$3;
   }

   private static void initCoreActivity(Brain<Warden> $$0) {
      $$0.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), SetWardenLookTarget.create(), new LookAtTargetSink(45, 90), new MoveToTargetSink()));
   }

   private static void initEmergeActivity(Brain<Warden> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(Activity.EMERGE, 5, ImmutableList.of(new Emerging(EMERGE_DURATION)), MemoryModuleType.IS_EMERGING);
   }

   private static void initDiggingActivity(Brain<Warden> $$0) {
      $$0.addActivityWithConditions(
         Activity.DIG,
         ImmutableList.of(Pair.of(0, new ForceUnmount()), Pair.of(1, new Digging(DIGGING_DURATION))),
         ImmutableSet.of(Pair.of(MemoryModuleType.ROAR_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.DIG_COOLDOWN, MemoryStatus.VALUE_ABSENT))
      );
   }

   private static void initIdleActivity(Brain<Warden> $$0) {
      $$0.addActivity(
         Activity.IDLE,
         10,
         ImmutableList.of(
            SetRoarTarget.create(Warden::getEntityAngryAt),
            TryToSniff.create(),
            new RunOne(
               ImmutableMap.of(MemoryModuleType.IS_SNIFFING, MemoryStatus.VALUE_ABSENT),
               ImmutableList.of(Pair.of(RandomStroll.stroll(0.5F), 2), Pair.of(new DoNothing(30, 60), 1))
            )
         )
      );
   }

   private static void initInvestigateActivity(Brain<Warden> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.INVESTIGATE,
         5,
         ImmutableList.of(SetRoarTarget.create(Warden::getEntityAngryAt), GoToTargetLocation.create(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F)),
         MemoryModuleType.DISTURBANCE_LOCATION
      );
   }

   private static void initSniffingActivity(Brain<Warden> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.SNIFF, 5, ImmutableList.of(SetRoarTarget.create(Warden::getEntityAngryAt), new Sniffing(SNIFFING_DURATION)), MemoryModuleType.IS_SNIFFING
      );
   }

   private static void initRoarActivity(Brain<Warden> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(Activity.ROAR, 10, ImmutableList.of(new Roar()), MemoryModuleType.ROAR_TARGET);
   }

   private static void initFightActivity(Warden $$0, Brain<Warden> $$1) {
      $$1.addActivityAndRemoveMemoryWhenStopped(
         Activity.FIGHT,
         10,
         ImmutableList.of(
            DIG_COOLDOWN_SETTER,
            StopAttackingIfTargetInvalid.create(($$1x, $$2) -> !$$0.getAngerLevel().isAngry() || !$$0.canTargetEntity($$2), WardenAi::onTargetInvalid, false),
            SetEntityLookTarget.create($$1x -> isTarget($$0, $$1x), (float)$$0.getAttributeValue(Attributes.FOLLOW_RANGE)),
            SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F),
            new SonicBoom(),
            MeleeAttack.create(18)
         ),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private static boolean isTarget(Warden $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter($$1x -> $$1x == $$1).isPresent();
   }

   private static void onTargetInvalid(ServerLevel $$0, Warden $$1, net.minecraft.world.entity.LivingEntity $$2) {
      if (!$$1.canTargetEntity($$2)) {
         $$1.clearAnger($$2);
      }

      setDigCooldown($$1);
   }

   public static void setDigCooldown(net.minecraft.world.entity.LivingEntity $$0) {
      if ($$0.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN)) {
         $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
      }
   }

   public static void setDisturbanceLocation(Warden $$0, BlockPos $$1) {
      if ($$0.level().getWorldBorder().isWithinBounds($$1)
         && !$$0.getEntityAngryAt().isPresent()
         && !$$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
         setDigCooldown($$0);
         $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
         $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$1), 100L);
         $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, $$1, 100L);
         $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      }
   }
}
