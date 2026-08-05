/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.GoAndGiveItemsToTarget;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StayCloseToTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AllayAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_DEPOSIT_TARGET = 2.25f;
    private static final float SPEED_MULTIPLIER_WHEN_RETRIEVING_ITEM = 1.75f;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.5f;
    private static final int CLOSE_ENOUGH_TO_TARGET = 4;
    private static final int TOO_FAR_FROM_TARGET = 16;
    private static final int MAX_LOOK_DISTANCE = 6;
    private static final int MIN_WAIT_DURATION = 30;
    private static final int MAX_WAIT_DURATION = 60;
    private static final int TIME_TO_FORGET_NOTEBLOCK = 600;
    private static final int DISTANCE_TO_WANTED_ITEM = 32;
    private static final int GIVE_ITEM_TIMEOUT_DURATION = 20;

    protected static Brain<?> makeBrain(Brain<Allay> $$0) {
        AllayAi.initCoreActivity($$0);
        AllayAi.initIdleActivity($$0);
        $$0.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        $$0.setDefaultActivity(Activity.IDLE);
        $$0.useDefaultActivity();
        return $$0;
    }

    private static void initCoreActivity(Brain<Allay> $$0) {
        $$0.addActivity(Activity.CORE, 0, (ImmutableList<BehaviorControl<Allay>>)ImmutableList.of(new Swim(0.8f), new AnimalPanic(2.5f), (Object)new LookAtTargetSink(45, 90), (Object)new MoveToTargetSink(), (Object)new CountDownCooldownTicks(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS), (Object)new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(Brain<Allay> $$02) {
        $$02.addActivityWithConditions(Activity.IDLE, (ImmutableList<Pair<Integer, BehaviorControl<Allay>>>)ImmutableList.of((Object)Pair.of((Object)0, GoToWantedItem.create($$0 -> true, 1.75f, true, 32)), (Object)Pair.of((Object)1, new GoAndGiveItemsToTarget(AllayAi::getItemDepositPosition, 2.25f, 20)), (Object)Pair.of((Object)2, StayCloseToTarget.create(AllayAi::getItemDepositPosition, Predicate.not(AllayAi::hasWantedItem), 4, 16, 2.25f)), (Object)Pair.of((Object)3, SetEntityLookTargetSometimes.create(6.0f, UniformInt.of(30, 60))), (Object)Pair.of((Object)4, new RunOne(ImmutableList.of((Object)Pair.of(RandomStroll.fly(1.0f), (Object)2), (Object)Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), (Object)2), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1))))), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of());
    }

    public static void updateActivity(Allay $$0) {
        $$0.getBrain().setActiveActivityToFirstValid((List<Activity>)ImmutableList.of((Object)Activity.IDLE));
    }

    public static void hearNoteblock(LivingEntity $$0, BlockPos $$1) {
        Brain<?> $$2 = $$0.getBrain();
        GlobalPos $$3 = GlobalPos.of($$0.level().dimension(), $$1);
        Optional<GlobalPos> $$4 = $$2.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        if ($$4.isEmpty()) {
            $$2.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION, $$3);
            $$2.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        } else if ($$4.get().equals($$3)) {
            $$2.setMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        }
    }

    private static Optional<PositionTracker> getItemDepositPosition(LivingEntity $$0) {
        Brain<?> $$1 = $$0.getBrain();
        Optional<GlobalPos> $$2 = $$1.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        if ($$2.isPresent()) {
            GlobalPos $$3 = $$2.get();
            if (AllayAi.shouldDepositItemsAtLikedNoteblock($$0, $$1, $$3)) {
                return Optional.of(new BlockPosTracker($$3.pos().above()));
            }
            $$1.eraseMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
        }
        return AllayAi.getLikedPlayerPositionTracker($$0);
    }

    private static boolean hasWantedItem(LivingEntity $$0) {
        Brain<ItemEntity> $$1 = $$0.getBrain();
        return $$1.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

    private static boolean shouldDepositItemsAtLikedNoteblock(LivingEntity $$0, Brain<?> $$1, GlobalPos $$2) {
        Optional<Integer> $$3 = $$1.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
        Level $$4 = $$0.level();
        return $$2.isCloseEnough($$4.dimension(), $$0.blockPosition(), 1024) && $$4.getBlockState($$2.pos()).is(Blocks.NOTE_BLOCK) && $$3.isPresent();
    }

    private static Optional<PositionTracker> getLikedPlayerPositionTracker(LivingEntity $$02) {
        return AllayAi.getLikedPlayer($$02).map($$0 -> new EntityTracker((Entity)$$0, true));
    }

    public static Optional<ServerPlayer> getLikedPlayer(LivingEntity $$0) {
        Level $$1 = $$0.level();
        if (!$$1.isClientSide() && $$1 instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)$$1;
            Optional<UUID> $$3 = $$0.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
            if ($$3.isPresent()) {
                Entity $$4 = $$2.getEntity($$3.get());
                if ($$4 instanceof ServerPlayer) {
                    ServerPlayer $$5 = (ServerPlayer)$$4;
                    if (($$5.gameMode.isSurvival() || $$5.gameMode.isCreative()) && $$5.closerThan($$0, 64.0)) {
                        return Optional.of($$5);
                    }
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}

