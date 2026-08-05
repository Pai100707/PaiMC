/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Shoot
extends Behavior<Breeze> {
    private static final int ATTACK_RANGE_MAX_SQRT = 256;
    private static final int UNCERTAINTY_BASE = 5;
    private static final int UNCERTAINTY_MULTIPLIER = 4;
    private static final float PROJECTILE_MOVEMENT_SCALE = 0.7f;
    private static final int SHOOT_INITIAL_DELAY_TICKS = Math.round(15.0f);
    private static final int SHOOT_RECOVER_DELAY_TICKS = Math.round(4.0f);
    private static final int SHOOT_COOLDOWN_TICKS = Math.round(10.0f);

    @VisibleForTesting
    public Shoot() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.BREEZE_SHOOT_COOLDOWN, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT_CHARGING, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT_RECOVERING, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.BREEZE_JUMP_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)), SHOOT_INITIAL_DELAY_TICKS + 1 + SHOOT_RECOVER_DELAY_TICKS);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Breeze $$12) {
        if ($$12.getPose() != Pose.STANDING) {
            return false;
        }
        return $$12.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map($$1 -> Shoot.isTargetWithinRange($$12, $$1)).map($$1 -> {
            if (!$$1.booleanValue()) {
                $$12.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
            }
            return $$1;
        }).orElse(false);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Breeze $$1, long $$2) {
        return $$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && $$1.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void start(ServerLevel $$0, Breeze $$12, long $$2) {
        $$12.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent($$1 -> $$12.setPose(Pose.SHOOTING));
        $$12.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, SHOOT_INITIAL_DELAY_TICKS);
        $$12.playSound(SoundEvents.BREEZE_INHALE, 1.0f, 1.0f);
    }

    @Override
    protected void stop(ServerLevel $$0, Breeze $$1, long $$2) {
        if ($$1.getPose() == Pose.SHOOTING) {
            $$1.setPose(Pose.STANDING);
        }
        $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, SHOOT_COOLDOWN_TICKS);
        $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void tick(ServerLevel $$0, Breeze $$1, long $$2) {
        Brain<Breeze> $$3 = $$1.getBrain();
        LivingEntity $$4 = $$3.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if ($$4 == null) {
            return;
        }
        $$1.lookAt(EntityAnchorArgument.Anchor.EYES, $$4.position());
        if ($$3.getMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() || $$3.getMemory(MemoryModuleType.BREEZE_SHOOT_RECOVERING).isPresent()) {
            return;
        }
        $$3.setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_RECOVERING, Unit.INSTANCE, SHOOT_RECOVER_DELAY_TICKS);
        double $$5 = $$4.getX() - $$1.getX();
        double $$6 = $$4.getY($$4.isPassenger() ? 0.8 : 0.3) - $$1.getFiringYPosition();
        double $$7 = $$4.getZ() - $$1.getZ();
        Projectile.spawnProjectileUsingShoot(new BreezeWindCharge($$1, (Level)$$0), $$0, ItemStack.EMPTY, $$5, $$6, $$7, 0.7f, 5 - $$0.getDifficulty().getId() * 4);
        $$1.playSound(SoundEvents.BREEZE_SHOOT, 1.5f, 1.0f);
    }

    private static boolean isTargetWithinRange(Breeze $$0, LivingEntity $$1) {
        double $$2 = $$0.position().distanceToSqr($$1.position());
        return $$2 < 256.0;
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Breeze)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Breeze)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Breeze)livingEntity, l);
    }
}

