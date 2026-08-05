/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PoisonMobEffect
extends MobEffect {
    public static final int DAMAGE_INTERVAL = 25;

    protected PoisonMobEffect(MobEffectCategory $$0, int $$1) {
        super($$0, $$1);
    }

    @Override
    public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
        if ($$1.getHealth() > 1.0f) {
            $$1.hurtServer($$0, $$1.damageSources().magic(), 1.0f);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
        int $$2 = 25 >> $$1;
        if ($$2 > 0) {
            return $$0 % $$2 == 0;
        }
        return true;
    }
}

