package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CopyMemoryWithExpiry;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.behavior.DismountOrSkipMounting;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.Mount;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.SpearApproach;
import net.minecraft.world.entity.ai.behavior.SpearAttack;
import net.minecraft.world.entity.ai.behavior.SpearRetreat;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PiglinAi {
   public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
   public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
   public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
   private static final int PLAYER_ANGER_RANGE = 16;
   private static final int ANGER_DURATION = 600;
   private static final int ADMIRE_DURATION = 119;
   private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
   private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
   private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
   private static final int CELEBRATION_TIME = 300;
   protected static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
   private static final int BABY_FLEE_DURATION_AFTER_GETTING_HIT = 100;
   private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
   private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
   private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
   private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
   private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
   private static final int MELEE_ATTACK_COOLDOWN = 20;
   private static final int EAT_COOLDOWN = 200;
   private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
   private static final int MAX_LOOK_DIST = 8;
   private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
   private static final int INTERACTION_RANGE = 8;
   private static final int MIN_DESIRED_DIST_FROM_TARGET_WHEN_HOLDING_CROSSBOW = 5;
   private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75F;
   private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
   private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
   private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
   private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1F;
   private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8F;
   private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;

   protected static Brain<?> makeBrain(Piglin $$0, Brain<Piglin> $$1) {
      initCoreActivity($$1);
      initIdleActivity($$1);
      initAdmireItemActivity($$1);
      initFightActivity($$0, $$1);
      initCelebrateActivity($$1);
      initRetreatActivity($$1);
      initRideHoglinActivity($$1);
      $$1.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$1.setDefaultActivity(Activity.IDLE);
      $$1.useDefaultActivity();
      return $$1;
   }

   protected static void initMemories(Piglin $$0, RandomSource $$1) {
      int $$2 = TIME_BETWEEN_HUNTS.sample($$1);
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, $$2);
   }

   private static void initCoreActivity(Brain<Piglin> $$0) {
      $$0.addActivity(
         Activity.CORE,
         0,
         ImmutableList.of(
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink(),
            InteractWithDoor.create(),
            babyAvoidNemesis(),
            avoidZombified(),
            StopHoldingItemIfNoLongerAdmiring.create(),
            StartAdmiringItemIfSeen.create(119),
            StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance),
            StopBeingAngryIfTargetDead.create()
         )
      );
   }

   private static void initIdleActivity(Brain<Piglin> $$0) {
      $$0.addActivity(
         Activity.IDLE,
         10,
         ImmutableList.of(
            SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
            StartAttacking.create(($$0x, $$1) -> $$1.isAdult(), PiglinAi::findNearestValidAttackTarget),
            BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()),
            avoidRepellent(),
            babySometimesRideBabyHoglin(),
            createIdleLookBehaviors(),
            createIdleMovementBehaviors(),
            SetLookAndInteract.create(net.minecraft.world.entity.EntityType.PLAYER, 4)
         )
      );
   }

   private static void initFightActivity(Piglin $$0, Brain<Piglin> $$1) {
      $$1.addActivityAndRemoveMemoryWhenStopped(
         Activity.FIGHT,
         10,
         ImmutableList.of(
            StopAttackingIfTargetInvalid.create(($$1x, $$2) -> !isNearestValidAttackTarget($$1x, $$0, $$2)),
            BehaviorBuilder.triggerIf(PiglinAi::hasCrossbow, BackUpIfTooClose.create(5, 0.75F)),
            SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
            new SpearApproach(1.0, 10.0F),
            new SpearAttack(1.0, 1.0, 10.0F, 2.0F),
            new SpearRetreat(1.0),
            MeleeAttack.create(20),
            new CrossbowAttack(),
            RememberIfHoglinWasKilled.create(),
            EraseMemoryIf.create(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)
         ),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private static void initCelebrateActivity(Brain<Piglin> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.CELEBRATE,
         10,
         ImmutableList.of(
            avoidRepellent(),
            SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F),
            StartAttacking.create(($$0x, $$1) -> $$1.isAdult(), PiglinAi::findNearestValidAttackTarget),
            BehaviorBuilder.triggerIf($$0x -> !$$0x.isDancing(), GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0F)),
            BehaviorBuilder.triggerIf(Piglin::isDancing, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6F)),
            new RunOne(
               ImmutableList.of(
                  Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PIGLIN, 8.0F), 1),
                  Pair.of(RandomStroll.stroll(0.6F, 2, 1), 1),
                  Pair.of(new DoNothing(10, 20), 1)
               )
            )
         ),
         MemoryModuleType.CELEBRATE_LOCATION
      );
   }

   private static void initAdmireItemActivity(Brain<Piglin> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.ADMIRE_ITEM,
         10,
         ImmutableList.of(
            GoToWantedItem.create(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0F, true, 9),
            StopAdmiringIfItemTooFarAway.create(9),
            StopAdmiringIfTiredOfTryingToReachItem.create(200, 200)
         ),
         MemoryModuleType.ADMIRING_ITEM
      );
   }

   private static void initRetreatActivity(Brain<Piglin> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.AVOID,
         10,
         ImmutableList.of(
            SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true),
            createIdleLookBehaviors(),
            createIdleMovementBehaviors(),
            EraseMemoryIf.create(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
         ),
         MemoryModuleType.AVOID_TARGET
      );
   }

   private static void initRideHoglinActivity(Brain<Piglin> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.RIDE,
         10,
         ImmutableList.of(
            Mount.create(0.8F),
            SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 8.0F),
            BehaviorBuilder.sequence(
               BehaviorBuilder.triggerIf(net.minecraft.world.entity.Entity::isPassenger),
               TriggerGate.triggerOneShuffled(
                  ImmutableList.builder().addAll(createLookBehaviors()).add(Pair.of(BehaviorBuilder.triggerIf($$0x -> true), 1)).build()
               )
            ),
            DismountOrSkipMounting.create(8, PiglinAi::wantsToStopRiding)
         ),
         MemoryModuleType.RIDE_TARGET
      );
   }

   private static ImmutableList<Pair<OneShot<net.minecraft.world.entity.LivingEntity>, Integer>> createLookBehaviors() {
      return ImmutableList.of(
         Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PLAYER, 8.0F), 1),
         Pair.of(SetEntityLookTarget.create(net.minecraft.world.entity.EntityType.PIGLIN, 8.0F), 1),
         Pair.of(SetEntityLookTarget.create(8.0F), 1)
      );
   }

   private static RunOne<net.minecraft.world.entity.LivingEntity> createIdleLookBehaviors() {
      return new RunOne<>(ImmutableList.builder().addAll(createLookBehaviors()).add(Pair.of(new DoNothing(30, 60), 1)).build());
   }

   private static RunOne<Piglin> createIdleMovementBehaviors() {
      return new RunOne<>(
         ImmutableList.of(
            Pair.of(RandomStroll.stroll(0.6F), 2),
            Pair.of(InteractWith.of(net.minecraft.world.entity.EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2),
            Pair.of(BehaviorBuilder.triggerIf(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6F, 3)), 2),
            Pair.of(new DoNothing(30, 60), 1)
         )
      );
   }

   private static BehaviorControl<net.minecraft.world.entity.PathfinderMob> avoidRepellent() {
      return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
   }

   private static BehaviorControl<Piglin> babyAvoidNemesis() {
      return CopyMemoryWithExpiry.create(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
   }

   private static BehaviorControl<Piglin> avoidZombified() {
      return CopyMemoryWithExpiry.create(
         PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION
      );
   }

   protected static void updateActivity(Piglin $$0) {
      Brain<Piglin> $$1 = $$0.getBrain();
      Activity $$2 = $$1.getActiveNonCoreActivity().orElse(null);
      $$1.setActiveActivityToFirstValid(
         ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE)
      );
      Activity $$3 = $$1.getActiveNonCoreActivity().orElse(null);
      if ($$2 != $$3) {
         getSoundForCurrentActivity($$0).ifPresent($$0::makeSound);
      }

      $$0.setAggressive($$1.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
      if (!$$1.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby($$0)) {
         $$0.stopRiding();
      }

      if (!$$1.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
         $$1.eraseMemory(MemoryModuleType.DANCING);
      }

      $$0.setDancing($$1.hasMemoryValue(MemoryModuleType.DANCING));
   }

   private static boolean isBabyRidingBaby(Piglin $$0) {
      if (!$$0.isBaby()) {
         return false;
      } else {
         net.minecraft.world.entity.Entity $$1 = $$0.getVehicle();
         return $$1 instanceof Piglin && ((Piglin)$$1).isBaby() || $$1 instanceof Hoglin && ((Hoglin)$$1).isBaby();
      }
   }

   protected static void pickUpItem(ServerLevel $$0, Piglin $$1, ItemEntity $$2) {
      stopWalking($$1);
      ItemStack $$3;
      if ($$2.getItem().is(Items.GOLD_NUGGET)) {
         $$1.take($$2, $$2.getItem().getCount());
         $$3 = $$2.getItem();
         $$2.discard();
      } else {
         $$1.take($$2, 1);
         $$3 = removeOneItemFromItemEntity($$2);
      }

      if (isLovedItem($$3)) {
         $$1.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
         holdInOffhand($$0, $$1, $$3);
         admireGoldItem($$1);
      } else if (isFood($$3) && !hasEatenRecently($$1)) {
         eat($$1);
      } else {
         boolean $$5 = !$$1.equipItemIfPossible($$0, $$3).equals(ItemStack.EMPTY);
         if (!$$5) {
            putInInventory($$1, $$3);
         }
      }
   }

   private static void holdInOffhand(ServerLevel $$0, Piglin $$1, ItemStack $$2) {
      if (isHoldingItemInOffHand($$1)) {
         $$1.spawnAtLocation($$0, $$1.getItemInHand(InteractionHand.OFF_HAND));
      }

      $$1.holdInOffHand($$2);
   }

   private static ItemStack removeOneItemFromItemEntity(ItemEntity $$0) {
      ItemStack $$1 = $$0.getItem();
      ItemStack $$2 = $$1.split(1);
      if ($$1.isEmpty()) {
         $$0.discard();
      } else {
         $$0.setItem($$1);
      }

      return $$2;
   }

   protected static void stopHoldingOffHandItem(ServerLevel $$0, Piglin $$1, boolean $$2) {
      ItemStack $$3 = $$1.getItemInHand(InteractionHand.OFF_HAND);
      $$1.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
      if ($$1.isAdult()) {
         boolean $$4 = isBarterCurrency($$3);
         if ($$2 && $$4) {
            throwItems($$1, getBarterResponseItems($$1));
         } else if (!$$4) {
            boolean $$5 = !$$1.equipItemIfPossible($$0, $$3).isEmpty();
            if (!$$5) {
               putInInventory($$1, $$3);
            }
         }
      } else {
         boolean $$6 = !$$1.equipItemIfPossible($$0, $$3).isEmpty();
         if (!$$6) {
            ItemStack $$7 = $$1.getMainHandItem();
            if (isLovedItem($$7)) {
               putInInventory($$1, $$7);
            } else {
               throwItems($$1, Collections.singletonList($$7));
            }

            $$1.holdInMainHand($$3);
         }
      }
   }

   protected static void cancelAdmiring(ServerLevel $$0, Piglin $$1) {
      if (isAdmiringItem($$1) && !$$1.getOffhandItem().isEmpty()) {
         $$1.spawnAtLocation($$0, $$1.getOffhandItem());
         $$1.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
      }
   }

   private static void putInInventory(Piglin $$0, ItemStack $$1) {
      ItemStack $$2 = $$0.addToInventory($$1);
      throwItemsTowardRandomPos($$0, Collections.singletonList($$2));
   }

   private static void throwItems(Piglin $$0, List<ItemStack> $$1) {
      Optional<Player> $$2 = $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
      if ($$2.isPresent()) {
         throwItemsTowardPlayer($$0, $$2.get(), $$1);
      } else {
         throwItemsTowardRandomPos($$0, $$1);
      }
   }

   private static void throwItemsTowardRandomPos(Piglin $$0, List<ItemStack> $$1) {
      throwItemsTowardPos($$0, $$1, getRandomNearbyPos($$0));
   }

   private static void throwItemsTowardPlayer(Piglin $$0, Player $$1, List<ItemStack> $$2) {
      throwItemsTowardPos($$0, $$2, $$1.position());
   }

   private static void throwItemsTowardPos(Piglin $$0, List<ItemStack> $$1, Vec3 $$2) {
      if (!$$1.isEmpty()) {
         $$0.swing(InteractionHand.OFF_HAND);

         for (ItemStack $$3 : $$1) {
            BehaviorUtils.throwItem($$0, $$3, $$2.add(0.0, 1.0, 0.0));
         }
      }
   }

   private static List<ItemStack> getBarterResponseItems(Piglin $$0) {
      LootTable $$1 = $$0.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
      List<ItemStack> $$2 = $$1.getRandomItems(
         new Builder((ServerLevel)$$0.level()).withParameter(LootContextParams.THIS_ENTITY, $$0).create(LootContextParamSets.PIGLIN_BARTER)
      );
      return $$2;
   }

   private static boolean wantsToDance(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return $$1.getType() != net.minecraft.world.entity.EntityType.HOGLIN ? false : RandomSource.create($$0.level().getGameTime()).nextFloat() < 0.1F;
   }

   protected static boolean wantsToPickup(Piglin $$0, ItemStack $$1) {
      if ($$0.isBaby() && $$1.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
         return false;
      } else if ($$1.is(ItemTags.PIGLIN_REPELLENTS)) {
         return false;
      } else if (isAdmiringDisabled($$0) && $$0.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
         return false;
      } else if (isBarterCurrency($$1)) {
         return isNotHoldingLovedItemInOffHand($$0);
      } else {
         boolean $$2 = $$0.canAddToInventory($$1);
         if ($$1.is(Items.GOLD_NUGGET)) {
            return $$2;
         } else if (isFood($$1)) {
            return !hasEatenRecently($$0) && $$2;
         } else {
            return !isLovedItem($$1) ? $$0.canReplaceCurrentItem($$1) : isNotHoldingLovedItemInOffHand($$0) && $$2;
         }
      }
   }

   protected static boolean isLovedItem(ItemStack $$0) {
      return $$0.is(ItemTags.PIGLIN_LOVED);
   }

   private static boolean wantsToStopRiding(Piglin $$0, net.minecraft.world.entity.Entity $$1) {
      return !($$1 instanceof net.minecraft.world.entity.Mob $$2)
         ? false
         : !$$2.isBaby() || !$$2.isAlive() || wasHurtRecently($$0) || wasHurtRecently($$2) || $$2 instanceof Piglin && $$2.getVehicle() == null;
   }

   private static boolean isNearestValidAttackTarget(ServerLevel $$0, Piglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      return findNearestValidAttackTarget($$0, $$1).filter($$1x -> $$1x == $$2).isPresent();
   }

   private static boolean isNearZombified(Piglin $$0) {
      Brain<Piglin> $$1 = $$0.getBrain();
      if ($$1.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
         net.minecraft.world.entity.LivingEntity $$2 = $$1.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
         return $$0.closerThan($$2, 6.0);
      } else {
         return false;
      }
   }

   private static Optional<? extends net.minecraft.world.entity.LivingEntity> findNearestValidAttackTarget(ServerLevel $$0, Piglin $$1) {
      Brain<Piglin> $$2 = $$1.getBrain();
      if (isNearZombified($$1)) {
         return Optional.empty();
      } else {
         Optional<net.minecraft.world.entity.LivingEntity> $$3 = BehaviorUtils.getLivingEntityFromUUIDMemory($$1, MemoryModuleType.ANGRY_AT);
         if ($$3.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$3.get())) {
            return $$3;
         } else {
            if ($$2.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
               Optional<Player> $$4 = $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
               if ($$4.isPresent()) {
                  return $$4;
               }
            }

            Optional<net.minecraft.world.entity.Mob> $$5 = $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
            if ($$5.isPresent()) {
               return $$5;
            } else {
               Optional<Player> $$6 = $$2.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
               return $$6.isPresent() && Sensor.isEntityAttackable($$0, $$1, $$6.get()) ? $$6 : Optional.empty();
            }
         }
      }
   }

   public static void angerNearbyPiglins(ServerLevel $$0, Player $$1, boolean $$2) {
      List<Piglin> $$3 = $$1.level().getEntitiesOfClass(Piglin.class, $$1.getBoundingBox().inflate(16.0));
      $$3.stream().filter(PiglinAi::isIdle).filter($$2x -> !$$2 || BehaviorUtils.canSee($$2x, $$1)).forEach($$2x -> {
         if ((Boolean)$$0.getGameRules().get(GameRules.UNIVERSAL_ANGER)) {
            setAngerTargetToNearestTargetablePlayerIfFound($$0, $$2x, $$1);
         } else {
            setAngerTarget($$0, $$2x, $$1);
         }
      });
   }

   public static InteractionResult mobInteract(ServerLevel $$0, Piglin $$1, Player $$2, InteractionHand $$3) {
      ItemStack $$4 = $$2.getItemInHand($$3);
      if (canAdmire($$1, $$4)) {
         ItemStack $$5 = $$4.consumeAndReturn(1, $$2);
         holdInOffhand($$0, $$1, $$5);
         admireGoldItem($$1);
         stopWalking($$1);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   protected static boolean canAdmire(Piglin $$0, ItemStack $$1) {
      return !isAdmiringDisabled($$0) && !isAdmiringItem($$0) && $$0.isAdult() && isBarterCurrency($$1);
   }

   protected static void wasHurtBy(ServerLevel $$0, Piglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      if (!($$2 instanceof Piglin)) {
         if (isHoldingItemInOffHand($$1)) {
            stopHoldingOffHandItem($$0, $$1, false);
         }

         Brain<Piglin> $$3 = $$1.getBrain();
         $$3.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
         $$3.eraseMemory(MemoryModuleType.DANCING);
         $$3.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
         if ($$2 instanceof Player) {
            $$3.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
         }

         getAvoidTarget($$1).ifPresent($$2x -> {
            if ($$2x.getType() != $$2.getType()) {
               $$3.eraseMemory(MemoryModuleType.AVOID_TARGET);
            }
         });
         if ($$1.isBaby()) {
            $$3.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, $$2, 100L);
            if (Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2)) {
               broadcastAngerTarget($$0, $$1, $$2);
            }
         } else if ($$2.getType() == net.minecraft.world.entity.EntityType.HOGLIN && hoglinsOutnumberPiglins($$1)) {
            setAvoidTargetAndDontHuntForAWhile($$1, $$2);
            broadcastRetreat($$1, $$2);
         } else {
            maybeRetaliate($$0, $$1, $$2);
         }
      }
   }

   protected static void maybeRetaliate(ServerLevel $$0, AbstractPiglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      if (!$$1.getBrain().isActive(Activity.AVOID)) {
         if (Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2)) {
            if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget($$1, $$2, 4.0)) {
               if ($$2.getType() == net.minecraft.world.entity.EntityType.PLAYER && (Boolean)$$0.getGameRules().get(GameRules.UNIVERSAL_ANGER)) {
                  setAngerTargetToNearestTargetablePlayerIfFound($$0, $$1, $$2);
                  broadcastUniversalAnger($$0, $$1);
               } else {
                  setAngerTarget($$0, $$1, $$2);
                  broadcastAngerTarget($$0, $$1, $$2);
               }
            }
         }
      }
   }

   public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin $$0) {
      return $$0.getBrain().getActiveNonCoreActivity().map($$1 -> getSoundForActivity($$0, $$1));
   }

   private static SoundEvent getSoundForActivity(Piglin $$0, Activity $$1) {
      if ($$1 == Activity.FIGHT) {
         return SoundEvents.PIGLIN_ANGRY;
      } else if ($$0.isConverting()) {
         return SoundEvents.PIGLIN_RETREAT;
      } else if ($$1 == Activity.AVOID && isNearAvoidTarget($$0)) {
         return SoundEvents.PIGLIN_RETREAT;
      } else if ($$1 == Activity.ADMIRE_ITEM) {
         return SoundEvents.PIGLIN_ADMIRING_ITEM;
      } else if ($$1 == Activity.CELEBRATE) {
         return SoundEvents.PIGLIN_CELEBRATE;
      } else if (seesPlayerHoldingLovedItem($$0)) {
         return SoundEvents.PIGLIN_JEALOUS;
      } else {
         return isNearRepellent($$0) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
      }
   }

   private static boolean isNearAvoidTarget(Piglin $$0) {
      Brain<Piglin> $$1 = $$0.getBrain();
      return !$$1.hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? false : $$1.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan($$0, 12.0);
   }

   protected static List<AbstractPiglin> getVisibleAdultPiglins(Piglin $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   public static boolean isWearingSafeArmor(net.minecraft.world.entity.LivingEntity $$0) {
      for (net.minecraft.world.entity.EquipmentSlot $$1 : net.minecraft.world.entity.EquipmentSlotGroup.ARMOR) {
         if ($$0.getItemBySlot($$1).is(ItemTags.PIGLIN_SAFE_ARMOR)) {
            return true;
         }
      }

      return false;
   }

   private static void stopWalking(Piglin $$0) {
      $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      $$0.getNavigation().stop();
   }

   private static BehaviorControl<net.minecraft.world.entity.LivingEntity> babySometimesRideBabyHoglin() {
      SetEntityLookTargetSometimes.Ticker $$0 = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
      return CopyMemoryWithExpiry.create(
         $$1 -> $$1.isBaby() && $$0.tickDownAndCheck($$1.level().random),
         MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
         MemoryModuleType.RIDE_TARGET,
         RIDE_DURATION
      );
   }

   protected static void broadcastAngerTarget(ServerLevel $$0, AbstractPiglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      getAdultPiglins($$1).forEach($$2x -> {
         if ($$2.getType() != net.minecraft.world.entity.EntityType.HOGLIN || $$2x.canHunt() && ((Hoglin)$$2).canBeHunted()) {
            setAngerTargetIfCloserThanCurrent($$0, $$2x, $$2);
         }
      });
   }

   protected static void broadcastUniversalAnger(ServerLevel $$0, AbstractPiglin $$1) {
      getAdultPiglins($$1).forEach($$1x -> getNearestVisibleTargetablePlayer($$1x).ifPresent($$2 -> setAngerTarget($$0, $$1x, $$2)));
   }

   protected static void setAngerTarget(ServerLevel $$0, AbstractPiglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      if (Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2)) {
         $$1.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, $$2.getUUID(), 600L);
         if ($$2.getType() == net.minecraft.world.entity.EntityType.HOGLIN && $$1.canHunt()) {
            dontKillAnyMoreHoglinsForAWhile($$1);
         }

         if ($$2.getType() == net.minecraft.world.entity.EntityType.PLAYER && (Boolean)$$0.getGameRules().get(GameRules.UNIVERSAL_ANGER)) {
            $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
         }
      }
   }

   private static void setAngerTargetToNearestTargetablePlayerIfFound(ServerLevel $$0, AbstractPiglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      Optional<Player> $$3 = getNearestVisibleTargetablePlayer($$1);
      if ($$3.isPresent()) {
         setAngerTarget($$0, $$1, $$3.get());
      } else {
         setAngerTarget($$0, $$1, $$2);
      }
   }

   private static void setAngerTargetIfCloserThanCurrent(ServerLevel $$0, AbstractPiglin $$1, net.minecraft.world.entity.LivingEntity $$2) {
      Optional<net.minecraft.world.entity.LivingEntity> $$3 = getAngerTarget($$1);
      net.minecraft.world.entity.LivingEntity $$4 = BehaviorUtils.getNearestTarget($$1, $$3, $$2);
      if (!$$3.isPresent() || $$3.get() != $$4) {
         setAngerTarget($$0, $$1, $$4);
      }
   }

   private static Optional<net.minecraft.world.entity.LivingEntity> getAngerTarget(AbstractPiglin $$0) {
      return BehaviorUtils.getLivingEntityFromUUIDMemory($$0, MemoryModuleType.ANGRY_AT);
   }

   public static Optional<net.minecraft.world.entity.LivingEntity> getAvoidTarget(Piglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? $$0.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
   }

   public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
         ? $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
         : Optional.empty();
   }

   private static void broadcastRetreat(Piglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      getVisibleAdultPiglins($$0).stream().filter($$0x -> $$0x instanceof Piglin).forEach($$1x -> retreatFromNearestTarget((Piglin)$$1x, $$1));
   }

   private static void retreatFromNearestTarget(Piglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Brain<Piglin> $$2 = $$0.getBrain();
      net.minecraft.world.entity.LivingEntity $$3 = BehaviorUtils.getNearestTarget($$0, $$2.getMemory(MemoryModuleType.AVOID_TARGET), $$1);
      $$3 = BehaviorUtils.getNearestTarget($$0, $$2.getMemory(MemoryModuleType.ATTACK_TARGET), $$3);
      setAvoidTargetAndDontHuntForAWhile($$0, $$3);
   }

   private static boolean wantsToStopFleeing(Piglin $$0) {
      Brain<Piglin> $$1 = $$0.getBrain();
      if (!$$1.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
         return true;
      } else {
         net.minecraft.world.entity.LivingEntity $$2 = $$1.getMemory(MemoryModuleType.AVOID_TARGET).get();
         net.minecraft.world.entity.EntityType<?> $$3 = $$2.getType();
         if ($$3 == net.minecraft.world.entity.EntityType.HOGLIN) {
            return piglinsEqualOrOutnumberHoglins($$0);
         } else {
            return isZombified($$3) ? !$$1.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, $$2) : false;
         }
      }
   }

   private static boolean piglinsEqualOrOutnumberHoglins(Piglin $$0) {
      return !hoglinsOutnumberPiglins($$0);
   }

   private static boolean hoglinsOutnumberPiglins(Piglin $$0) {
      int $$1 = $$0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
      int $$2 = $$0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
      return $$2 > $$1;
   }

   private static void setAvoidTargetAndDontHuntForAWhile(Piglin $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$0.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
      $$0.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, $$1, RETREAT_DURATION.sample($$0.level().random));
      dontKillAnyMoreHoglinsForAWhile($$0);
   }

   protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglin $$0) {
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, TIME_BETWEEN_HUNTS.sample($$0.level().random));
   }

   private static void eat(Piglin $$0) {
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
   }

   private static Vec3 getRandomNearbyPos(Piglin $$0) {
      Vec3 $$1 = LandRandomPos.getPos($$0, 4, 2);
      return $$1 == null ? $$0.position() : $$1;
   }

   private static boolean hasEatenRecently(Piglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
   }

   protected static boolean isIdle(AbstractPiglin $$0) {
      return $$0.getBrain().isActive(Activity.IDLE);
   }

   private static boolean hasCrossbow(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.isHolding(Items.CROSSBOW);
   }

   private static void admireGoldItem(net.minecraft.world.entity.LivingEntity $$0) {
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 119L);
   }

   private static boolean isAdmiringItem(Piglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
   }

   private static boolean isBarterCurrency(ItemStack $$0) {
      return $$0.is(BARTERING_ITEM);
   }

   private static boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.PIGLIN_FOOD);
   }

   private static boolean isNearRepellent(Piglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean seesPlayerHoldingLovedItem(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
   }

   private static boolean doesntSeeAnyPlayerHoldingLovedItem(net.minecraft.world.entity.LivingEntity $$0) {
      return !seesPlayerHoldingLovedItem($$0);
   }

   public static boolean isPlayerHoldingLovedItem(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getType() == net.minecraft.world.entity.EntityType.PLAYER && $$0.isHolding(PiglinAi::isLovedItem);
   }

   private static boolean isAdmiringDisabled(Piglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
   }

   private static boolean wasHurtRecently(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
   }

   private static boolean isHoldingItemInOffHand(Piglin $$0) {
      return !$$0.getOffhandItem().isEmpty();
   }

   private static boolean isNotHoldingLovedItemInOffHand(Piglin $$0) {
      return $$0.getOffhandItem().isEmpty() || !isLovedItem($$0.getOffhandItem());
   }

   public static boolean isZombified(net.minecraft.world.entity.EntityType<?> $$0) {
      return $$0 == net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN || $$0 == net.minecraft.world.entity.EntityType.ZOGLIN;
   }
}
