package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public class LongJump extends Behavior<Breeze> {
   private static final int REQUIRED_AIR_BLOCKS_ABOVE = 4;
   private static final int JUMP_COOLDOWN_TICKS = 10;
   private static final int JUMP_COOLDOWN_WHEN_HURT_TICKS = 2;
   private static final int INHALING_DURATION_TICKS = Math.round(10.0F);
   private static final float DEFAULT_FOLLOW_RANGE = 24.0F;
   private static final float DEFAULT_MAX_JUMP_VELOCITY = 1.4F;
   private static final float MAX_JUMP_VELOCITY_MULTIPLIER = 0.058333334F;
   private static final ObjectArrayList<Integer> ALLOWED_ANGLES = new ObjectArrayList(Lists.newArrayList(new Integer[]{40, 55, 60, 75, 80}));

   @VisibleForTesting
   public LongJump() {
      super(
         Map.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.BREEZE_JUMP_COOLDOWN,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_JUMP_INHALING,
            MemoryStatus.REGISTERED,
            MemoryModuleType.BREEZE_JUMP_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.BREEZE_SHOOT,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_LEAVING_WATER,
            MemoryStatus.REGISTERED
         ),
         200
      );
   }

   public static boolean canRun(ServerLevel $$0, Breeze $$1) {
      if (!$$1.onGround() && !$$1.isInWater()) {
         return false;
      } else if (Swim.shouldSwim($$1)) {
         return false;
      } else if ($$1.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_PRESENT)) {
         return true;
      } else {
         net.minecraft.world.entity.LivingEntity $$2 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
         if ($$2 == null) {
            return false;
         } else if (outOfAggroRange($$1, $$2)) {
            $$1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return false;
         } else if (tooCloseForJump($$1, $$2)) {
            return false;
         } else if (!canJumpFromCurrentPosition($$0, $$1)) {
            return false;
         } else {
            BlockPos $$3 = snapToSurface($$1, BreezeUtil.randomPointBehindTarget($$2, $$1.getRandom()));
            if ($$3 == null) {
               return false;
            } else {
               BlockState $$4 = $$0.getBlockState($$3.below());
               if ($$1.getType().isBlockDangerous($$4)) {
                  return false;
               } else if (!BreezeUtil.hasLineOfSight($$1, $$3.getCenter()) && !BreezeUtil.hasLineOfSight($$1, $$3.above(4).getCenter())) {
                  return false;
               } else {
                  $$1.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, $$3);
                  return true;
               }
            }
         }
      }
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Breeze $$1) {
      return canRun($$0, $$1);
   }

   protected boolean canStillUse(ServerLevel $$0, Breeze $$1, long $$2) {
      return $$1.getPose() != net.minecraft.world.entity.Pose.STANDING && !$$1.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
   }

   protected void start(ServerLevel $$0, Breeze $$1, long $$2) {
      if ($$1.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT)) {
         $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, INHALING_DURATION_TICKS);
      }

      $$1.setPose(net.minecraft.world.entity.Pose.INHALING);
      $$0.playSound(null, $$1, SoundEvents.BREEZE_CHARGE, SoundSource.HOSTILE, 1.0F, 1.0F);
      $$1.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).ifPresent($$1x -> $$1.lookAt(Anchor.EYES, $$1x.getCenter()));
   }

   protected void tick(ServerLevel $$0, Breeze $$1, long $$2) {
      boolean $$3 = $$1.isInWater();
      if (!$$3 && $$1.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_PRESENT)) {
         $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
      }

      if (isFinishedInhaling($$1)) {
         Vec3 $$4 = $$1.getBrain()
            .getMemory(MemoryModuleType.BREEZE_JUMP_TARGET)
            .flatMap($$1x -> calculateOptimalJumpVector($$1, $$1.getRandom(), Vec3.atBottomCenterOf($$1x)))
            .orElse(null);
         if ($$4 == null) {
            $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
            return;
         }

         if ($$3) {
            $$1.getBrain().setMemory(MemoryModuleType.BREEZE_LEAVING_WATER, Unit.INSTANCE);
         }

         $$1.playSound(SoundEvents.BREEZE_JUMP, 1.0F, 1.0F);
         $$1.setPose(net.minecraft.world.entity.Pose.LONG_JUMPING);
         $$1.setYRot($$1.yBodyRot);
         $$1.setDiscardFriction(true);
         $$1.setDeltaMovement($$4);
      } else if (isFinishedJumping($$1)) {
         $$1.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
         $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
         $$1.setDiscardFriction(false);
         boolean $$5 = $$1.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
         $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, $$5 ? 2L : 10L);
         $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
      }
   }

   protected void stop(ServerLevel $$0, Breeze $$1, long $$2) {
      if ($$1.getPose() == net.minecraft.world.entity.Pose.LONG_JUMPING || $$1.getPose() == net.minecraft.world.entity.Pose.INHALING) {
         $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
      }

      $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_TARGET);
      $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_INHALING);
      $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
   }

   private static boolean isFinishedInhaling(Breeze $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && $$0.getPose() == net.minecraft.world.entity.Pose.INHALING;
   }

   private static boolean isFinishedJumping(Breeze $$0) {
      boolean $$1 = $$0.getPose() == net.minecraft.world.entity.Pose.LONG_JUMPING;
      boolean $$2 = $$0.onGround();
      boolean $$3 = $$0.isInWater() && $$0.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_ABSENT);
      return $$1 && ($$2 || $$3);
   }

   @Nullable
   private static BlockPos snapToSurface(net.minecraft.world.entity.LivingEntity $$0, Vec3 $$1) {
      ClipContext $$2 = new ClipContext($$1, $$1.relative(Direction.DOWN, 10.0), Block.COLLIDER, Fluid.NONE, $$0);
      HitResult $$3 = $$0.level().clip($$2);
      if ($$3.getType() == Type.BLOCK) {
         return BlockPos.containing($$3.getLocation()).above();
      } else {
         ClipContext $$4 = new ClipContext($$1, $$1.relative(Direction.UP, 10.0), Block.COLLIDER, Fluid.NONE, $$0);
         HitResult $$5 = $$0.level().clip($$4);
         return $$5.getType() == Type.BLOCK ? BlockPos.containing($$5.getLocation()).above() : null;
      }
   }

   private static boolean outOfAggroRange(Breeze $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return !$$1.closerThan($$0, $$0.getAttributeValue(Attributes.FOLLOW_RANGE));
   }

   private static boolean tooCloseForJump(Breeze $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return $$1.distanceTo($$0) - 4.0F <= 0.0F;
   }

   private static boolean canJumpFromCurrentPosition(ServerLevel $$0, Breeze $$1) {
      BlockPos $$2 = $$1.blockPosition();
      if ($$0.getBlockState($$2).is(Blocks.HONEY_BLOCK)) {
         return false;
      } else {
         for (int $$3 = 1; $$3 <= 4; $$3++) {
            BlockPos $$4 = $$2.relative(Direction.UP, $$3);
            if (!$$0.getBlockState($$4).isAir() && !$$0.getFluidState($$4).is(FluidTags.WATER)) {
               return false;
            }
         }

         return true;
      }
   }

   private static Optional<Vec3> calculateOptimalJumpVector(Breeze $$0, RandomSource $$1, Vec3 $$2) {
      for (int $$4 : Util.shuffledCopy(ALLOWED_ANGLES, $$1)) {
         float $$5 = 0.058333334F * (float)$$0.getAttributeValue(Attributes.FOLLOW_RANGE);
         Optional<Vec3> $$6 = LongJumpUtil.calculateJumpVectorForAngle($$0, $$2, $$5, $$4, false);
         if ($$6.isPresent()) {
            if ($$0.hasEffect(MobEffects.JUMP_BOOST)) {
               double $$7 = $$6.get().normalize().y * $$0.getJumpBoostPower();
               return $$6.map($$1x -> $$1x.add(0.0, $$7, 0.0));
            }

            return $$6;
         }
      }

      return Optional.empty();
   }
}
