/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile.arrow;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class Arrow
extends AbstractArrow {
    private static final int EXPOSED_POTION_DECAY_TIME = 600;
    private static final int NO_EFFECT_COLOR = -1;
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
    private static final byte EVENT_POTION_PUFF = 0;

    public Arrow(EntityType<? extends Arrow> $$0, Level $$1) {
        super((EntityType<? extends AbstractArrow>)$$0, $$1);
    }

    public Arrow(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4, @Nullable ItemStack $$5) {
        super(EntityType.ARROW, $$1, $$2, $$3, $$0, $$4, $$5);
        this.updateColor();
    }

    public Arrow(Level $$0, LivingEntity $$1, ItemStack $$2, @Nullable ItemStack $$3) {
        super(EntityType.ARROW, $$1, $$0, $$2, $$3);
        this.updateColor();
    }

    private PotionContents getPotionContents() {
        return this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    private float getPotionDurationScale() {
        return this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue();
    }

    private void setPotionContents(PotionContents $$0) {
        this.getPickupItemStackOrigin().set(DataComponents.POTION_CONTENTS, $$0);
        this.updateColor();
    }

    @Override
    protected void setPickupItemStack(ItemStack $$0) {
        super.setPickupItemStack($$0);
        this.updateColor();
    }

    private void updateColor() {
        PotionContents $$0 = this.getPotionContents();
        this.entityData.set(ID_EFFECT_COLOR, $$0.equals(PotionContents.EMPTY) ? -1 : $$0.getColor());
    }

    public void addEffect(MobEffectInstance $$0) {
        this.setPotionContents(this.getPotionContents().withEffectAdded($$0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(ID_EFFECT_COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.isInGround()) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }
        } else if (this.isInGround() && this.inGroundTime != 0 && !this.getPotionContents().equals(PotionContents.EMPTY) && this.inGroundTime >= 600) {
            this.level().broadcastEntityEvent(this, (byte)0);
            this.setPickupItemStack(new ItemStack(Items.ARROW));
        }
    }

    private void makeParticle(int $$0) {
        int $$1 = this.getColor();
        if ($$1 == -1 || $$0 <= 0) {
            return;
        }
        for (int $$2 = 0; $$2 < $$0; ++$$2) {
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, $$1), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
        }
    }

    public int getColor() {
        return this.entityData.get(ID_EFFECT_COLOR);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity $$0) {
        super.doPostHurtEffects($$0);
        Entity $$1 = this.getEffectSource();
        PotionContents $$22 = this.getPotionContents();
        float $$3 = this.getPotionDurationScale();
        $$22.forEachEffect($$2 -> $$0.addEffect((MobEffectInstance)$$2, $$1), $$3);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 0) {
            int $$1 = this.getColor();
            if ($$1 != -1) {
                float $$2 = (float)($$1 >> 16 & 0xFF) / 255.0f;
                float $$3 = (float)($$1 >> 8 & 0xFF) / 255.0f;
                float $$4 = (float)($$1 >> 0 & 0xFF) / 255.0f;
                for (int $$5 = 0; $$5 < 20; ++$$5) {
                    this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, $$2, $$3, $$4), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
                }
            }
        } else {
            super.handleEntityEvent($$0);
        }
    }
}

