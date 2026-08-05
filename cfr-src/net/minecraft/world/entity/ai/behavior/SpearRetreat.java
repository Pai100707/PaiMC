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
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SpearRetreat
extends Behavior<PathfinderMob> {
    public static final int MIN_COOLDOWN_DISTANCE = 9;
    public static final int MAX_COOLDOWN_DISTANCE = 11;
    public static final int MAX_FLEEING_TIME = 100;
    double speedModifierWhenRepositioning;

    public SpearRetreat(double $$0) {
        super(Map.of(MemoryModuleType.SPEAR_STATUS, MemoryStatus.VALUE_PRESENT), 100);
        this.speedModifierWhenRepositioning = $$0;
    }

    private @Nullable LivingEntity getTarget(PathfinderMob $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    private boolean ableToAttack(PathfinderMob $$0) {
        return this.getTarget($$0) != null && $$0.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, PathfinderMob $$1) {
        double $$5;
        if (!this.ableToAttack($$1) || $$1.isUsingItem()) {
            return false;
        }
        if ($$1.getBrain().getMemory(MemoryModuleType.SPEAR_STATUS).orElse(SpearAttack.SpearStatus.APPROACH) != SpearAttack.SpearStatus.RETREAT) {
            return false;
        }
        LivingEntity $$2 = this.getTarget($$1);
        double $$3 = $$1.distanceToSqr($$2.getX(), $$2.getY(), $$2.getZ());
        int $$4 = $$1.isPassenger() ? 2 : 0;
        Vec3 $$6 = LandRandomPos.getPosAway($$1, Math.max(0.0, (double)(9 + $$4) - ($$5 = Math.sqrt($$3))), Math.max(1.0, (double)(11 + $$4) - $$5), 7, $$2.position());
        if ($$6 == null) {
            return false;
        }
        $$1.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_POSITION, $$6);
        return true;
    }

    @Override
    protected void start(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        $$1.setAggressive(true);
        $$1.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_TIME, 0);
        super.start($$0, $$1, $$2);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        return $$1.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_TIME).orElse(100) < 100 && $$1.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_POSITION).isPresent() && !$$1.getNavigation().isDone() && this.ableToAttack($$1);
    }

    @Override
    protected void tick(ServerLevel $$0, PathfinderMob $$1, long $$22) {
        float f;
        LivingEntity $$3 = this.getTarget($$1);
        Entity $$4 = $$1.getRootVehicle();
        if ($$4 instanceof Mob) {
            Mob $$5 = (Mob)$$4;
            f = $$5.chargeSpeedModifier();
        } else {
            f = 1.0f;
        }
        float $$6 = f;
        $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$3, true));
        $$1.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_TIME, $$1.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_TIME).orElse(0) + 1);
        $$1.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_POSITION).ifPresent($$2 -> $$1.getNavigation().moveTo($$2.x, $$2.y, $$2.z, (double)$$6 * this.speedModifierWhenRepositioning));
    }

    @Override
    protected void stop(ServerLevel $$0, PathfinderMob $$1, long $$2) {
        $$1.getNavigation().stop();
        $$1.setAggressive(false);
        $$1.stopUsingItem();
        $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_FLEEING_TIME);
        $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_FLEEING_POSITION);
        $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_STATUS);
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

