/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
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
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import net.minecraft.world.entity.monster.piglin.StartAdmiringItemIfSeen;
import net.minecraft.world.entity.monster.piglin.StartHuntingHoglin;
import net.minecraft.world.entity.monster.piglin.StopAdmiringIfItemTooFarAway;
import net.minecraft.world.entity.monster.piglin.StopAdmiringIfTiredOfTryingToReachItem;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
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
    private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75f;
    private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
    private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1f;
    private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8f;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6f;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6f;

    protected static Brain<?> makeBrain(Piglin $$0, Brain<Piglin> $$1) {
        PiglinAi.initCoreActivity($$1);
        PiglinAi.initIdleActivity($$1);
        PiglinAi.initAdmireItemActivity($$1);
        PiglinAi.initFightActivity($$0, $$1);
        PiglinAi.initCelebrateActivity($$1);
        PiglinAi.initRetreatActivity($$1);
        PiglinAi.initRideHoglinActivity($$1);
        $$1.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$1.setDefaultActivity(Activity.IDLE);
        $$1.useDefaultActivity();
        return $$1;
    }

    protected static void initMemories(Piglin $$0, RandomSource $$1) {
        int $$2 = TIME_BETWEEN_HUNTS.sample($$1);
        $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, $$2);
    }

    private static void initCoreActivity(Brain<Piglin> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of((Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), InteractWithDoor.create(), PiglinAi.babyAvoidNemesis(), PiglinAi.avoidZombified(), StopHoldingItemIfNoLongerAdmiring.create(), StartAdmiringItemIfSeen.create(119), StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance), StopBeingAngryIfTargetDead.create()));
    }

    private static void initIdleActivity(Brain<Piglin> $$02) {
        $$02.addActivity(Activity.IDLE, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0f), StartAttacking.create(($$0, $$1) -> $$1.isAdult(), PiglinAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()), PiglinAi.avoidRepellent(), PiglinAi.babySometimesRideBabyHoglin(), PiglinAi.createIdleLookBehaviors(), PiglinAi.createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4)));
    }

    private static void initFightActivity(Piglin $$0, Brain<Piglin> $$12) {
        $$12.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(StopAttackingIfTargetInvalid.create(($$1, $$2) -> !PiglinAi.isNearestValidAttackTarget($$1, $$0, $$2)), BehaviorBuilder.triggerIf(PiglinAi::hasCrossbow, BackUpIfTooClose.create(5, 0.75f)), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0f), (Object)new SpearApproach(1.0, 10.0f), (Object)new SpearAttack(1.0, 1.0, 10.0f, 2.0f), (Object)new SpearRetreat(1.0), MeleeAttack.create(20), new CrossbowAttack(), RememberIfHoglinWasKilled.create(), EraseMemoryIf.create(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCelebrateActivity(Brain<Piglin> $$02) {
        $$02.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(PiglinAi.avoidRepellent(), SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0f), StartAttacking.create(($$0, $$1) -> $$1.isAdult(), PiglinAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf($$0 -> !$$0.isDancing(), GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0f)), BehaviorBuilder.triggerIf(Piglin::isDancing, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6f)), new RunOne(ImmutableList.of((Object)Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0f), (Object)1), (Object)Pair.of(RandomStroll.stroll(0.6f, 2, 1), (Object)1), (Object)Pair.of((Object)new DoNothing(10, 20), (Object)1)))), MemoryModuleType.CELEBRATE_LOCATION);
    }

    private static void initAdmireItemActivity(Brain<Piglin> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(GoToWantedItem.create(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0f, true, 9), StopAdmiringIfItemTooFarAway.create(9), StopAdmiringIfTiredOfTryingToReachItem.create(200, 200)), MemoryModuleType.ADMIRING_ITEM);
    }

    private static void initRetreatActivity(Brain<Piglin> $$0) {
        $$0.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0f, 12, true), PiglinAi.createIdleLookBehaviors(), PiglinAi.createIdleMovementBehaviors(), EraseMemoryIf.create(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
    }

    private static void initRideHoglinActivity(Brain<Piglin> $$02) {
        $$02.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, (ImmutableList<BehaviorControl<Piglin>>)ImmutableList.of(Mount.create(0.8f), SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 8.0f), BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(Entity::isPassenger), TriggerGate.triggerOneShuffled(ImmutableList.builder().addAll(PiglinAi.createLookBehaviors()).add((Object)Pair.of(BehaviorBuilder.triggerIf($$0 -> true), (Object)1)).build())), DismountOrSkipMounting.create(8, PiglinAi::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
    }

    private static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
        return ImmutableList.of((Object)Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(8.0f), (Object)1));
    }

    private static RunOne<LivingEntity> createIdleLookBehaviors() {
        return new RunOne<LivingEntity>((List<Pair<BehaviorControl<LivingEntity>, Integer>>)ImmutableList.builder().addAll(PiglinAi.createLookBehaviors()).add((Object)Pair.of((Object)new DoNothing(30, 60), (Object)1)).build());
    }

    private static RunOne<Piglin> createIdleMovementBehaviors() {
        return new RunOne<Piglin>((List<Pair<BehaviorControl<Piglin>, Integer>>)ImmutableList.of((Object)Pair.of(RandomStroll.stroll(0.6f), (Object)2), (Object)Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6f, 2), (Object)2), (Object)Pair.of(BehaviorBuilder.triggerIf(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6f, 3)), (Object)2), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1)));
    }

    private static BehaviorControl<PathfinderMob> avoidRepellent() {
        return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0f, 8, false);
    }

    private static BehaviorControl<Piglin> babyAvoidNemesis() {
        return CopyMemoryWithExpiry.create(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
    }

    private static BehaviorControl<Piglin> avoidZombified() {
        return CopyMemoryWithExpiry.create(PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
    }

    protected static void updateActivity(Piglin $$0) {
        Brain<Piglin> $$1 = $$0.getBrain();
        Activity $$2 = $$1.getActiveNonCoreActivity().orElse(null);
        $$1.setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.ADMIRE_ITEM, (Object)Activity.FIGHT, (Object)Activity.AVOID, (Object)Activity.CELEBRATE, (Object)Activity.RIDE, (Object)Activity.IDLE));
        Activity $$3 = $$1.getActiveNonCoreActivity().orElse(null);
        if ($$2 != $$3) {
            PiglinAi.getSoundForCurrentActivity($$0).ifPresent($$0::makeSound);
        }
        $$0.setAggressive($$1.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        if (!$$1.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && PiglinAi.isBabyRidingBaby($$0)) {
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
        }
        Entity $$1 = $$0.getVehicle();
        return $$1 instanceof Piglin && ((Piglin)$$1).isBaby() || $$1 instanceof Hoglin && ((Hoglin)$$1).isBaby();
    }

    protected static void pickUpItem(ServerLevel $$0, Piglin $$1, ItemEntity $$2) {
        boolean $$5;
        ItemStack $$4;
        PiglinAi.stopWalking($$1);
        if ($$2.getItem().is(Items.GOLD_NUGGET)) {
            $$1.take($$2, $$2.getItem().getCount());
            ItemStack $$3 = $$2.getItem();
            $$2.discard();
        } else {
            $$1.take($$2, 1);
            $$4 = PiglinAi.removeOneItemFromItemEntity($$2);
        }
        if (PiglinAi.isLovedItem($$4)) {
            $$1.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
            PiglinAi.holdInOffhand($$0, $$1, $$4);
            PiglinAi.admireGoldItem($$1);
            return;
        }
        if (PiglinAi.isFood($$4) && !PiglinAi.hasEatenRecently($$1)) {
            PiglinAi.eat($$1);
            return;
        }
        boolean bl = $$5 = !$$1.equipItemIfPossible($$0, $$4).equals(ItemStack.EMPTY);
        if ($$5) {
            return;
        }
        PiglinAi.putInInventory($$1, $$4);
    }

    private static void holdInOffhand(ServerLevel $$0, Piglin $$1, ItemStack $$2) {
        if (PiglinAi.isHoldingItemInOffHand($$1)) {
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
            boolean $$4 = PiglinAi.isBarterCurrency($$3);
            if ($$2 && $$4) {
                PiglinAi.throwItems($$1, PiglinAi.getBarterResponseItems($$1));
            } else if (!$$4) {
                boolean $$5;
                boolean bl = $$5 = !$$1.equipItemIfPossible($$0, $$3).isEmpty();
                if (!$$5) {
                    PiglinAi.putInInventory($$1, $$3);
                }
            }
        } else {
            boolean $$6;
            boolean bl = $$6 = !$$1.equipItemIfPossible($$0, $$3).isEmpty();
            if (!$$6) {
                ItemStack $$7 = $$1.getMainHandItem();
                if (PiglinAi.isLovedItem($$7)) {
                    PiglinAi.putInInventory($$1, $$7);
                } else {
                    PiglinAi.throwItems($$1, Collections.singletonList($$7));
                }
                $$1.holdInMainHand($$3);
            }
        }
    }

    protected static void cancelAdmiring(ServerLevel $$0, Piglin $$1) {
        if (PiglinAi.isAdmiringItem($$1) && !$$1.getOffhandItem().isEmpty()) {
            $$1.spawnAtLocation($$0, $$1.getOffhandItem());
            $$1.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    private static void putInInventory(Piglin $$0, ItemStack $$1) {
        ItemStack $$2 = $$0.addToInventory($$1);
        PiglinAi.throwItemsTowardRandomPos($$0, Collections.singletonList($$2));
    }

    private static void throwItems(Piglin $$0, List<ItemStack> $$1) {
        Optional<Player> $$2 = $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if ($$2.isPresent()) {
            PiglinAi.throwItemsTowardPlayer($$0, $$2.get(), $$1);
        } else {
            PiglinAi.throwItemsTowardRandomPos($$0, $$1);
        }
    }

    private static void throwItemsTowardRandomPos(Piglin $$0, List<ItemStack> $$1) {
        PiglinAi.throwItemsTowardPos($$0, $$1, PiglinAi.getRandomNearbyPos($$0));
    }

    private static void throwItemsTowardPlayer(Piglin $$0, Player $$1, List<ItemStack> $$2) {
        PiglinAi.throwItemsTowardPos($$0, $$2, $$1.position());
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
        ObjectArrayList<ItemStack> $$2 = $$1.getRandomItems(new LootParams.Builder((ServerLevel)$$0.level()).withParameter(LootContextParams.THIS_ENTITY, $$0).create(LootContextParamSets.PIGLIN_BARTER));
        return $$2;
    }

    private static boolean wantsToDance(LivingEntity $$0, LivingEntity $$1) {
        if ($$1.getType() != EntityType.HOGLIN) {
            return false;
        }
        return RandomSource.create($$0.level().getGameTime()).nextFloat() < 0.1f;
    }

    protected static boolean wantsToPickup(Piglin $$0, ItemStack $$1) {
        if ($$0.isBaby() && $$1.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
            return false;
        }
        if ($$1.is(ItemTags.PIGLIN_REPELLENTS)) {
            return false;
        }
        if (PiglinAi.isAdmiringDisabled($$0) && $$0.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return false;
        }
        if (PiglinAi.isBarterCurrency($$1)) {
            return PiglinAi.isNotHoldingLovedItemInOffHand($$0);
        }
        boolean $$2 = $$0.canAddToInventory($$1);
        if ($$1.is(Items.GOLD_NUGGET)) {
            return $$2;
        }
        if (PiglinAi.isFood($$1)) {
            return !PiglinAi.hasEatenRecently($$0) && $$2;
        }
        if (PiglinAi.isLovedItem($$1)) {
            return PiglinAi.isNotHoldingLovedItemInOffHand($$0) && $$2;
        }
        return $$0.canReplaceCurrentItem($$1);
    }

    protected static boolean isLovedItem(ItemStack $$0) {
        return $$0.is(ItemTags.PIGLIN_LOVED);
    }

    private static boolean wantsToStopRiding(Piglin $$0, Entity $$1) {
        if ($$1 instanceof Mob) {
            Mob $$2 = (Mob)$$1;
            return !$$2.isBaby() || !$$2.isAlive() || PiglinAi.wasHurtRecently($$0) || PiglinAi.wasHurtRecently($$2) || $$2 instanceof Piglin && $$2.getVehicle() == null;
        }
        return false;
    }

    private static boolean isNearestValidAttackTarget(ServerLevel $$0, Piglin $$12, LivingEntity $$2) {
        return PiglinAi.findNearestValidAttackTarget($$0, $$12).filter($$1 -> $$1 == $$2).isPresent();
    }

    private static boolean isNearZombified(Piglin $$0) {
        Brain<Piglin> $$1 = $$0.getBrain();
        if ($$1.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
            LivingEntity $$2 = $$1.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
            return $$0.closerThan($$2, 6.0);
        }
        return false;
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel $$0, Piglin $$1) {
        Optional<Player> $$4;
        Brain<Piglin> $$2 = $$1.getBrain();
        if (PiglinAi.isNearZombified($$1)) {
            return Optional.empty();
        }
        Optional<LivingEntity> $$3 = BehaviorUtils.getLivingEntityFromUUIDMemory($$1, MemoryModuleType.ANGRY_AT);
        if ($$3.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$3.get())) {
            return $$3;
        }
        if ($$2.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER) && ($$4 = $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)).isPresent()) {
            return $$4;
        }
        Optional<Mob> $$5 = $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
        if ($$5.isPresent()) {
            return $$5;
        }
        Optional<Player> $$6 = $$2.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
        if ($$6.isPresent() && Sensor.isEntityAttackable($$0, $$1, $$6.get())) {
            return $$6;
        }
        return Optional.empty();
    }

    public static void angerNearbyPiglins(ServerLevel $$0, Player $$1, boolean $$22) {
        List<Piglin> $$3 = $$1.level().getEntitiesOfClass(Piglin.class, $$1.getBoundingBox().inflate(16.0));
        $$3.stream().filter(PiglinAi::isIdle).filter($$2 -> !$$22 || BehaviorUtils.canSee($$2, $$1)).forEach($$2 -> {
            if ($$0.getGameRules().get(GameRules.UNIVERSAL_ANGER).booleanValue()) {
                PiglinAi.setAngerTargetToNearestTargetablePlayerIfFound($$0, $$2, $$1);
            } else {
                PiglinAi.setAngerTarget($$0, $$2, $$1);
            }
        });
    }

    public static InteractionResult mobInteract(ServerLevel $$0, Piglin $$1, Player $$2, InteractionHand $$3) {
        ItemStack $$4 = $$2.getItemInHand($$3);
        if (PiglinAi.canAdmire($$1, $$4)) {
            ItemStack $$5 = $$4.consumeAndReturn(1, $$2);
            PiglinAi.holdInOffhand($$0, $$1, $$5);
            PiglinAi.admireGoldItem($$1);
            PiglinAi.stopWalking($$1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected static boolean canAdmire(Piglin $$0, ItemStack $$1) {
        return !PiglinAi.isAdmiringDisabled($$0) && !PiglinAi.isAdmiringItem($$0) && $$0.isAdult() && PiglinAi.isBarterCurrency($$1);
    }

    protected static void wasHurtBy(ServerLevel $$0, Piglin $$1, LivingEntity $$22) {
        if ($$22 instanceof Piglin) {
            return;
        }
        if (PiglinAi.isHoldingItemInOffHand($$1)) {
            PiglinAi.stopHoldingOffHandItem($$0, $$1, false);
        }
        Brain<Piglin> $$3 = $$1.getBrain();
        $$3.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
        $$3.eraseMemory(MemoryModuleType.DANCING);
        $$3.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
        if ($$22 instanceof Player) {
            $$3.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
        }
        PiglinAi.getAvoidTarget($$1).ifPresent($$2 -> {
            if ($$2.getType() != $$22.getType()) {
                $$3.eraseMemory(MemoryModuleType.AVOID_TARGET);
            }
        });
        if ($$1.isBaby()) {
            $$3.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, $$22, 100L);
            if (Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$22)) {
                PiglinAi.broadcastAngerTarget($$0, $$1, $$22);
            }
            return;
        }
        if ($$22.getType() == EntityType.HOGLIN && PiglinAi.hoglinsOutnumberPiglins($$1)) {
            PiglinAi.setAvoidTargetAndDontHuntForAWhile($$1, $$22);
            PiglinAi.broadcastRetreat($$1, $$22);
            return;
        }
        PiglinAi.maybeRetaliate($$0, $$1, $$22);
    }

    protected static void maybeRetaliate(ServerLevel $$0, AbstractPiglin $$1, LivingEntity $$2) {
        if ($$1.getBrain().isActive(Activity.AVOID)) {
            return;
        }
        if (!Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2)) {
            return;
        }
        if (BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget($$1, $$2, 4.0)) {
            return;
        }
        if ($$2.getType() == EntityType.PLAYER && $$0.getGameRules().get(GameRules.UNIVERSAL_ANGER).booleanValue()) {
            PiglinAi.setAngerTargetToNearestTargetablePlayerIfFound($$0, $$1, $$2);
            PiglinAi.broadcastUniversalAnger($$0, $$1);
        } else {
            PiglinAi.setAngerTarget($$0, $$1, $$2);
            PiglinAi.broadcastAngerTarget($$0, $$1, $$2);
        }
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin $$0) {
        return $$0.getBrain().getActiveNonCoreActivity().map($$1 -> PiglinAi.getSoundForActivity($$0, $$1));
    }

    private static SoundEvent getSoundForActivity(Piglin $$0, Activity $$1) {
        if ($$1 == Activity.FIGHT) {
            return SoundEvents.PIGLIN_ANGRY;
        }
        if ($$0.isConverting()) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        if ($$1 == Activity.AVOID && PiglinAi.isNearAvoidTarget($$0)) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        if ($$1 == Activity.ADMIRE_ITEM) {
            return SoundEvents.PIGLIN_ADMIRING_ITEM;
        }
        if ($$1 == Activity.CELEBRATE) {
            return SoundEvents.PIGLIN_CELEBRATE;
        }
        if (PiglinAi.seesPlayerHoldingLovedItem($$0)) {
            return SoundEvents.PIGLIN_JEALOUS;
        }
        if (PiglinAi.isNearRepellent($$0)) {
            return SoundEvents.PIGLIN_RETREAT;
        }
        return SoundEvents.PIGLIN_AMBIENT;
    }

    private static boolean isNearAvoidTarget(Piglin $$0) {
        Brain<Piglin> $$1 = $$0.getBrain();
        if (!$$1.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return false;
        }
        return $$1.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan($$0, 12.0);
    }

    protected static List<AbstractPiglin> getVisibleAdultPiglins(Piglin $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse((List<AbstractPiglin>)ImmutableList.of());
    }

    private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse((List<AbstractPiglin>)ImmutableList.of());
    }

    public static boolean isWearingSafeArmor(LivingEntity $$0) {
        for (EquipmentSlot $$1 : EquipmentSlotGroup.ARMOR) {
            if (!$$0.getItemBySlot($$1).is(ItemTags.PIGLIN_SAFE_ARMOR)) continue;
            return true;
        }
        return false;
    }

    private static void stopWalking(Piglin $$0) {
        $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        $$0.getNavigation().stop();
    }

    private static BehaviorControl<LivingEntity> babySometimesRideBabyHoglin() {
        SetEntityLookTargetSometimes.Ticker $$0 = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
        return CopyMemoryWithExpiry.create($$1 -> $$1.isBaby() && $$0.tickDownAndCheck($$1.level().random), MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION);
    }

    protected static void broadcastAngerTarget(ServerLevel $$0, AbstractPiglin $$1, LivingEntity $$22) {
        PiglinAi.getAdultPiglins($$1).forEach($$2 -> {
            if (!($$22.getType() != EntityType.HOGLIN || $$2.canHunt() && ((Hoglin)$$22).canBeHunted())) {
                return;
            }
            PiglinAi.setAngerTargetIfCloserThanCurrent($$0, $$2, $$22);
        });
    }

    protected static void broadcastUniversalAnger(ServerLevel $$0, AbstractPiglin $$12) {
        PiglinAi.getAdultPiglins($$12).forEach($$1 -> PiglinAi.getNearestVisibleTargetablePlayer($$1).ifPresent($$2 -> PiglinAi.setAngerTarget($$0, $$1, $$2)));
    }

    protected static void setAngerTarget(ServerLevel $$0, AbstractPiglin $$1, LivingEntity $$2) {
        if (!Sensor.isEntityAttackableIgnoringLineOfSight($$0, $$1, $$2)) {
            return;
        }
        $$1.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, $$2.getUUID(), 600L);
        if ($$2.getType() == EntityType.HOGLIN && $$1.canHunt()) {
            PiglinAi.dontKillAnyMoreHoglinsForAWhile($$1);
        }
        if ($$2.getType() == EntityType.PLAYER && $$0.getGameRules().get(GameRules.UNIVERSAL_ANGER).booleanValue()) {
            $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(ServerLevel $$0, AbstractPiglin $$1, LivingEntity $$2) {
        Optional<Player> $$3 = PiglinAi.getNearestVisibleTargetablePlayer($$1);
        if ($$3.isPresent()) {
            PiglinAi.setAngerTarget($$0, $$1, $$3.get());
        } else {
            PiglinAi.setAngerTarget($$0, $$1, $$2);
        }
    }

    private static void setAngerTargetIfCloserThanCurrent(ServerLevel $$0, AbstractPiglin $$1, LivingEntity $$2) {
        Optional<LivingEntity> $$3 = PiglinAi.getAngerTarget($$1);
        LivingEntity $$4 = BehaviorUtils.getNearestTarget($$1, $$3, $$2);
        if ($$3.isPresent() && $$3.get() == $$4) {
            return;
        }
        PiglinAi.setAngerTarget($$0, $$1, $$4);
    }

    private static Optional<LivingEntity> getAngerTarget(AbstractPiglin $$0) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory($$0, MemoryModuleType.ANGRY_AT);
    }

    public static Optional<LivingEntity> getAvoidTarget(Piglin $$0) {
        if ($$0.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return $$0.getBrain().getMemory(MemoryModuleType.AVOID_TARGET);
        }
        return Optional.empty();
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin $$0) {
        if ($$0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)) {
            return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
        }
        return Optional.empty();
    }

    private static void broadcastRetreat(Piglin $$02, LivingEntity $$12) {
        PiglinAi.getVisibleAdultPiglins($$02).stream().filter($$0 -> $$0 instanceof Piglin).forEach($$1 -> PiglinAi.retreatFromNearestTarget((Piglin)$$1, $$12));
    }

    private static void retreatFromNearestTarget(Piglin $$0, LivingEntity $$1) {
        Brain<Piglin> $$2 = $$0.getBrain();
        LivingEntity $$3 = $$1;
        $$3 = BehaviorUtils.getNearestTarget($$0, $$2.getMemory(MemoryModuleType.AVOID_TARGET), $$3);
        $$3 = BehaviorUtils.getNearestTarget($$0, $$2.getMemory(MemoryModuleType.ATTACK_TARGET), $$3);
        PiglinAi.setAvoidTargetAndDontHuntForAWhile($$0, $$3);
    }

    private static boolean wantsToStopFleeing(Piglin $$0) {
        Brain<Piglin> $$1 = $$0.getBrain();
        if (!$$1.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        }
        LivingEntity $$2 = $$1.getMemory(MemoryModuleType.AVOID_TARGET).get();
        EntityType<?> $$3 = $$2.getType();
        if ($$3 == EntityType.HOGLIN) {
            return PiglinAi.piglinsEqualOrOutnumberHoglins($$0);
        }
        if (PiglinAi.isZombified($$3)) {
            return !$$1.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, $$2);
        }
        return false;
    }

    private static boolean piglinsEqualOrOutnumberHoglins(Piglin $$0) {
        return !PiglinAi.hoglinsOutnumberPiglins($$0);
    }

    private static boolean hoglinsOutnumberPiglins(Piglin $$0) {
        int $$1 = $$0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
        int $$2 = $$0.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
        return $$2 > $$1;
    }

    private static void setAvoidTargetAndDontHuntForAWhile(Piglin $$0, LivingEntity $$1) {
        $$0.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        $$0.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        $$0.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, $$1, RETREAT_DURATION.sample($$0.level().random));
        PiglinAi.dontKillAnyMoreHoglinsForAWhile($$0);
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

    private static boolean hasCrossbow(LivingEntity $$0) {
        return $$0.isHolding(Items.CROSSBOW);
    }

    private static void admireGoldItem(LivingEntity $$0) {
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

    private static boolean seesPlayerHoldingLovedItem(LivingEntity $$0) {
        return $$0.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity $$0) {
        return !PiglinAi.seesPlayerHoldingLovedItem($$0);
    }

    public static boolean isPlayerHoldingLovedItem(LivingEntity $$0) {
        return $$0.getType() == EntityType.PLAYER && $$0.isHolding(PiglinAi::isLovedItem);
    }

    private static boolean isAdmiringDisabled(Piglin $$0) {
        return $$0.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
    }

    private static boolean wasHurtRecently(LivingEntity $$0) {
        return $$0.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private static boolean isHoldingItemInOffHand(Piglin $$0) {
        return !$$0.getOffhandItem().isEmpty();
    }

    private static boolean isNotHoldingLovedItemInOffHand(Piglin $$0) {
        return $$0.getOffhandItem().isEmpty() || !PiglinAi.isLovedItem($$0.getOffhandItem());
    }

    public static boolean isZombified(EntityType<?> $$0) {
        return $$0 == EntityType.ZOMBIFIED_PIGLIN || $$0 == EntityType.ZOGLIN;
    }
}

