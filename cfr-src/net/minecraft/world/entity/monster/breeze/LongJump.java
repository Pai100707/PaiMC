/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.arguments.EntityAnchorArgument;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.breeze.BreezeUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LongJump
extends Behavior<Breeze> {
    private static final int REQUIRED_AIR_BLOCKS_ABOVE = 4;
    private static final int JUMP_COOLDOWN_TICKS = 10;
    private static final int JUMP_COOLDOWN_WHEN_HURT_TICKS = 2;
    private static final int INHALING_DURATION_TICKS = Math.round(10.0f);
    private static final float DEFAULT_FOLLOW_RANGE = 24.0f;
    private static final float DEFAULT_MAX_JUMP_VELOCITY = 1.4f;
    private static final float MAX_JUMP_VELOCITY_MULTIPLIER = 0.058333334f;
    private static final ObjectArrayList<Integer> ALLOWED_ANGLES = new ObjectArrayList((Collection)Lists.newArrayList((Object[])new Integer[]{40, 55, 60, 75, 80}));

    @VisibleForTesting
    public LongJump() {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.REGISTERED, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.REGISTERED), 200);
    }

    public static boolean canRun(ServerLevel $$0, Breeze $$1) {
        if (!$$1.onGround() && !$$1.isInWater()) {
            return false;
        }
        if (Swim.shouldSwim($$1)) {
            return false;
        }
        if ($$1.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_PRESENT)) {
            return true;
        }
        LivingEntity $$2 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if ($$2 == null) {
            return false;
        }
        if (LongJump.outOfAggroRange($$1, $$2)) {
            $$1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return false;
        }
        if (LongJump.tooCloseForJump($$1, $$2)) {
            return false;
        }
        if (!LongJump.canJumpFromCurrentPosition($$0, $$1)) {
            return false;
        }
        BlockPos $$3 = LongJump.snapToSurface($$1, BreezeUtil.randomPointBehindTarget($$2, $$1.getRandom()));
        if ($$3 == null) {
            return false;
        }
        BlockState $$4 = $$0.getBlockState($$3.below());
        if ($$1.getType().isBlockDangerous($$4)) {
            return false;
        }
        if (!BreezeUtil.hasLineOfSight($$1, $$3.getCenter()) && !BreezeUtil.hasLineOfSight($$1, $$3.above(4).getCenter())) {
            return false;
        }
        $$1.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, $$3);
        return true;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Breeze $$1) {
        return LongJump.canRun($$0, $$1);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Breeze $$1, long $$2) {
        return $$1.getPose() != Pose.STANDING && !$$1.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
    }

    @Override
    protected void start(ServerLevel $$0, Breeze $$12, long $$2) {
        if ($$12.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT)) {
            $$12.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, INHALING_DURATION_TICKS);
        }
        $$12.setPose(Pose.INHALING);
        $$0.playSound(null, $$12, SoundEvents.BREEZE_CHARGE, SoundSource.HOSTILE, 1.0f, 1.0f);
        $$12.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).ifPresent($$1 -> $$12.lookAt(EntityAnchorArgument.Anchor.EYES, $$1.getCenter()));
    }

    @Override
    protected void tick(ServerLevel $$0, Breeze $$12, long $$2) {
        boolean $$3 = $$12.isInWater();
        if (!$$3 && $$12.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_PRESENT)) {
            $$12.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
        }
        if (LongJump.isFinishedInhaling($$12)) {
            Vec3 $$4 = $$12.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).flatMap($$1 -> LongJump.calculateOptimalJumpVector($$12, $$12.getRandom(), Vec3.atBottomCenterOf($$1))).orElse(null);
            if ($$4 == null) {
                $$12.setPose(Pose.STANDING);
                return;
            }
            if ($$3) {
                $$12.getBrain().setMemory(MemoryModuleType.BREEZE_LEAVING_WATER, Unit.INSTANCE);
            }
            $$12.playSound(SoundEvents.BREEZE_JUMP, 1.0f, 1.0f);
            $$12.setPose(Pose.LONG_JUMPING);
            $$12.setYRot($$12.yBodyRot);
            $$12.setDiscardFriction(true);
            $$12.setDeltaMovement($$4);
        } else if (LongJump.isFinishedJumping($$12)) {
            $$12.playSound(SoundEvents.BREEZE_LAND, 1.0f, 1.0f);
            $$12.setPose(Pose.STANDING);
            $$12.setDiscardFriction(false);
            boolean $$5 = $$12.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
            $$12.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, $$5 ? 2L : 10L);
            $$12.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
        }
    }

    @Override
    protected void stop(ServerLevel $$0, Breeze $$1, long $$2) {
        if ($$1.getPose() == Pose.LONG_JUMPING || $$1.getPose() == Pose.INHALING) {
            $$1.setPose(Pose.STANDING);
        }
        $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_TARGET);
        $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_INHALING);
        $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_LEAVING_WATER);
    }

    private static boolean isFinishedInhaling(Breeze $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && $$0.getPose() == Pose.INHALING;
    }

    private static boolean isFinishedJumping(Breeze $$0) {
        boolean $$1 = $$0.getPose() == Pose.LONG_JUMPING;
        boolean $$2 = $$0.onGround();
        boolean $$3 = $$0.isInWater() && $$0.getBrain().checkMemory(MemoryModuleType.BREEZE_LEAVING_WATER, MemoryStatus.VALUE_ABSENT);
        return $$1 && ($$2 || $$3);
    }

    private static @Nullable BlockPos snapToSurface(LivingEntity $$0, Vec3 $$1) {
        ClipContext $$2 = new ClipContext($$1, $$1.relative(Direction.DOWN, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, $$0);
        BlockHitResult $$3 = $$0.level().clip($$2);
        if (((HitResult)$$3).getType() == HitResult.Type.BLOCK) {
            return BlockPos.containing($$3.getLocation()).above();
        }
        ClipContext $$4 = new ClipContext($$1, $$1.relative(Direction.UP, 10.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, $$0);
        BlockHitResult $$5 = $$0.level().clip($$4);
        if (((HitResult)$$5).getType() == HitResult.Type.BLOCK) {
            return BlockPos.containing($$5.getLocation()).above();
        }
        return null;
    }

    private static boolean outOfAggroRange(Breeze $$0, LivingEntity $$1) {
        return !$$1.closerThan($$0, $$0.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    private static boolean tooCloseForJump(Breeze $$0, LivingEntity $$1) {
        return $$1.distanceTo($$0) - 4.0f <= 0.0f;
    }

    private static boolean canJumpFromCurrentPosition(ServerLevel $$0, Breeze $$1) {
        BlockPos $$2 = $$1.blockPosition();
        if ($$0.getBlockState($$2).is(Blocks.HONEY_BLOCK)) {
            return false;
        }
        for (int $$3 = 1; $$3 <= 4; ++$$3) {
            BlockPos $$4 = $$2.relative(Direction.UP, $$3);
            if ($$0.getBlockState($$4).isAir() || $$0.getFluidState($$4).is(FluidTags.WATER)) continue;
            return false;
        }
        return true;
    }

    private static Optional<Vec3> calculateOptimalJumpVector(Breeze $$0, RandomSource $$12, Vec3 $$2) {
        List<Integer> $$3 = Util.shuffledCopy(ALLOWED_ANGLES, $$12);
        for (int $$4 : $$3) {
            float $$5 = 0.058333334f * (float)$$0.getAttributeValue(Attributes.FOLLOW_RANGE);
            Optional<Vec3> $$6 = LongJumpUtil.calculateJumpVectorForAngle($$0, $$2, $$5, $$4, false);
            if (!$$6.isPresent()) continue;
            if ($$0.hasEffect(MobEffects.JUMP_BOOST)) {
                double $$7 = $$6.get().normalize().y * (double)$$0.getJumpBoostPower();
                return $$6.map($$1 -> $$1.add(0.0, $$7, 0.0));
            }
            return $$6;
        }
        return Optional.empty();
    }

    @Override
    protected /* synthetic */ boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
        return this.checkExtraStartConditions(serverLevel, (Breeze)livingEntity);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Breeze)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Breeze)livingEntity, l);
    }
}

