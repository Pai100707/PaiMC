package net.minecraft.world.entity.vehicle.boat;

import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public abstract class AbstractChestBoat extends AbstractBoat implements net.minecraft.world.entity.HasCustomInventoryScreen, ContainerEntity {
   private static final int CONTAINER_SIZE = 27;
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
   @Nullable
   private ResourceKey<LootTable> lootTable;
   private long lootTableSeed;

   public AbstractChestBoat(net.minecraft.world.entity.EntityType<? extends AbstractChestBoat> $$0, Level $$1, Supplier<Item> $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected float getSinglePassengerXOffset() {
      return 0.15F;
   }

   @Override
   protected int getMaxPassengers() {
      return 1;
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
   public void destroy(ServerLevel $$0, DamageSource $$1) {
      this.destroy($$0, this.getDropItem());
      this.chestVehicleDestroyed($$1, $$0, this);
   }

   @Override
   public void remove(net.minecraft.world.entity.Entity.RemovalReason $$0) {
      if (!this.level().isClientSide() && $$0.shouldDestroy()) {
         Containers.dropContents(this.level(), this, this);
      }

      super.remove($$0);
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      InteractionResult $$2 = super.interact($$0, $$1);
      if ($$2 != InteractionResult.PASS) {
         return $$2;
      } else if (this.canAddPassenger($$0) && !$$0.isSecondaryUseActive()) {
         return InteractionResult.PASS;
      } else {
         InteractionResult $$3 = this.interactWithContainerVehicle($$0);
         if ($$3.consumesAction() && $$0.level() instanceof ServerLevel $$4) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, $$0);
            PiglinAi.angerNearbyPiglins($$4, $$0, true);
         }

         return $$3;
      }
   }

   @Override
   public void openCustomInventoryScreen(Player $$0) {
      $$0.openMenu(this);
      if ($$0.level() instanceof ServerLevel $$1) {
         this.gameEvent(GameEvent.CONTAINER_OPEN, $$0);
         PiglinAi.angerNearbyPiglins($$1, $$0, true);
      }
   }

   public void clearContent() {
      this.clearChestVehicleContent();
   }

   public int getContainerSize() {
      return 27;
   }

   public ItemStack getItem(int $$0) {
      return this.getChestVehicleItem($$0);
   }

   public ItemStack removeItem(int $$0, int $$1) {
      return this.removeChestVehicleItem($$0, $$1);
   }

   public ItemStack removeItemNoUpdate(int $$0) {
      return this.removeChestVehicleItemNoUpdate($$0);
   }

   public void setItem(int $$0, ItemStack $$1) {
      this.setChestVehicleItem($$0, $$1);
   }

   @Override
   public net.minecraft.world.entity.SlotAccess getSlot(int $$0) {
      return this.getChestVehicleSlot($$0);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player $$0) {
      return this.isChestVehicleStillValid($$0);
   }

   @Nullable
   public AbstractContainerMenu createMenu(int $$0, Inventory $$1, Player $$2) {
      if (this.lootTable != null && $$2.isSpectator()) {
         return null;
      } else {
         this.unpackLootTable($$1.player);
         return ChestMenu.threeRows($$0, $$1, this);
      }
   }

   public void unpackLootTable(@Nullable Player $$0) {
      this.unpackChestVehicleLootTable($$0);
   }

   @Nullable
   @Override
   public ResourceKey<LootTable> getContainerLootTable() {
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

   public void stopOpen(net.minecraft.world.entity.ContainerUser $$0) {
      this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), Context.of($$0.getLivingEntity()));
   }
}
