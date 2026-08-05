/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.skeleton;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class Bogged
extends AbstractSkeleton
implements Shearable {
    private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(Bogged.class, EntityDataSerializers.BOOLEAN);
    private static final String SHEARED_TAG_NAME = "sheared";
    private static final boolean DEFAULT_SHEARED = false;

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
    }

    public Bogged(EntityType<? extends Bogged> $$0, Level $$1) {
        super((EntityType<? extends AbstractSkeleton>)$$0, $$1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_SHEARED, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean(SHEARED_TAG_NAME, this.isSheared());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setSheared($$0.getBooleanOr(SHEARED_TAG_NAME, false));
    }

    public boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }

    public void setSheared(boolean $$0) {
        this.entityData.set(DATA_SHEARED, $$0);
    }

    @Override
    protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if ($$2.is(Items.SHEARS) && this.readyForShearing()) {
            Level level = this.level();
            if (level instanceof ServerLevel) {
                ServerLevel $$3 = (ServerLevel)level;
                this.shear($$3, SoundSource.PLAYERS, $$2);
                this.gameEvent(GameEvent.SHEAR, $$0);
                $$2.hurtAndBreak(1, (LivingEntity)$$0, $$1.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract($$0, $$1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BOGGED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.BOGGED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BOGGED_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.BOGGED_STEP;
    }

    @Override
    protected AbstractArrow getArrow(ItemStack $$0, float $$1, @Nullable ItemStack $$2) {
        AbstractArrow $$3 = super.getArrow($$0, $$1, $$2);
        if ($$3 instanceof Arrow) {
            Arrow $$4 = (Arrow)$$3;
            $$4.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        }
        return $$3;
    }

    @Override
    protected int getHardAttackInterval() {
        return 50;
    }

    @Override
    protected int getAttackInterval() {
        return 70;
    }

    @Override
    public void shear(ServerLevel $$0, SoundSource $$1, ItemStack $$2) {
        $$0.playSound(null, this, SoundEvents.BOGGED_SHEAR, $$1, 1.0f, 1.0f);
        this.spawnShearedMushrooms($$0, $$2);
        this.setSheared(true);
    }

    private void spawnShearedMushrooms(ServerLevel $$02, ItemStack $$12) {
        this.dropFromShearingLootTable($$02, BuiltInLootTables.BOGGED_SHEAR, $$12, ($$0, $$1) -> this.spawnAtLocation((ServerLevel)$$0, (ItemStack)$$1, this.getBbHeight()));
    }

    @Override
    public boolean readyForShearing() {
        return !this.isSheared() && this.isAlive();
    }
}

