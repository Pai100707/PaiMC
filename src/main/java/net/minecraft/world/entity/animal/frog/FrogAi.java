package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.Croak;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LongJumpMidJump;
import net.minecraft.world.entity.ai.behavior.LongJumpToPreferredBlock;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.TryFindLand;
import net.minecraft.world.entity.ai.behavior.TryFindLandNearWater;
import net.minecraft.world.entity.ai.behavior.TryLaySpawnOnWaterNearLand;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FrogAi {
   private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
   private static final float SPEED_MULTIPLIER_ON_LAND = 1.0F;
   private static final float SPEED_MULTIPLIER_IN_WATER = 0.75F;
   private static final UniformInt TIME_BETWEEN_LONG_JUMPS = UniformInt.of(100, 140);
   private static final int MAX_LONG_JUMP_HEIGHT = 2;
   private static final int MAX_LONG_JUMP_WIDTH = 4;
   private static final float MAX_JUMP_VELOCITY_MULTIPLIER = 3.5714288F;
   private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;

   protected static void initMemories(Frog $$0, RandomSource $$1) {
      $$0.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, TIME_BETWEEN_LONG_JUMPS.sample($$1));
   }

   protected static Brain<?> makeBrain(Brain<Frog> $$0) {
      initCoreActivity($$0);
      initIdleActivity($$0);
      initSwimActivity($$0);
      initLaySpawnActivity($$0);
      initTongueActivity($$0);
      initJumpActivity($$0);
      $$0.setCoreActivities(ImmutableSet.of(Activity.CORE));
      $$0.setDefaultActivity(Activity.IDLE);
      $$0.useDefaultActivity();
      return $$0;
   }

   private static void initCoreActivity(Brain<Frog> $$0) {
      $$0.addActivity(
         Activity.CORE,
         0,
         ImmutableList.of(
            new AnimalPanic(2.0F),
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink(),
            new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
            new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS)
         )
      );
   }

   private static void initIdleActivity(Brain<Frog> $$0) {
      $$0.addActivityWithConditions(
         Activity.IDLE,
         ImmutableList.of(
            Pair.of(0, SetEntityLookTargetSometimes.create(net.minecraft.world.entity.EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
            Pair.of(0, new AnimalMakeLove(net.minecraft.world.entity.EntityType.FROG)),
            Pair.of(1, new FollowTemptation($$0x -> 1.25F)),
            Pair.of(2, StartAttacking.create(($$0x, $$1) -> canAttack($$1), ($$0x, $$1) -> $$1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
            Pair.of(3, TryFindLand.create(6, 1.0F)),
            Pair.of(
               4,
               new RunOne(
                  ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                  ImmutableList.of(
                     Pair.of(RandomStroll.stroll(1.0F), 1),
                     Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                     Pair.of(new Croak(), 3),
                     Pair.of(BehaviorBuilder.triggerIf(net.minecraft.world.entity.Entity::onGround), 2)
                  )
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)
         )
      );
   }

   private static void initSwimActivity(Brain<Frog> $$0) {
      $$0.addActivityWithConditions(
         Activity.SWIM,
         ImmutableList.of(
            Pair.of(0, SetEntityLookTargetSometimes.create(net.minecraft.world.entity.EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
            Pair.of(1, new FollowTemptation($$0x -> 1.25F)),
            Pair.of(2, StartAttacking.create(($$0x, $$1) -> canAttack($$1), ($$0x, $$1) -> $$1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
            Pair.of(3, TryFindLand.create(8, 1.5F)),
            Pair.of(
               5,
               new GateBehavior(
                  ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                  ImmutableSet.of(),
                  GateBehavior.OrderPolicy.ORDERED,
                  GateBehavior.RunningPolicy.TRY_ALL,
                  ImmutableList.of(
                     Pair.of(RandomStroll.swim(0.75F), 1),
                     Pair.of(RandomStroll.stroll(1.0F, true), 1),
                     Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                     Pair.of(BehaviorBuilder.triggerIf(net.minecraft.world.entity.Entity::isInWater), 5)
                  )
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_PRESENT)
         )
      );
   }

   private static void initLaySpawnActivity(Brain<Frog> $$0) {
      $$0.addActivityWithConditions(
         Activity.LAY_SPAWN,
         ImmutableList.of(
            Pair.of(0, SetEntityLookTargetSometimes.create(net.minecraft.world.entity.EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
            Pair.of(1, StartAttacking.create(($$0x, $$1) -> canAttack($$1), ($$0x, $$1) -> $$1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE))),
            Pair.of(2, TryFindLandNearWater.create(8, 1.0F)),
            Pair.of(3, TryLaySpawnOnWaterNearLand.create(Blocks.FROGSPAWN)),
            Pair.of(
               4,
               new RunOne(
                  ImmutableList.of(
                     Pair.of(RandomStroll.stroll(1.0F), 2),
                     Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                     Pair.of(new Croak(), 2),
                     Pair.of(BehaviorBuilder.triggerIf(net.minecraft.world.entity.Entity::onGround), 1)
                  )
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.IS_PREGNANT, MemoryStatus.VALUE_PRESENT)
         )
      );
   }

   private static void initJumpActivity(Brain<Frog> $$0) {
      $$0.addActivityWithConditions(
         Activity.LONG_JUMP,
         ImmutableList.of(
            Pair.of(0, new LongJumpMidJump(TIME_BETWEEN_LONG_JUMPS, SoundEvents.FROG_STEP)),
            Pair.of(
               1,
               new LongJumpToPreferredBlock<>(
                  TIME_BETWEEN_LONG_JUMPS,
                  2,
                  4,
                  3.5714288F,
                  $$0x -> SoundEvents.FROG_LONG_JUMP,
                  BlockTags.FROG_PREFER_JUMP_TO,
                  0.5F,
                  FrogAi::isAcceptableLandingSpot
               )
            )
         ),
         ImmutableSet.of(
            Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_ABSENT)
         )
      );
   }

   private static void initTongueActivity(Brain<Frog> $$0) {
      $$0.addActivityAndRemoveMemoryWhenStopped(
         Activity.TONGUE,
         0,
         ImmutableList.of(StopAttackingIfTargetInvalid.create(), new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT)),
         MemoryModuleType.ATTACK_TARGET
      );
   }

   private static <E extends net.minecraft.world.entity.Mob> boolean isAcceptableLandingSpot(E $$0, BlockPos $$1) {
      Level $$2 = $$0.level();
      BlockPos $$3 = $$1.below();
      if ($$2.getFluidState($$1).isEmpty() && $$2.getFluidState($$3).isEmpty() && $$2.getFluidState($$1.above()).isEmpty()) {
         BlockState $$4 = $$2.getBlockState($$1);
         BlockState $$5 = $$2.getBlockState($$3);
         if (!$$4.is(BlockTags.FROG_PREFER_JUMP_TO) && !$$5.is(BlockTags.FROG_PREFER_JUMP_TO)) {
            PathfindingContext $$6 = new PathfindingContext($$0.level(), $$0);
            PathType $$7 = WalkNodeEvaluator.getPathTypeStatic($$6, $$1.mutable());
            PathType $$8 = WalkNodeEvaluator.getPathTypeStatic($$6, $$3.mutable());
            return $$7 != PathType.TRAPDOOR && (!$$4.isAir() || $$8 != PathType.TRAPDOOR) ? LongJumpToRandomPos.defaultAcceptableLandingSpot($$0, $$1) : true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean canAttack(Frog $$0) {
      return !BehaviorUtils.isBreeding($$0);
   }

   public static void updateActivity(Frog $$0) {
      $$0.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.TONGUE, Activity.LAY_SPAWN, Activity.LONG_JUMP, Activity.SWIM, Activity.IDLE));
   }

   public static Predicate<ItemStack> getTemptations() {
      return $$0 -> $$0.is(ItemTags.FROG_FOOD);
   }
}
