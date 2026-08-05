/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractMinecartContainer
extends AbstractMinecart
implements ContainerEntity {
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
    private @Nullable ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    protected AbstractMinecartContainer(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public void destroy(ServerLevel $$0, DamageSource $$1) {
        super.destroy($$0, $$1);
        this.chestVehicleDestroyed($$1, $$0, this);
    }

    @Override
    public ItemStack getItem(int $$0) {
        return this.getChestVehicleItem($$0);
    }

    @Override
    public ItemStack removeItem(int $$0, int $$1) {
        return this.removeChestVehicleItem($$0, $$1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int $$0) {
        return this.removeChestVehicleItemNoUpdate($$0);
    }

    @Override
    public void setItem(int $$0, ItemStack $$1) {
        this.setChestVehicleItem($$0, $$1);
    }

    @Override
    public SlotAccess getSlot(int $$0) {
        return this.getChestVehicleSlot($$0);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player $$0) {
        return this.isChestVehicleStillValid($$0);
    }

    @Override
    public void remove(Entity.RemovalReason $$0) {
        if (!this.level().isClientSide() && $$0.shouldDestroy()) {
            Containers.dropContents(this.level(), this, (Container)this);
        }
        super.remove($$0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        this.addChestVehicleSaveData($$0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.readChestVehicleSaveData($$0);
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        return this.interactWithContainerVehicle($$0);
    }

    @Override
    protected Vec3 applyNaturalSlowdown(Vec3 $$0) {
        float $$1 = 0.98f;
        if (this.lootTable == null) {
            int $$2 = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
            $$1 += (float)$$2 * 0.001f;
        }
        if (this.isInWater()) {
            $$1 *= 0.95f;
        }
        return $$0.multiply($$1, 0.0, $$1);
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    public void setLootTable(ResourceKey<LootTable> $$0, long $$1) {
        this.lootTable = $$0;
        this.lootTableSeed = $$1;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int $$0, Inventory $$1, Player $$2) {
        if (this.lootTable == null || !$$2.isSpectator()) {
            this.unpackChestVehicleLootTable($$1.player);
            return this.createMenu($$0, $$1);
        }
        return null;
    }

    protected abstract AbstractContainerMenu createMenu(int var1, Inventory var2);

    @Override
    public @Nullable ResourceKey<LootTable> getContainerLootTable() {
        return this.lootTable;
    }

    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> $$0) {
        this.lootTable = $$0;
    }

    @Override
    public long getContainerLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setContainerLootTableSeed(long $$0) {
        this.lootTableSeed = $$0;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }
}

