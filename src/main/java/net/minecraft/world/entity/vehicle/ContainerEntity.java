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
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface ContainerEntity extends Container, MenuProvider {
   Vec3 position();

   AABB getBoundingBox();

   
   ResourceKey<LootTable> getContainerLootTable();

   void setContainerLootTable(ResourceKey<LootTable> var1);

   long getContainerLootTableSeed();

   void setContainerLootTableSeed(long var1);

   NonNullList<ItemStack> getItemStacks();

   void clearItemStacks();

   Level level();

   boolean isRemoved();

   default boolean isEmpty() {
      return this.isChestVehicleEmpty();
   }

   default void addChestVehicleSaveData(ValueOutput $$0) {
      if (this.getContainerLootTable() != null) {
         $$0.putString("LootTable", this.getContainerLootTable().identifier().toString());
         if (this.getContainerLootTableSeed() != 0L) {
            $$0.putLong("LootTableSeed", this.getContainerLootTableSeed());
         }
      } else {
         ContainerHelper.saveAllItems($$0, this.getItemStacks());
      }
   }

   default void readChestVehicleSaveData(ValueInput $$0) {
      this.clearItemStacks();
      ResourceKey<LootTable> $$1 = (ResourceKey<LootTable>)$$0.read("LootTable", LootTable.KEY_CODEC).orElse(null);
      this.setContainerLootTable($$1);
      this.setContainerLootTableSeed($$0.getLongOr("LootTableSeed", 0L));
      if ($$1 == null) {
         ContainerHelper.loadAllItems($$0, this.getItemStacks());
      }
   }

   default void chestVehicleDestroyed(DamageSource $$0, ServerLevel $$1, net.minecraft.world.entity.Entity $$2) {
      if ((Boolean)$$1.getGameRules().get(GameRules.ENTITY_DROPS)) {
         Containers.dropContents($$1, $$2, this);
         net.minecraft.world.entity.Entity $$3 = $$0.getDirectEntity();
         if ($$3 != null && $$3.getType() == net.minecraft.world.entity.EntityType.PLAYER) {
            PiglinAi.angerNearbyPiglins($$1, (Player)$$3, true);
         }
      }
   }

   default InteractionResult interactWithContainerVehicle(Player $$0) {
      $$0.openMenu(this);
      return InteractionResult.SUCCESS;
   }

   default void unpackChestVehicleLootTable(Player $$0) {
      MinecraftServer $$1 = this.level().getServer();
      if (this.getContainerLootTable() != null && $$1 != null) {
         LootTable $$2 = $$1.reloadableRegistries().getLootTable(this.getContainerLootTable());
         if ($$0 != null) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)$$0, this.getContainerLootTable());
         }

         this.setContainerLootTable(null);
         Builder $$3 = new Builder((ServerLevel)this.level()).withParameter(LootContextParams.ORIGIN, this.position());
         if ($$0 != null) {
            $$3.withLuck($$0.getLuck()).withParameter(LootContextParams.THIS_ENTITY, $$0);
         }

         $$2.fill(this, $$3.create(LootContextParamSets.CHEST), this.getContainerLootTableSeed());
      }
   }

   default void clearChestVehicleContent() {
      this.unpackChestVehicleLootTable(null);
      this.getItemStacks().clear();
   }

   default boolean isChestVehicleEmpty() {
      for (ItemStack $$0 : this.getItemStacks()) {
         if (!$$0.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   default ItemStack removeChestVehicleItemNoUpdate(int $$0) {
      this.unpackChestVehicleLootTable(null);
      ItemStack $$1 = (ItemStack)this.getItemStacks().get($$0);
      if ($$1.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.getItemStacks().set($$0, ItemStack.EMPTY);
         return $$1;
      }
   }

   default ItemStack getChestVehicleItem(int $$0) {
      this.unpackChestVehicleLootTable(null);
      return (ItemStack)this.getItemStacks().get($$0);
   }

   default ItemStack removeChestVehicleItem(int $$0, int $$1) {
      this.unpackChestVehicleLootTable(null);
      return ContainerHelper.removeItem(this.getItemStacks(), $$0, $$1);
   }

   default void setChestVehicleItem(int $$0, ItemStack $$1) {
      this.unpackChestVehicleLootTable(null);
      this.getItemStacks().set($$0, $$1);
      $$1.limitSize(this.getMaxStackSize($$1));
   }

   
   default net.minecraft.world.entity.SlotAccess getChestVehicleSlot(final int $$0) {
      return $$0 >= 0 && $$0 < this.getContainerSize() ? new net.minecraft.world.entity.SlotAccess() {
         @Override
         public ItemStack get() {
            return ContainerEntity.this.getChestVehicleItem($$0);
         }

         @Override
         public boolean set(ItemStack $$0x) {
            ContainerEntity.this.setChestVehicleItem($$0, $$0);
            return true;
         }
      } : null;
   }

   default boolean isChestVehicleStillValid(Player $$0) {
      return !this.isRemoved() && $$0.isWithinEntityInteractionRange(this.getBoundingBox(), 4.0);
   }
}
