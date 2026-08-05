/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class EvokerFangs
extends Entity
implements TraceableEntity {
    public static final int ATTACK_DURATION = 20;
    public static final int LIFE_OFFSET = 2;
    public static final int ATTACK_TRIGGER_TICKS = 14;
    private static final int DEFAULT_WARMUP_DELAY = 0;
    private int warmupDelayTicks = 0;
    private boolean sentSpikeEvent;
    private int lifeTicks = 22;
    private boolean clientSideAttackStarted;
    private @Nullable EntityReference<LivingEntity> owner;

    public EvokerFangs(EntityType<? extends EvokerFangs> $$0, Level $$1) {
        super($$0, $$1);
    }

    public EvokerFangs(Level $$0, double $$1, double $$2, double $$3, float $$4, int $$5, LivingEntity $$6) {
        this((EntityType<? extends EvokerFangs>)EntityType.EVOKER_FANGS, $$0);
        this.warmupDelayTicks = $$5;
        this.setOwner($$6);
        this.setYRot($$4 * 57.295776f);
        this.setPos($$1, $$2, $$3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
    }

    public void setOwner(@Nullable LivingEntity $$0) {
        this.owner = EntityReference.of($$0);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.owner, this.level());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.warmupDelayTicks = $$0.getIntOr("Warmup", 0);
        this.owner = EntityReference.read($$0, "Owner");
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.putInt("Warmup", this.warmupDelayTicks);
        EntityReference.store(this.owner, $$0, "Owner");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.clientSideAttackStarted) {
                --this.lifeTicks;
                if (this.lifeTicks == 14) {
                    for (int $$0 = 0; $$0 < 12; ++$$0) {
                        double $$1 = this.getX() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double $$2 = this.getY() + 0.05 + this.random.nextDouble();
                        double $$3 = this.getZ() + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double $$4 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        double $$5 = 0.3 + this.random.nextDouble() * 0.3;
                        double $$6 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        this.level().addParticle(ParticleTypes.CRIT, $$1, $$2 + 1.0, $$3, $$4, $$5, $$6);
                    }
                }
            }
        } else if (--this.warmupDelayTicks < 0) {
            if (this.warmupDelayTicks == -8) {
                List<LivingEntity> $$7 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2, 0.0, 0.2));
                for (LivingEntity $$8 : $$7) {
                    this.dealDamageTo($$8);
                }
            }
            if (!this.sentSpikeEvent) {
                this.level().broadcastEntityEvent(this, (byte)4);
                this.sentSpikeEvent = true;
            }
            if (--this.lifeTicks < 0) {
                this.discard();
            }
        }
    }

    private void dealDamageTo(LivingEntity $$0) {
        LivingEntity $$1 = this.getOwner();
        if (!$$0.isAlive() || $$0.isInvulnerable() || $$0 == $$1) {
            return;
        }
        if ($$1 == null) {
            $$0.hurt(this.damageSources().magic(), 6.0f);
        } else {
            ServerLevel $$3;
            if ($$1.isAlliedTo($$0)) {
                return;
            }
            DamageSource $$2 = this.damageSources().indirectMagic(this, $$1);
            Level level = this.level();
            if (level instanceof ServerLevel && $$0.hurtServer($$3 = (ServerLevel)level, $$2, 6.0f)) {
                EnchantmentHelper.doPostAttackEffects($$3, $$0, $$2);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        super.handleEntityEvent($$0);
        if ($$0 == 4) {
            this.clientSideAttackStarted = true;
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0f, this.random.nextFloat() * 0.2f + 0.85f, false);
            }
        }
    }

    public float getAnimationProgress(float $$0) {
        if (!this.clientSideAttackStarted) {
            return 0.0f;
        }
        int $$1 = this.lifeTicks - 2;
        if ($$1 <= 0) {
            return 1.0f;
        }
        return 1.0f - ((float)$$1 - $$0) / 20.0f;
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        return false;
    }

    @Override
    public /* synthetic */ @Nullable Entity getOwner() {
        return this.getOwner();
    }
}

