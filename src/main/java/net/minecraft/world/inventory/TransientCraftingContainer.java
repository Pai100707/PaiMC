package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;

public class TransientCraftingContainer implements net.minecraft.world.inventory.CraftingContainer {
   private final NonNullList<ItemStack> items;
   private final int width;
   private final int height;
   private final net.minecraft.world.inventory.AbstractContainerMenu menu;

   public TransientCraftingContainer(net.minecraft.world.inventory.AbstractContainerMenu $$0, int $$1, int $$2) {
      this($$0, $$1, $$2, NonNullList.withSize($$1 * $$2, ItemStack.EMPTY));
   }

   private TransientCraftingContainer(net.minecraft.world.inventory.AbstractContainerMenu $$0, int $$1, int $$2, NonNullList<ItemStack> $$3) {
      this.items = $$3;
      this.menu = $$0;
      this.width = $$1;
      this.height = $$2;
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for (ItemStack $$0 : this.items) {
         if (!$$0.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int $$0) {
      return $$0 >= this.getContainerSize() ? ItemStack.EMPTY : (ItemStack)this.items.get($$0);
   }

   public ItemStack removeItemNoUpdate(int $$0) {
      return ContainerHelper.takeItem(this.items, $$0);
   }

   public ItemStack removeItem(int $$0, int $$1) {
      ItemStack $$2 = ContainerHelper.removeItem(this.items, $$0, $$1);
      if (!$$2.isEmpty()) {
         this.menu.slotsChanged(this);
      }

      return $$2;
   }

   public void setItem(int $$0, ItemStack $$1) {
      this.items.set($$0, $$1);
      this.menu.slotsChanged(this);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player $$0) {
      return true;
   }

   public void clearContent() {
      this.items.clear();
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public List<ItemStack> getItems() {
      return List.copyOf(this.items);
   }

   @Override
   public void fillStackedContents(StackedItemContents $$0) {
      for (ItemStack $$1 : this.items) {
         $$0.accountSimpleStack($$1);
      }
   }
}
