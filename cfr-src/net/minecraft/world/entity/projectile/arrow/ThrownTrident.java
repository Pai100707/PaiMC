/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile.arrow;

import java.util.Collection;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ThrownTrident
extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BOOLEAN);
    private static final float WATER_INERTIA = 0.99f;
    private static final boolean DEFAULT_DEALT_DAMAGE = false;
    private boolean dealtDamage = false;
    public int clientSideReturnTridentTickCount;

    public ThrownTrident(EntityType<? extends ThrownTrident> $$0, Level $$1) {
        super((EntityType<? extends AbstractArrow>)$$0, $$1);
    }

    public ThrownTrident(Level $$0, LivingEntity $$1, ItemStack $$2) {
        super(EntityType.TRIDENT, $$1, $$0, $$2, null);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem($$2));
        this.entityData.set(ID_FOIL, $$2.hasFoil());
    }

    public ThrownTrident(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        super(EntityType.TRIDENT, $$1, $$2, $$3, $$0, $$4, $$4);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem($$4));
        this.entityData.set(ID_FOIL, $$4.hasFoil());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(ID_LOYALTY, (byte)0);
        $$0.define(ID_FOIL, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        Entity $$0 = this.getOwner();
        byte $$1 = this.entityData.get(ID_LOYALTY);
        if ($$1 > 0 && (this.dealtDamage || this.isNoPhysics()) && $$0 != null) {
            if (!this.isAcceptibleReturnOwner()) {
                Level level = this.level();
                if (level instanceof ServerLevel) {
                    ServerLevel $$2 = (ServerLevel)level;
                    if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                        this.spawnAtLocation($$2, this.getPickupItem(), 0.1f);
                    }
                }
                this.discard();
            } else {
                if (!($$0 instanceof Player) && this.position().distanceTo($$0.getEyePosition()) < (double)$$0.getBbWidth() + 1.0) {
                    this.discard();
                    return;
                }
                this.setNoPhysics(true);
                Vec3 $$3 = $$0.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + $$3.y * 0.015 * (double)$$1, this.getZ());
                double $$4 = 0.05 * (double)$$1;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add($$3.normalize().scale($$4)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0f, 1.0f);
                }
                ++this.clientSideReturnTridentTickCount;
            }
        }
        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity $$0 = this.getOwner();
        if ($$0 == null || !$$0.isAlive()) {
            return false;
        }
        return !($$0 instanceof ServerPlayer) || !$$0.isSpectator();
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    @Override
    protected @Nullable EntityHitResult findHitEntity(Vec3 $$0, Vec3 $$1) {
        if (this.dealtDamage) {
            return null;
        }
        return super.findHitEntity($$0, $$1);
    }

    @Override
    protected Collection<EntityHitResult> findHitEntities(Vec3 $$0, Vec3 $$1) {
        EntityHitResult $$2 = this.findHitEntity($$0, $$1);
        if ($$2 != null) {
            return List.of($$2);
        }
        return List.of();
    }

    @Override
    protected void onHitEntity(EntityHitResult $$0) {
        Entity $$12 = $$0.getEntity();
        float $$2 = 8.0f;
        Entity $$3 = this.getOwner();
        DamageSource $$4 = this.damageSources().trident(this, $$3 == null ? this : $$3);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$5 = (ServerLevel)level;
            $$2 = EnchantmentHelper.modifyDamage($$5, this.getWeaponItem(), $$12, $$4, $$2);
        }
        this.dealtDamage = true;
        if ($$12.hurtOrSimulate($$4, $$2)) {
            if ($$12.getType() == EntityType.ENDERMAN) {
                return;
            }
            level = this.level();
            if (level instanceof ServerLevel) {
                ServerLevel $$6 = (ServerLevel)level;
                EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak($$6, $$12, $$4, this.getWeaponItem(), $$1 -> this.kill($$6));
            }
            if ($$12 instanceof LivingEntity) {
                LivingEntity $$7 = (LivingEntity)$$12;
                this.doKnockback($$7, $$4);
                this.doPostHurtEffects($$7);
            }
        }
        this.deflect(ProjectileDeflection.REVERSE, $$12, this.owner, false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0f, 1.0f);
    }

    @Override
    protected void hitBlockEnchantmentEffects(ServerLevel $$0, BlockHitResult $$12, ItemStack $$2) {
        LivingEntity $$4;
        Vec3 $$3 = $$12.getBlockPos().clampLocationWithin($$12.getLocation());
        Entity entity = this.getOwner();
        EnchantmentHelper.onHitBlock($$0, $$2, entity instanceof LivingEntity ? ($$4 = (LivingEntity)entity) : null, this, null, $$3, $$0.getBlockState($$12.getBlockPos()), $$1 -> this.kill($$0));
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected boolean tryPickup(Player $$0) {
        return super.tryPickup($$0) || this.isNoPhysics() && this.ownedBy($$0) && $$0.getInventory().add(this.getPickupItem());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.TRIDENT);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player $$0) {
        if (this.ownedBy($$0) || this.getOwner() == null) {
            super.playerTouch($$0);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.dealtDamage = $$0.getBooleanOr("DealtDamage", false);
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(this.getPickupItemStackOrigin()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("DealtDamage", this.dealtDamage);
    }

    private byte getLoyaltyFromItem(ItemStack $$0) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            return (byte)Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration($$1, $$0, this), 0, 127);
        }
        return 0;
    }

    @Override
    public void tickDespawn() {
        byte $$0 = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || $$0 <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99f;
    }

    @Override
    public boolean shouldRender(double $$0, double $$1, double $$2) {
        return true;
    }
}

