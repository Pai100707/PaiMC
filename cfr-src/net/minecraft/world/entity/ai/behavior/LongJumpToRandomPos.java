/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LongJumpToRandomPos<E extends Mob>
extends Behavior<E> {
    protected static final int FIND_JUMP_TRIES = 20;
    private static final int PREPARE_JUMP_DURATION = 40;
    protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
    private static final int TIME_OUT_DURATION = 200;
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList((Object[])new Integer[]{65, 70, 75, 80});
    private final UniformInt timeBetweenLongJumps;
    protected final int maxLongJumpHeight;
    protected final int maxLongJumpWidth;
    protected final float maxJumpVelocityMultiplier;
    protected List<PossibleJump> jumpCandidates = Lists.newArrayList();
    protected Optional<Vec3> initialPosition = Optional.empty();
    protected @Nullable Vec3 chosenJump;
    protected int findJumpTries;
    protected long prepareJumpStart;
    private final Function<E, SoundEvent> getJumpSound;
    private final BiPredicate<E, BlockPos> acceptableLandingSpot;

    public LongJumpToRandomPos(UniformInt $$0, int $$1, int $$2, float $$3, Function<E, SoundEvent> $$4) {
        this($$0, $$1, $$2, $$3, $$4, LongJumpToRandomPos::defaultAcceptableLandingSpot);
    }

    public static <E extends Mob> boolean defaultAcceptableLandingSpot(E $$0, BlockPos $$1) {
        BlockPos $$3;
        Level $$2 = $$0.level();
        return $$2.getBlockState($$3 = $$1.below()).isSolidRender() && $$0.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic($$0, $$1)) == 0.0f;
    }

    public LongJumpToRandomPos(UniformInt $$0, int $$1, int $$2, float $$3, Function<E, SoundEvent> $$4, BiPredicate<E, BlockPos> $$5) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 200);
        this.timeBetweenLongJumps = $$0;
        this.maxLongJumpHeight = $$1;
        this.maxLongJumpWidth = $$2;
        this.maxJumpVelocityMultiplier = $$3;
        this.getJumpSound = $$4;
        this.acceptableLandingSpot = $$5;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Mob $$1) {
        boolean $$2;
        boolean bl = $$2 = $$1.onGround() && !$$1.isInWater() && !$$1.isInLava() && !$$0.getBlockState($$1.blockPosition()).is(Blocks.HONEY_BLOCK);
        if (!$$2) {
            $$1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample($$0.random) / 2);
        }
        return $$2;
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Mob $$1, long $$2) {
        boolean $$3;
        boolean bl = $$3 = this.initialPosition.isPresent() && this.initialPosition.get().equals($$1.position()) && this.findJumpTries > 0 && !$$1.isInWater() && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
        if (!$$3 && $$1.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
            $$1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample($$0.random) / 2);
            $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
        return $$3;
    }

    @Override
    protected void start(ServerLevel $$0, E $$12, long $$2) {
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.initialPosition = Optional.of(((Entity)$$12).position());
        BlockPos $$3 = ((Entity)$$12).blockPosition();
        int $$4 = $$3.getX();
        int $$5 = $$3.getY();
        int $$6 = $$3.getZ();
        this.jumpCandidates = BlockPos.betweenClosedStream($$4 - this.maxLongJumpWidth, $$5 - this.maxLongJumpHeight, $$6 - this.maxLongJumpWidth, $$4 + this.maxLongJumpWidth, $$5 + this.maxLongJumpHeight, $$6 + this.maxLongJumpWidth).filter($$1 -> !$$1.equals($$3)).map($$1 -> new PossibleJump($$1.immutable(), Mth.ceil($$3.distSqr((Vec3i)$$1)))).collect(Collectors.toCollection(Lists::newArrayList));
    }

    @Override
    protected void tick(ServerLevel $$0, E $$1, long $$2) {
        if (this.chosenJump != null) {
            if ($$2 - this.prepareJumpStart >= 40L) {
                ((Entity)$$1).setYRot(((Mob)$$1).yBodyRot);
                ((LivingEntity)$$1).setDiscardFriction(true);
                double $$3 = this.chosenJump.length();
                double $$4 = $$3 + (double)((LivingEntity)$$1).getJumpBoostPower();
                ((Entity)$$1).setDeltaMovement(this.chosenJump.scale($$4 / $$3));
                ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                $$0.playSound(null, (Entity)$$1, this.getJumpSound.apply($$1), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        } else {
            --this.findJumpTries;
            this.pickCandidate($$0, $$1, $$2);
        }
    }

    protected void pickCandidate(ServerLevel $$0, E $$1, long $$2) {
        while (!this.jumpCandidates.isEmpty()) {
            Vec3 $$6;
            Vec3 $$7;
            PossibleJump $$4;
            BlockPos $$5;
            Optional<PossibleJump> $$3 = this.getJumpCandidate($$0);
            if ($$3.isEmpty() || !this.isAcceptableLandingPosition($$0, $$1, $$5 = ($$4 = $$3.get()).targetPos()) || ($$7 = this.calculateOptimalJumpVector((Mob)$$1, $$6 = Vec3.atCenterOf($$5))) == null) continue;
            ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$5));
            PathNavigation $$8 = ((Mob)$$1).getNavigation();
            Path $$9 = $$8.createPath($$5, 0, 8);
            if ($$9 != null && $$9.canReach()) continue;
            this.chosenJump = $$7;
            this.prepareJumpStart = $$2;
            return;
        }
    }

    protected Optional<PossibleJump> getJumpCandidate(ServerLevel $$0) {
        Optional<PossibleJump> $$1 = WeightedRandom.getRandomItem($$0.random, this.jumpCandidates, PossibleJump::weight);
        $$1.ifPresent(this.jumpCandidates::remove);
        return $$1;
    }

    private boolean isAcceptableLandingPosition(ServerLevel $$0, E $$1, BlockPos $$2) {
        BlockPos $$3 = ((Entity)$$1).blockPosition();
        int $$4 = $$3.getX();
        int $$5 = $$3.getZ();
        if ($$4 == $$2.getX() && $$5 == $$2.getZ()) {
            return false;
        }
        return this.acceptableLandingSpot.test($$1, $$2);
    }

    protected @Nullable Vec3 calculateOptimalJumpVector(Mob $$0, Vec3 $$1) {
        ArrayList $$2 = Lists.newArrayList(ALLOWED_ANGLES);
        Collections.shuffle($$2);
        float $$3 = (float)($$0.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.maxJumpVelocityMultiplier);
        Iterator iterator = $$2.iterator();
        while (iterator.hasNext()) {
            int $$4 = (Integer)iterator.next();
            Optional<Vec3> $$5 = LongJumpUtil.calculateJumpVectorForAngle($$0, $$1, $$3, $$4, true);
            if (!$$5.isPresent()) continue;
            return $$5.get();
        }
        return null;
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Mob)livingEntity), l);
    }

    public record PossibleJump(BlockPos targetPos, int weight) {
    }
}

