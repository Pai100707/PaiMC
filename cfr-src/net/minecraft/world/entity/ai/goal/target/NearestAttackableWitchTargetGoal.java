/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.raid.Raider;
import org.jspecify.annotations.Nullable;

public class NearestAttackableWitchTargetGoal<T extends LivingEntity>
extends NearestAttackableTargetGoal<T> {
    private boolean canAttack = true;

    public NearestAttackableWitchTargetGoal(Raider $$0, Class<T> $$1, int $$2, boolean $$3, boolean $$4,  @Nullable TargetingConditions.Selector $$5) {
        super($$0, $$1, $$2, $$3, $$4, $$5);
    }

    public void setCanAttack(boolean $$0) {
        this.canAttack = $$0;
    }

    @Override
    public boolean canUse() {
        return this.canAttack && super.canUse();
    }
}

