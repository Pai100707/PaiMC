/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile.throwableitemprojectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class ThrowableItemProjectile
extends ThrowableProjectile
implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrowableItemProjectile.class, EntityDataSerializers.ITEM_STACK);

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> $$0, Level $$1) {
        super((EntityType<? extends ThrowableProjectile>)$$0, $$1);
    }

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> $$0, double $$1, double $$2, double $$3, Level $$4, ItemStack $$5) {
        super($$0, $$1, $$2, $$3, $$4);
        this.setItem($$5);
    }

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> $$0, LivingEntity $$1, Level $$2, ItemStack $$3) {
        this($$0, $$1.getX(), $$1.getEyeY() - (double)0.1f, $$1.getZ(), $$2, $$3);
        this.setOwner($$1);
    }

    public void setItem(ItemStack $$0) {
        this.getEntityData().set(DATA_ITEM_STACK, $$0.copyWithCount(1));
    }

    protected abstract Item getDefaultItem();

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_ITEM_STACK, new ItemStack(this.getDefaultItem()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("Item", ItemStack.CODEC, this.getItem());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setItem($$0.read("Item", ItemStack.CODEC).orElseGet(() -> new ItemStack(this.getDefaultItem())));
    }
}

