/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

class RaidOmenMobEffect
extends MobEffect {
    protected RaidOmenMobEffect(MobEffectCategory $$0, int $$1, ParticleOptions $$2) {
        super($$0, $$1, $$2);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int $$0, int $$1) {
        return $$0 == 1;
    }

    @Override
    public boolean applyEffectTick(ServerLevel $$0, LivingEntity $$1, int $$2) {
        if ($$1 instanceof ServerPlayer) {
            BlockPos $$4;
            ServerPlayer $$3 = (ServerPlayer)$$1;
            if (!$$1.isSpectator() && ($$4 = $$3.getRaidOmenPosition()) != null) {
                $$0.getRaids().createOrExtendRaid($$3, $$4);
                $$3.clearRaidOmenPosition();
                return false;
            }
        }
        return true;
    }
}

