package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity implements RandomizableContainer {
   
   protected ResourceKey<LootTable> lootTable;
   protected long lootTableSeed = 0L;

   protected RandomizableContainerBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
      super($$0, $$1, $$2);
   }

   
   public ResourceKey<LootTable> getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(ResourceKey<LootTable> $$0) {
      this.lootTable = $$0;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long $$0) {
      this.lootTableSeed = $$0;
   }

   @Override
   public boolean isEmpty() {
      this.unpackLootTable(null);
      return super.isEmpty();
   }

   @Override
   public ItemStack getItem(int $$0) {
      this.unpackLootTable(null);
      return super.getItem($$0);
   }

   @Override
   public ItemStack removeItem(int $$0, int $$1) {
      this.unpackLootTable(null);
      return super.removeItem($$0, $$1);
   }

   @Override
   public ItemStack removeItemNoUpdate(int $$0) {
      this.unpackLootTable(null);
      return super.removeItemNoUpdate($$0);
   }

   @Override
   public void setItem(int $$0, ItemStack $$1) {
      this.unpackLootTable(null);
      super.setItem($$0, $$1);
   }

   @Override
   public boolean canOpen(Player $$0) {
      return super.canOpen($$0) && (this.lootTable == null || !$$0.isSpectator());
   }

   
   @Override
   public AbstractContainerMenu createMenu(int $$0, Inventory $$1, Player $$2) {
      if (this.canOpen($$2)) {
         this.unpackLootTable($$1.player);
         return this.createMenu($$0, $$1);
      } else {
         BaseContainerBlockEntity.sendChestLockedNotifications(this.getBlockPos().getCenter(), $$2, this.getDisplayName());
         return null;
      }
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      SeededContainerLoot $$1 = (SeededContainerLoot)$$0.get(DataComponents.CONTAINER_LOOT);
      if ($$1 != null) {
         this.lootTable = $$1.lootTable();
         this.lootTableSeed = $$1.seed();
      }
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      if (this.lootTable != null) {
         $$0.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.lootTable, this.lootTableSeed));
      }
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      super.removeComponentsFromTag($$0);
      $$0.discard("LootTable");
      $$0.discard("LootTableSeed");
   }
}
