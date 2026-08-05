/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.world.effect;

import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

class InfestedMobEffect
extends MobEffect {
    private final float chanceToSpawn;
    private final ToIntFunction<RandomSource> spawnedCount;

    protected InfestedMobEffect(MobEffectCategory $$0, int $$1, float $$2, ToIntFunction<RandomSource> $$3) {
        super($$0, $$1, ParticleTypes.INFESTED);
        this.chanceToSpawn = $$2;
        this.spawnedCount = $$3;
    }

    @Override
    public void onMobHurt(ServerLevel $$0, LivingEntity $$1, int $$2, DamageSource $$3, float $$4) {
        if ($$1.getRandom().nextFloat() <= this.chanceToSpawn) {
            int $$5 = this.spawnedCount.applyAsInt($$1.getRandom());
            for (int $$6 = 0; $$6 < $$5; ++$$6) {
                this.spawnSilverfish($$0, $$1, $$1.getX(), $$1.getY() + (double)$$1.getBbHeight() / 2.0, $$1.getZ());
            }
        }
    }

    private void spawnSilverfish(ServerLevel $$0, LivingEntity $$1, double $$2, double $$3, double $$4) {
        Silverfish $$5 = EntityType.SILVERFISH.create($$0, EntitySpawnReason.TRIGGERED);
        if ($$5 == null) {
            return;
        }
        RandomSource $$6 = $$1.getRandom();
        float $$7 = 1.5707964f;
        float $$8 = Mth.randomBetween($$6, -1.5707964f, 1.5707964f);
        Vector3f $$9 = $$1.getLookAngle().toVector3f().mul(0.3f).mul(1.0f, 1.5f, 1.0f).rotateY($$8);
        $$5.snapTo($$2, $$3, $$4, $$0.getRandom().nextFloat() * 360.0f, 0.0f);
        $$5.setDeltaMovement(new Vec3((Vector3fc)$$9));
        $$0.addFreshEntity($$5);
        $$5.playSound(SoundEvents.SILVERFISH_HURT);
    }
}

