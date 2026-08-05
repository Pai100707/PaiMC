/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile.hurtingprojectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class Fireball
extends AbstractHurtingProjectile
implements ItemSupplier {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25f;
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Fireball.class, EntityDataSerializers.ITEM_STACK);

    public Fireball(EntityType<? extends Fireball> $$0, Level $$1) {
        super((EntityType<? extends AbstractHurtingProjectile>)$$0, $$1);
    }

    public Fireball(EntityType<? extends Fireball> $$0, double $$1, double $$2, double $$3, Vec3 $$4, Level $$5) {
        super($$0, $$1, $$2, $$3, $$4, $$5);
    }

    public Fireball(EntityType<? extends Fireball> $$0, LivingEntity $$1, Vec3 $$2, Level $$3) {
        super($$0, $$1, $$2, $$3);
    }

    public void setItem(ItemStack $$0) {
        if ($$0.isEmpty()) {
            this.getEntityData().set(DATA_ITEM_STACK, this.getDefaultItem());
        } else {
            this.getEntityData().set(DATA_ITEM_STACK, $$0.copyWithCount(1));
        }
    }

    @Override
    protected void playEntityOnFireExtinguishedSound() {
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_ITEM_STACK, this.getDefaultItem());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("Item", ItemStack.CODEC, this.getItem());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setItem($$0.read("Item", ItemStack.CODEC).orElse(this.getDefaultItem()));
    }

    private ItemStack getDefaultItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    public @Nullable SlotAccess getSlot(int $$0) {
        if ($$0 == 0) {
            return SlotAccess.of(this::getItem, this::setItem);
        }
        return super.getSlot($$0);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double $$0) {
        if (this.tickCount < 2 && $$0 < 12.25) {
            return false;
        }
        return super.shouldRenderAtSqrDistance($$0);
    }
}

