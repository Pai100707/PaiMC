/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AnimalPanic<E extends PathfinderMob>
extends Behavior<E> {
    private static final int PANIC_MIN_DURATION = 100;
    private static final int PANIC_MAX_DURATION = 120;
    private static final int PANIC_DISTANCE_HORIZONTAL = 5;
    private static final int PANIC_DISTANCE_VERTICAL = 4;
    private final float speedMultiplier;
    private final Function<PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes;
    private final Function<E, Vec3> positionGetter;

    public AnimalPanic(float $$02) {
        this($$02, $$0 -> DamageTypeTags.PANIC_CAUSES, $$0 -> LandRandomPos.getPos($$0, 5, 4));
    }

    public AnimalPanic(float $$02, int $$12) {
        this($$02, $$0 -> DamageTypeTags.PANIC_CAUSES, $$1 -> AirAndWaterRandomPos.getPos($$1, 5, 4, $$12, $$1.getViewVector((float)0.0f).x, $$1.getViewVector((float)0.0f).z, 1.5707963705062866));
    }

    public AnimalPanic(float $$02, Function<PathfinderMob, TagKey<DamageType>> $$1) {
        this($$02, $$1, $$0 -> LandRandomPos.getPos($$0, 5, 4));
    }

    public AnimalPanic(float $$0, Function<PathfinderMob, TagKey<DamageType>> $$1, Function<E, Vec3> $$2) {
        super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED), 100, 120);
        this.speedMultiplier = $$0;
        this.panicCausingDamageTypes = $$1;
        this.positionGetter = $$2;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, E $$12) {
        return ((LivingEntity)$$12).getBrain().getMemory(MemoryModuleType.HURT_BY).map($$1 -> $$1.is(this.panicCausingDamageTypes.apply((PathfinderMob)$$12))).orElse(false) != false || ((LivingEntity)$$12).getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
        return true;
    }

    @Override
    protected void start(ServerLevel $$0, E $$1, long $$2) {
        ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
        ((LivingEntity)$$1).getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        ((Mob)$$1).getNavigation().stop();
    }

    @Override
    protected void stop(ServerLevel $$0, E $$1, long $$2) {
        Brain<?> $$3 = ((LivingEntity)$$1).getBrain();
        $$3.eraseMemory(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void tick(ServerLevel $$0, E $$1, long $$2) {
        Vec3 $$3;
        if (((Mob)$$1).getNavigation().isDone() && ($$3 = this.getPanicPos($$1, $$0)) != null) {
            ((LivingEntity)$$1).getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$3, this.speedMultiplier, 0));
        }
    }

    private @Nullable Vec3 getPanicPos(E $$0, ServerLevel $$1) {
        Optional<Vec3> $$2;
        if (((Entity)$$0).isOnFire() && ($$2 = this.lookForWater($$1, (Entity)$$0).map(Vec3::atBottomCenterOf)).isPresent()) {
            return $$2.get();
        }
        return this.positionGetter.apply($$0);
    }

    private Optional<BlockPos> lookForWater(BlockGetter $$0, Entity $$13) {
        Predicate<BlockPos> $$4;
        BlockPos $$2 = $$13.blockPosition();
        if (!$$0.getBlockState($$2).getCollisionShape($$0, $$2).isEmpty()) {
            return Optional.empty();
        }
        if (Mth.ceil($$13.getBbWidth()) == 2) {
            Predicate<BlockPos> $$3 = $$12 -> BlockPos.squareOutSouthEast($$12).allMatch($$1 -> $$0.getFluidState((BlockPos)$$1).is(FluidTags.WATER));
        } else {
            $$4 = $$1 -> $$0.getFluidState((BlockPos)$$1).is(FluidTags.WATER);
        }
        return BlockPos.findClosestMatch($$2, 5, 1, $$4);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((PathfinderMob)livingEntity), l);
    }
}

