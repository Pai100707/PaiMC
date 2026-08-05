/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

class RegenerationMobEffect
extends MobEffect {
    protected RegenerationMobEffect(MobEffectCategory $$0, int $$1) {
        super($$0, $$1);
    }

    @Override
    public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
        if ($$1.getHealth() < $$1.getMaxHealth()) {
            $$1.heal(1.0f);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
        int $$2 = 50 >> $$1;
        if ($$2 > 0) {
            return $$0 % $$2 == 0;
        }
        return true;
    }
}

