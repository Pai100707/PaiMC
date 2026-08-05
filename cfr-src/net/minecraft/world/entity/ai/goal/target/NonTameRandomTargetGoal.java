/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jspecify.annotations.Nullable;

public class NonTameRandomTargetGoal<T extends LivingEntity>
extends NearestAttackableTargetGoal<T> {
    private final TamableAnimal tamableMob;

    public NonTameRandomTargetGoal(TamableAnimal $$0, Class<T> $$1, boolean $$2,  @Nullable TargetingConditions.Selector $$3) {
        super($$0, $$1, 10, $$2, false, $$3);
        this.tamableMob = $$0;
    }

    @Override
    public boolean canUse() {
        return !this.tamableMob.isTame() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetConditions != null) {
            return this.targetConditions.test(NonTameRandomTargetGoal.getServerLevel(this.mob), this.mob, this.target);
        }
        return super.canContinueToUse();
    }
}

