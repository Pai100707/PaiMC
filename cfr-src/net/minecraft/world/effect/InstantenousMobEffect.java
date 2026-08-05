/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class InstantenousMobEffect
extends MobEffect {
    public InstantenousMobEffect(MobEffectCategory $$0, int $$1) {
        super($$0, $$1);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
        return $$0 >= 1;
    }
}

