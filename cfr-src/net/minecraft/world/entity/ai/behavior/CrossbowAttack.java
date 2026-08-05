/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

public class CrossbowAttack<E extends Mob, T extends LivingEntity>
extends Behavior<E> {
    private static final int TIMEOUT = 1200;
    private int attackDelay;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;

    public CrossbowAttack() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
        LivingEntity $$2 = CrossbowAttack.getAttackTarget($$1);
        return ((LivingEntity)$$1).isHolding(Items.CROSSBOW) && BehaviorUtils.canSee($$1, $$2) && BehaviorUtils.isWithinAttackRange($$1, $$2, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
        return ((LivingEntity)$$1).getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions($$0, $$1);
    }

    @Override
    protected void tick(ServerLevel $$0, E $$1, long $$2) {
        LivingEntity $$3 = CrossbowAttack.getAttackTarget($$1);
        this.lookAtTarget((Mob)$$1, $$3);
        this.crossbowAttack($$1, $$3);
    }

    @Override
    protected void stop(ServerLevel $$0, E $$1, long $$2) {
        if (((LivingEntity)$$1).isUsingItem()) {
            ((LivingEntity)$$1).stopUsingItem();
        }
        if (((LivingEntity)$$1).isHolding(Items.CROSSBOW)) {
            ((CrossbowAttackMob)$$1).setChargingCrossbow(false);
            ((LivingEntity)$$1).getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    private void crossbowAttack(E $$0, LivingEntity $$1) {
        if (this.crossbowState == CrossbowState.UNCHARGED) {
            ((LivingEntity)$$0).startUsingItem(ProjectileUtil.getWeaponHoldingHand($$0, Items.CROSSBOW));
            this.crossbowState = CrossbowState.CHARGING;
            ((CrossbowAttackMob)$$0).setChargingCrossbow(true);
        } else if (this.crossbowState == CrossbowState.CHARGING) {
            ItemStack $$3;
            int $$2;
            if (!((LivingEntity)$$0).isUsingItem()) {
                this.crossbowState = CrossbowState.UNCHARGED;
            }
            if (($$2 = ((LivingEntity)$$0).getTicksUsingItem()) >= CrossbowItem.getChargeDuration($$3 = ((LivingEntity)$$0).getUseItem(), $$0)) {
                ((LivingEntity)$$0).releaseUsingItem();
                this.crossbowState = CrossbowState.CHARGED;
                this.attackDelay = 20 + ((Entity)$$0).getRandom().nextInt(20);
                ((CrossbowAttackMob)$$0).setChargingCrossbow(false);
            }
        } else if (this.crossbowState == CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.crossbowState = CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK) {
            ((RangedAttackMob)$$0).performRangedAttack($$1, 1.0f);
            this.crossbowState = CrossbowState.UNCHARGED;
        }
    }

    private void lookAtTarget(Mob $$0, LivingEntity $$1) {
        $$0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$1, true));
    }

    private static LivingEntity getAttackTarget(LivingEntity $$0) {
        return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (E)((Mob)livingEntity), l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (E)((Mob)livingEntity), l);
    }

    static enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;

    }
}

