/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public class ChargeAttack
extends Behavior<Animal> {
    private final int timeBetweenAttacks;
    private final TargetingConditions chargeTargeting;
    private final float speed;
    private final float knockbackForce;
    private final double maxTargetDetectionDistance;
    private final double maxChargeDistance;
    private final SoundEvent chargeSound;
    private Vec3 chargeVelocityVector;
    private Vec3 startPosition;

    public ChargeAttack(int $$0, TargetingConditions $$1, float $$2, float $$3, double $$4, double $$5, SoundEvent $$6) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.CHARGE_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
        this.timeBetweenAttacks = $$0;
        this.chargeTargeting = $$1;
        this.speed = $$2;
        this.knockbackForce = $$3;
        this.maxChargeDistance = $$4;
        this.maxTargetDetectionDistance = $$5;
        this.chargeSound = $$6;
        this.chargeVelocityVector = Vec3.ZERO;
        this.startPosition = Vec3.ZERO;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Animal $$1) {
        return $$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Animal $$1, long $$2) {
        TamableAnimal $$6;
        Brain<Integer> $$3 = $$1.getBrain();
        Optional<LivingEntity> $$4 = $$3.getMemory(MemoryModuleType.ATTACK_TARGET);
        if ($$4.isEmpty()) {
            return false;
        }
        LivingEntity $$5 = $$4.get();
        if ($$1 instanceof TamableAnimal && ($$6 = (TamableAnimal)$$1).isTame()) {
            return false;
        }
        if ($$1.position().subtract(this.startPosition).lengthSqr() >= this.maxChargeDistance * this.maxChargeDistance) {
            return false;
        }
        if ($$5.position().subtract($$1.position()).lengthSqr() >= this.maxTargetDetectionDistance * this.maxTargetDetectionDistance) {
            return false;
        }
        if (!$$1.hasLineOfSight($$5)) {
            return false;
        }
        return !$$3.hasMemoryValue(MemoryModuleType.CHARGE_COOLDOWN_TICKS);
    }

    @Override
    protected void start(ServerLevel $$0, Animal $$1, long $$2) {
        Brain<?> $$3 = $$1.getBrain();
        this.startPosition = $$1.position();
        LivingEntity $$4 = $$3.getMemory(MemoryModuleType.ATTACK_TARGET).get();
        Vec3 $$5 = $$4.position().subtract($$1.position()).normalize();
        this.chargeVelocityVector = $$5.scale(this.speed);
        if (this.canStillUse($$0, $$1, $$2)) {
            $$1.playSound(this.chargeSound);
        }
    }

    @Override
    protected void tick(ServerLevel $$0, Animal $$1, long $$22) {
        Brain<?> $$3 = $$1.getBrain();
        LivingEntity $$4 = $$3.getMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow();
        $$1.lookAt($$4, 360.0f, 360.0f);
        $$1.setDeltaMovement(this.chargeVelocityVector);
        ArrayList $$5 = new ArrayList(1);
        $$0.getEntities(EntityTypeTest.forClass(LivingEntity.class), $$1.getBoundingBox(), $$2 -> this.chargeTargeting.test($$0, $$1, (LivingEntity)$$2), $$5, 1);
        if (!$$5.isEmpty()) {
            LivingEntity $$6 = (LivingEntity)$$5.get(0);
            if ($$1.hasPassenger($$6)) {
                return;
            }
            this.dealDamageToTarget($$0, $$1, $$6);
            this.dealKnockBack($$1, $$6);
            this.stop($$0, $$1, $$22);
        }
    }

    private void dealDamageToTarget(ServerLevel $$0, Animal $$1, LivingEntity $$2) {
        float $$4;
        DamageSource $$3 = $$0.damageSources().mobAttack($$1);
        if ($$2.hurtServer($$0, $$3, $$4 = (float)$$1.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
            EnchantmentHelper.doPostAttackEffects($$0, $$2, $$3);
        }
    }

    private void dealKnockBack(Animal $$0, LivingEntity $$1) {
        int $$2 = $$0.hasEffect(MobEffects.SPEED) ? $$0.getEffect(MobEffects.SPEED).getAmplifier() + 1 : 0;
        int $$3 = $$0.hasEffect(MobEffects.SLOWNESS) ? $$0.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1 : 0;
        float $$4 = 0.25f * (float)($$2 - $$3);
        float $$5 = Mth.clamp(this.speed * (float)$$0.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.2f, 2.0f) + $$4;
        $$0.causeExtraKnockback($$1, $$5 * this.knockbackForce, $$0.getDeltaMovement());
    }

    @Override
    protected void stop(ServerLevel $$0, Animal $$1, long $$2) {
        $$1.getBrain().setMemory(MemoryModuleType.CHARGE_COOLDOWN_TICKS, this.timeBetweenAttacks);
        $$1.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Animal)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Animal)livingEntity, l);
    }
}

