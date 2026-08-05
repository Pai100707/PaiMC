/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class AreaEffectCloud
extends Entity
implements TraceableEntity {
    private static final int TIME_BETWEEN_APPLICATIONS = 5;
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private static final float MAX_RADIUS = 32.0f;
    private static final int DEFAULT_AGE = 0;
    private static final int DEFAULT_DURATION_ON_USE = 0;
    private static final float DEFAULT_RADIUS_ON_USE = 0.0f;
    private static final float DEFAULT_RADIUS_PER_TICK = 0.0f;
    private static final float DEFAULT_POTION_DURATION_SCALE = 1.0f;
    private static final float MINIMAL_RADIUS = 0.5f;
    private static final float DEFAULT_RADIUS = 3.0f;
    public static final float DEFAULT_WIDTH = 6.0f;
    public static final float HEIGHT = 0.5f;
    public static final int INFINITE_DURATION = -1;
    public static final int DEFAULT_LINGERING_DURATION = 600;
    private static final int DEFAULT_WAIT_TIME = 20;
    private static final int DEFAULT_REAPPLICATION_DELAY = 20;
    private static final ColorParticleOption DEFAULT_PARTICLE = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1);
    private @Nullable ParticleOptions customParticle;
    private PotionContents potionContents = PotionContents.EMPTY;
    private float potionDurationScale = 1.0f;
    private final Map<Entity, Integer> victims = Maps.newHashMap();
    private int duration = -1;
    private int waitTime = 20;
    private int reapplicationDelay = 20;
    private int durationOnUse = 0;
    private float radiusOnUse = 0.0f;
    private float radiusPerTick = 0.0f;
    private @Nullable EntityReference<LivingEntity> owner;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> $$0, Level $$1) {
        super($$0, $$1);
        this.noPhysics = true;
    }

    public AreaEffectCloud(Level $$0, double $$1, double $$2, double $$3) {
        this((EntityType<? extends AreaEffectCloud>)EntityType.AREA_EFFECT_CLOUD, $$0);
        this.setPos($$1, $$2, $$3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_RADIUS, Float.valueOf(3.0f));
        $$0.define(DATA_WAITING, false);
        $$0.define(DATA_PARTICLE, DEFAULT_PARTICLE);
    }

    public void setRadius(float $$0) {
        if (!this.level().isClientSide()) {
            this.getEntityData().set(DATA_RADIUS, Float.valueOf(Mth.clamp($$0, 0.0f, 32.0f)));
        }
    }

    @Override
    public void refreshDimensions() {
        double $$0 = this.getX();
        double $$1 = this.getY();
        double $$2 = this.getZ();
        super.refreshDimensions();
        this.setPos($$0, $$1, $$2);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS).floatValue();
    }

    public void setPotionContents(PotionContents $$0) {
        this.potionContents = $$0;
        this.updateParticle();
    }

    public void setCustomParticle(@Nullable ParticleOptions $$0) {
        this.customParticle = $$0;
        this.updateParticle();
    }

    public void setPotionDurationScale(float $$0) {
        this.potionDurationScale = $$0;
    }

    private void updateParticle() {
        if (this.customParticle != null) {
            this.entityData.set(DATA_PARTICLE, this.customParticle);
        } else {
            int $$0 = ARGB.opaque(this.potionContents.getColor());
            this.entityData.set(DATA_PARTICLE, ColorParticleOption.create(DEFAULT_PARTICLE.getType(), $$0));
        }
    }

    public void addEffect(MobEffectInstance $$0) {
        this.setPotionContents(this.potionContents.withEffectAdded($$0));
    }

    public ParticleOptions getParticle() {
        return this.getEntityData().get(DATA_PARTICLE);
    }

    protected void setWaiting(boolean $$0) {
        this.getEntityData().set(DATA_WAITING, $$0);
    }

    public boolean isWaiting() {
        return this.getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int $$0) {
        this.duration = $$0;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            this.serverTick($$0);
        } else {
            this.clientTick();
        }
    }

    private void clientTick() {
        float $$6;
        int $$5;
        boolean $$0 = this.isWaiting();
        float $$1 = this.getRadius();
        if ($$0 && this.random.nextBoolean()) {
            return;
        }
        ParticleOptions $$2 = this.getParticle();
        if ($$0) {
            int $$3 = 2;
            float $$4 = 0.2f;
        } else {
            $$5 = Mth.ceil((float)Math.PI * $$1 * $$1);
            $$6 = $$1;
        }
        for (int $$7 = 0; $$7 < $$5; ++$$7) {
            float $$8 = this.random.nextFloat() * ((float)Math.PI * 2);
            float $$9 = Mth.sqrt(this.random.nextFloat()) * $$6;
            double $$10 = this.getX() + (double)(Mth.cos($$8) * $$9);
            double $$11 = this.getY();
            double $$12 = this.getZ() + (double)(Mth.sin($$8) * $$9);
            if ($$2.getType() == ParticleTypes.ENTITY_EFFECT) {
                if ($$0 && this.random.nextBoolean()) {
                    this.level().addAlwaysVisibleParticle(DEFAULT_PARTICLE, $$10, $$11, $$12, 0.0, 0.0, 0.0);
                    continue;
                }
                this.level().addAlwaysVisibleParticle($$2, $$10, $$11, $$12, 0.0, 0.0, 0.0);
                continue;
            }
            if ($$0) {
                this.level().addAlwaysVisibleParticle($$2, $$10, $$11, $$12, 0.0, 0.0, 0.0);
                continue;
            }
            this.level().addAlwaysVisibleParticle($$2, $$10, $$11, $$12, (0.5 - this.random.nextDouble()) * 0.15, 0.01f, (0.5 - this.random.nextDouble()) * 0.15);
        }
    }

    private void serverTick(ServerLevel $$02) {
        boolean $$2;
        if (this.duration != -1 && this.tickCount - this.waitTime >= this.duration) {
            this.discard();
            return;
        }
        boolean $$1 = this.isWaiting();
        boolean bl = $$2 = this.tickCount < this.waitTime;
        if ($$1 != $$2) {
            this.setWaiting($$2);
        }
        if ($$2) {
            return;
        }
        float $$3 = this.getRadius();
        if (this.radiusPerTick != 0.0f) {
            if (($$3 += this.radiusPerTick) < 0.5f) {
                this.discard();
                return;
            }
            this.setRadius($$3);
        }
        if (this.tickCount % 5 == 0) {
            this.victims.entrySet().removeIf($$0 -> this.tickCount >= (Integer)$$0.getValue());
            if (!this.potionContents.hasEffects()) {
                this.victims.clear();
            } else {
                ArrayList $$4 = new ArrayList();
                this.potionContents.forEachEffect($$4::add, this.potionDurationScale);
                List<LivingEntity> $$5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
                if (!$$5.isEmpty()) {
                    for (LivingEntity $$6 : $$5) {
                        double $$8;
                        double $$7;
                        double $$9;
                        if (this.victims.containsKey($$6) || !$$6.isAffectedByPotions()) continue;
                        if ($$4.stream().noneMatch($$6::canBeAffected) || !(($$9 = ($$7 = $$6.getX() - this.getX()) * $$7 + ($$8 = $$6.getZ() - this.getZ()) * $$8) <= (double)($$3 * $$3))) continue;
                        this.victims.put($$6, this.tickCount + this.reapplicationDelay);
                        for (MobEffectInstance $$10 : $$4) {
                            if ($$10.getEffect().value().isInstantenous()) {
                                $$10.getEffect().value().applyInstantenousEffect($$02, this, this.getOwner(), $$6, $$10.getAmplifier(), 0.5);
                                continue;
                            }
                            $$6.addEffect(new MobEffectInstance($$10), this);
                        }
                        if (this.radiusOnUse != 0.0f) {
                            if (($$3 += this.radiusOnUse) < 0.5f) {
                                this.discard();
                                return;
                            }
                            this.setRadius($$3);
                        }
                        if (this.durationOnUse == 0 || this.duration == -1) continue;
                        this.duration += this.durationOnUse;
                        if (this.duration > 0) continue;
                        this.discard();
                        return;
                    }
                }
            }
        }
    }

    public float getRadiusOnUse() {
        return this.radiusOnUse;
    }

    public void setRadiusOnUse(float $$0) {
        this.radiusOnUse = $$0;
    }

    public float getRadiusPerTick() {
        return this.radiusPerTick;
    }

    public void setRadiusPerTick(float $$0) {
        this.radiusPerTick = $$0;
    }

    public int getDurationOnUse() {
        return this.durationOnUse;
    }

    public void setDurationOnUse(int $$0) {
        this.durationOnUse = $$0;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int $$0) {
        this.waitTime = $$0;
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
        this.tickCount = $$0.getIntOr("Age", 0);
        this.duration = $$0.getIntOr("Duration", -1);
        this.waitTime = $$0.getIntOr("WaitTime", 20);
        this.reapplicationDelay = $$0.getIntOr("ReapplicationDelay", 20);
        this.durationOnUse = $$0.getIntOr("DurationOnUse", 0);
        this.radiusOnUse = $$0.getFloatOr("RadiusOnUse", 0.0f);
        this.radiusPerTick = $$0.getFloatOr("RadiusPerTick", 0.0f);
        this.setRadius($$0.getFloatOr("Radius", 3.0f));
        this.owner = EntityReference.read($$0, "Owner");
        this.setCustomParticle($$0.read("custom_particle", ParticleTypes.CODEC).orElse(null));
        this.setPotionContents($$0.read("potion_contents", PotionContents.CODEC).orElse(PotionContents.EMPTY));
        this.potionDurationScale = $$0.getFloatOr("potion_duration_scale", 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.putInt("Age", this.tickCount);
        $$0.putInt("Duration", this.duration);
        $$0.putInt("WaitTime", this.waitTime);
        $$0.putInt("ReapplicationDelay", this.reapplicationDelay);
        $$0.putInt("DurationOnUse", this.durationOnUse);
        $$0.putFloat("RadiusOnUse", this.radiusOnUse);
        $$0.putFloat("RadiusPerTick", this.radiusPerTick);
        $$0.putFloat("Radius", this.getRadius());
        $$0.storeNullable("custom_particle", ParticleTypes.CODEC, this.customParticle);
        EntityReference.store(this.owner, $$0, "Owner");
        if (!this.potionContents.equals(PotionContents.EMPTY)) {
            $$0.store("potion_contents", PotionContents.CODEC, this.potionContents);
        }
        if (this.potionDurationScale != 1.0f) {
            $$0.putFloat("potion_duration_scale", this.potionDurationScale);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (DATA_RADIUS.equals($$0)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated($$0);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public EntityDimensions getDimensions(Pose $$0) {
        return EntityDimensions.scalable(this.getRadius() * 2.0f, 0.5f);
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        return false;
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.POTION_CONTENTS) {
            return AreaEffectCloud.castComponentValue($$0, this.potionContents);
        }
        if ($$0 == DataComponents.POTION_DURATION_SCALE) {
            return AreaEffectCloud.castComponentValue($$0, Float.valueOf(this.potionDurationScale));
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.POTION_CONTENTS);
        this.applyImplicitComponentIfPresent($$0, DataComponents.POTION_DURATION_SCALE);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.POTION_CONTENTS) {
            this.setPotionContents(AreaEffectCloud.castComponentValue(DataComponents.POTION_CONTENTS, $$1));
            return true;
        }
        if ($$0 == DataComponents.POTION_DURATION_SCALE) {
            this.setPotionDurationScale(AreaEffectCloud.castComponentValue(DataComponents.POTION_DURATION_SCALE, $$1).floatValue());
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public /* synthetic */ @Nullable Entity getOwner() {
        return this.getOwner();
    }
}

