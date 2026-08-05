/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface ContainerEntity
extends Container,
MenuProvider {
    public Vec3 position();

    public AABB getBoundingBox();

    public @Nullable ResourceKey<LootTable> getContainerLootTable();

    public void setContainerLootTable(@Nullable ResourceKey<LootTable> var1);

    public long getContainerLootTableSeed();

    public void setContainerLootTableSeed(long var1);

    public NonNullList<ItemStack> getItemStacks();

    public void clearItemStacks();

    public Level level();

    public boolean isRemoved();

    @Override
    default public boolean isEmpty() {
        return this.isChestVehicleEmpty();
    }

    default public void addChestVehicleSaveData(ValueOutput $$0) {
        if (this.getContainerLootTable() != null) {
            $$0.putString("LootTable", this.getContainerLootTable().identifier().toString());
            if (this.getContainerLootTableSeed() != 0L) {
                $$0.putLong("LootTableSeed", this.getContainerLootTableSeed());
            }
        } else {
            ContainerHelper.saveAllItems($$0, this.getItemStacks());
        }
    }

    default public void readChestVehicleSaveData(ValueInput $$0) {
        this.clearItemStacks();
        ResourceKey $$1 = $$0.read("LootTable", LootTable.KEY_CODEC).orElse(null);
        this.setContainerLootTable($$1);
        this.setContainerLootTableSeed($$0.getLongOr("LootTableSeed", 0L));
        if ($$1 == null) {
            ContainerHelper.loadAllItems($$0, this.getItemStacks());
        }
    }

    default public void chestVehicleDestroyed(DamageSource $$0, ServerLevel $$1, Entity $$2) {
        if (!$$1.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
            return;
        }
        Containers.dropContents((Level)$$1, $$2, (Container)this);
        Entity $$3 = $$0.getDirectEntity();
        if ($$3 != null && $$3.getType() == EntityType.PLAYER) {
            PiglinAi.angerNearbyPiglins($$1, (Player)$$3, true);
        }
    }

    default public InteractionResult interactWithContainerVehicle(Player $$0) {
        $$0.openMenu(this);
        return InteractionResult.SUCCESS;
    }

    default public void unpackChestVehicleLootTable(@Nullable Player $$0) {
        MinecraftServer $$1 = this.level().getServer();
        if (this.getContainerLootTable() != null && $$1 != null) {
            LootTable $$2 = $$1.reloadableRegistries().getLootTable(this.getContainerLootTable());
            if ($$0 != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)$$0, this.getContainerLootTable());
            }
            this.setContainerLootTable(null);
            LootParams.Builder $$3 = new LootParams.Builder((ServerLevel)this.level()).withParameter(LootContextParams.ORIGIN, this.position());
            if ($$0 != null) {
                $$3.withLuck($$0.getLuck()).withParameter(LootContextParams.THIS_ENTITY, $$0);
            }
            $$2.fill(this, $$3.create(LootContextParamSets.CHEST), this.getContainerLootTableSeed());
        }
    }

    default public void clearChestVehicleContent() {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().clear();
    }

    default public boolean isChestVehicleEmpty() {
        for (ItemStack $$0 : this.getItemStacks()) {
            if ($$0.isEmpty()) continue;
            return false;
        }
        return true;
    }

    default public ItemStack removeChestVehicleItemNoUpdate(int $$0) {
        this.unpackChestVehicleLootTable(null);
        ItemStack $$1 = this.getItemStacks().get($$0);
        if ($$1.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.getItemStacks().set($$0, ItemStack.EMPTY);
        return $$1;
    }

    default public ItemStack getChestVehicleItem(int $$0) {
        this.unpackChestVehicleLootTable(null);
        return this.getItemStacks().get($$0);
    }

    default public ItemStack removeChestVehicleItem(int $$0, int $$1) {
        this.unpackChestVehicleLootTable(null);
        return ContainerHelper.removeItem(this.getItemStacks(), $$0, $$1);
    }

    default public void setChestVehicleItem(int $$0, ItemStack $$1) {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().set($$0, $$1);
        $$1.limitSize(this.getMaxStackSize($$1));
    }

    default public @Nullable SlotAccess getChestVehicleSlot(final int $$0) {
        if ($$0 >= 0 && $$0 < this.getContainerSize()) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return ContainerEntity.this.getChestVehicleItem($$0);
                }

                @Override
                public boolean set(ItemStack $$02) {
                    ContainerEntity.this.setChestVehicleItem($$0, $$02);
                    return true;
                }
            };
        }
        return null;
    }

    default public boolean isChestVehicleStillValid(Player $$0) {
        return !this.isRemoved() && $$0.isWithinEntityInteractionRange(this.getBoundingBox(), 4.0);
    }
}

