package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends net.minecraft.world.entity.Mob> extends Behavior<E> {
   protected static final int FIND_JUMP_TRIES = 20;
   private static final int PREPARE_JUMP_DURATION = 40;
   protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
   private static final int TIME_OUT_DURATION = 200;
   private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(new Integer[]{65, 70, 75, 80});
   private final UniformInt timeBetweenLongJumps;
   protected final int maxLongJumpHeight;
   protected final int maxLongJumpWidth;
   protected final float maxJumpVelocityMultiplier;
   protected List<LongJumpToRandomPos.PossibleJump> jumpCandidates = Lists.newArrayList();
   protected Optional<Vec3> initialPosition = Optional.empty();
   
   protected Vec3 chosenJump;
   protected int findJumpTries;
   protected long prepareJumpStart;
   private final Function<E, SoundEvent> getJumpSound;
   private final BiPredicate<E, BlockPos> acceptableLandingSpot;

   public LongJumpToRandomPos(UniformInt $$0, int $$1, int $$2, float $$3, Function<E, SoundEvent> $$4) {
      this($$0, $$1, $$2, $$3, $$4, LongJumpToRandomPos::defaultAcceptableLandingSpot);
   }

   public static <E extends net.minecraft.world.entity.Mob> boolean defaultAcceptableLandingSpot(E $$0, BlockPos $$1) {
      Level $$2 = $$0.level();
      BlockPos $$3 = $$1.below();
      return $$2.getBlockState($$3).isSolidRender() && $$0.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic($$0, $$1)) == 0.0F;
   }

   public LongJumpToRandomPos(UniformInt $$0, int $$1, int $$2, float $$3, Function<E, SoundEvent> $$4, BiPredicate<E, BlockPos> $$5) {
      super(
         ImmutableMap.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.LONG_JUMP_MID_JUMP,
            MemoryStatus.VALUE_ABSENT
         ),
         200
      );
      this.timeBetweenLongJumps = $$0;
      this.maxLongJumpHeight = $$1;
      this.maxLongJumpWidth = $$2;
      this.maxJumpVelocityMultiplier = $$3;
      this.getJumpSound = $$4;
      this.acceptableLandingSpot = $$5;
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.Mob $$1) {
      boolean $$2 = $$1.onGround() && !$$1.isInWater() && !$$1.isInLava() && !$$0.getBlockState($$1.blockPosition()).is(Blocks.HONEY_BLOCK);
      if (!$$2) {
         $$1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample($$0.random) / 2);
      }

      return $$2;
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      boolean $$3 = this.initialPosition.isPresent()
         && this.initialPosition.get().equals($$1.position())
         && this.findJumpTries > 0
         && !$$1.isInWater()
         && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
      if (!$$3 && $$1.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
         $$1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample($$0.random) / 2);
         $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      }

      return $$3;
   }

   protected void start(ServerLevel $$0, E $$1, long $$2) {
      this.chosenJump = null;
      this.findJumpTries = 20;
      this.initialPosition = Optional.of($$1.position());
      BlockPos $$3 = $$1.blockPosition();
      int $$4 = $$3.getX();
      int $$5 = $$3.getY();
      int $$6 = $$3.getZ();
      this.jumpCandidates = BlockPos.betweenClosedStream(
            $$4 - this.maxLongJumpWidth,
            $$5 - this.maxLongJumpHeight,
            $$6 - this.maxLongJumpWidth,
            $$4 + this.maxLongJumpWidth,
            $$5 + this.maxLongJumpHeight,
            $$6 + this.maxLongJumpWidth
         )
         .filter($$1x -> !$$1x.equals($$3))
         .map($$1x -> new LongJumpToRandomPos.PossibleJump($$1x.immutable(), Mth.ceil($$3.distSqr($$1x))))
         .collect(Collectors.toCollection(Lists::newArrayList));
   }

   protected void tick(ServerLevel $$0, E $$1, long $$2) {
      if (this.chosenJump != null) {
         if ($$2 - this.prepareJumpStart >= 40L) {
            $$1.setYRot($$1.yBodyRot);
            $$1.setDiscardFriction(true);
            double $$3 = this.chosenJump.length();
            double $$4 = $$3 + $$1.getJumpBoostPower();
            $$1.setDeltaMovement(this.chosenJump.scale($$4 / $$3));
            $$1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
            $$0.playSound(null, $$1, this.getJumpSound.apply($$1), SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      } else {
         this.findJumpTries--;
         this.pickCandidate($$0, $$1, $$2);
      }
   }

   protected void pickCandidate(ServerLevel $$0, E $$1, long $$2) {
      while (!this.jumpCandidates.isEmpty()) {
         Optional<LongJumpToRandomPos.PossibleJump> $$3 = this.getJumpCandidate($$0);
         if (!$$3.isEmpty()) {
            LongJumpToRandomPos.PossibleJump $$4 = $$3.get();
            BlockPos $$5 = $$4.targetPos();
            if (this.isAcceptableLandingPosition($$0, $$1, $$5)) {
               Vec3 $$6 = Vec3.atCenterOf($$5);
               Vec3 $$7 = this.calculateOptimalJumpVector($$1, $$6);
               if ($$7 != null) {
                  $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$5));
                  PathNavigation $$8 = $$1.getNavigation();
                  Path $$9 = $$8.createPath($$5, 0, 8);
                  if ($$9 == null || !$$9.canReach()) {
                     this.chosenJump = $$7;
                     this.prepareJumpStart = $$2;
                     return;
                  }
               }
            }
         }
      }
   }

   protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel $$0) {
      Optional<LongJumpToRandomPos.PossibleJump> $$1 = WeightedRandom.getRandomItem($$0.random, this.jumpCandidates, LongJumpToRandomPos.PossibleJump::weight);
      $$1.ifPresent(this.jumpCandidates::remove);
      return $$1;
   }

   private boolean isAcceptableLandingPosition(ServerLevel $$0, E $$1, BlockPos $$2) {
      BlockPos $$3 = $$1.blockPosition();
      int $$4 = $$3.getX();
      int $$5 = $$3.getZ();
      return $$4 == $$2.getX() && $$5 == $$2.getZ() ? false : this.acceptableLandingSpot.test($$1, $$2);
   }

   
   protected Vec3 calculateOptimalJumpVector(net.minecraft.world.entity.Mob $$0, Vec3 $$1) {
      List<Integer> $$2 = Lists.newArrayList(ALLOWED_ANGLES);
      Collections.shuffle($$2);
      float $$3 = (float)($$0.getAttributeValue(Attributes.JUMP_STRENGTH) * this.maxJumpVelocityMultiplier);

      for (int $$4 : $$2) {
         Optional<Vec3> $$5 = LongJumpUtil.calculateJumpVectorForAngle($$0, $$1, $$3, $$4, true);
         if ($$5.isPresent()) {
            return $$5.get();
         }
      }

      return null;
   }

   public record PossibleJump(BlockPos targetPos, int weight) {
   }
}
