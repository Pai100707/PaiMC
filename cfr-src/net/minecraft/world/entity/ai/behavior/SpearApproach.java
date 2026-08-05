/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.SpearAttack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jspecify.annotations.Nullable;

public class SpearApproach
extends Behavior<PathfinderMob> {
    double speedModifierWhenRepositioning;
    float approachDistanceSq;

    public SpearApproach(double $$0, float $$1) {
        super(Map.of(MemoryModuleType.SPEAR_STATUS, MemoryStatus.VALUE_ABSENT));
        this.speedModifierWhenRepositioning = $$0;
        this.approachDistanceSq = $$1 * $$1;
    }

    private boolean ableToAttack(PathfinderMob $$0) {
        return this.getTarget($$0) != null && $$0.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, PathfinderMob $$1) {
        return this.ableToAttack($$1) && !$$1.isUsingItem();
    }

    @Override
    protected void start(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        $$1.setAggressive(true);
        $$1.getBrain().setMemory(MemoryModuleType.SPEAR_STATUS, SpearAttack.SpearStatus.APPROACH);
        super.start($$0, $$1, $$2);
    }

    private @Nullable LivingEntity getTarget(PathfinderMob $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        return this.ableToAttack($$1) && this.farEnough($$1);
    }

    private boolean farEnough(PathfinderMob $$0) {
        LivingEntity $$1 = this.getTarget($$0);
        double $$2 = $$0.distanceToSqr($$1.getX(), $$1.getY(), $$1.getZ());
        return $$2 > (double)this.approachDistanceSq;
    }

    @Override
    protected void tick(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        LivingEntity $$3 = this.getTarget($$1);
        Entity $$4 = $$1.getRootVehicle();
        float $$5 = 1.0f;
        if ($$4 instanceof Mob) {
            Mob $$6 = (Mob)$$4;
            $$5 = $$6.chargeSpeedModifier();
        }
        $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$3, true));
        $$1.getNavigation().moveTo($$3, (double)$$5 * this.speedModifierWhenRepositioning);
    }

    @Override
    protected void stop(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        $$1.getNavigation().stop();
        $$1.getBrain().setMemory(MemoryModuleType.SPEAR_STATUS, SpearAttack.SpearStatus.CHARGING);
    }

    @Override
    protected boolean timedOut(long $$0) {
        return false;
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }
}

