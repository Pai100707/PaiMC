package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput.TypedInputList;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;

public class SimpleContainer implements Container, StackedContentsCompatible {
   private final int size;
   private final NonNullList<ItemStack> items;
   
   private List<ContainerListener> listeners;

   public SimpleContainer(int $$0) {
      this.size = $$0;
      this.items = NonNullList.withSize($$0, ItemStack.EMPTY);
   }

   public SimpleContainer(ItemStack... $$0) {
      this.size = $$0.length;
      this.items = NonNullList.of(ItemStack.EMPTY, $$0);
   }

   public void addListener(ContainerListener $$0) {
      if (this.listeners == null) {
         this.listeners = Lists.newArrayList();
      }

      this.listeners.add($$0);
   }

   public void removeListener(ContainerListener $$0) {
      if (this.listeners != null) {
         this.listeners.remove($$0);
      }
   }

   @Override
   public ItemStack getItem(int $$0) {
      return $$0 >= 0 && $$0 < this.items.size() ? (ItemStack)this.items.get($$0) : ItemStack.EMPTY;
   }

   public List<ItemStack> removeAllItems() {
      List<ItemStack> $$0 = this.items.stream().filter($$0x -> !$$0x.isEmpty()).collect(Collectors.toList());
      this.clearContent();
      return $$0;
   }

   @Override
   public ItemStack removeItem(int $$0, int $$1) {
      ItemStack $$2 = ContainerHelper.removeItem(this.items, $$0, $$1);
      if (!$$2.isEmpty()) {
         this.setChanged();
      }

      return $$2;
   }

   public ItemStack removeItemType(Item $$0, int $$1) {
      ItemStack $$2 = new ItemStack($$0, 0);

      for (int $$3 = this.size - 1; $$3 >= 0; $$3--) {
         ItemStack $$4 = this.getItem($$3);
         if ($$4.getItem().equals($$0)) {
            int $$5 = $$1 - $$2.getCount();
            ItemStack $$6 = $$4.split($$5);
            $$2.grow($$6.getCount());
            if ($$2.getCount() == $$1) {
               break;
            }
         }
      }

      if (!$$2.isEmpty()) {
         this.setChanged();
      }

      return $$2;
   }

   public ItemStack addItem(ItemStack $$0) {
      if ($$0.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack $$1 = $$0.copy();
         this.moveItemToOccupiedSlotsWithSameType($$1);
         if ($$1.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            this.moveItemToEmptySlots($$1);
            return $$1.isEmpty() ? ItemStack.EMPTY : $$1;
         }
      }
   }

   public boolean canAddItem(ItemStack $$0) {
      boolean $$1 = false;

      for (ItemStack $$2 : this.items) {
         if ($$2.isEmpty() || ItemStack.isSameItemSameComponents($$2, $$0) && $$2.getCount() < $$2.getMaxStackSize()) {
            $$1 = true;
            break;
         }
      }

      return $$1;
   }

   @Override
   public ItemStack removeItemNoUpdate(int $$0) {
      ItemStack $$1 = (ItemStack)this.items.get($$0);
      if ($$1.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.items.set($$0, ItemStack.EMPTY);
         return $$1;
      }
   }

   @Override
   public void setItem(int $$0, ItemStack $$1) {
      this.items.set($$0, $$1);
      $$1.limitSize(this.getMaxStackSize($$1));
      this.setChanged();
   }

   @Override
   public int getContainerSize() {
      return this.size;
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack $$0 : this.items) {
         if (!$$0.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void setChanged() {
      if (this.listeners != null) {
         for (ContainerListener $$0 : this.listeners) {
            $$0.containerChanged(this);
         }
      }
   }

   @Override
   public boolean stillValid(Player $$0) {
      return true;
   }

   @Override
   public void clearContent() {
      this.items.clear();
      this.setChanged();
   }

   public void fillStackedContents(StackedItemContents $$0) {
      for (ItemStack $$1 : this.items) {
         $$0.accountStack($$1);
      }
   }

   @Override
   public String toString() {
      return this.items.stream().filter($$0 -> !$$0.isEmpty()).collect(Collectors.toList()).toString();
   }

   private void moveItemToEmptySlots(ItemStack $$0) {
      for (int $$1 = 0; $$1 < this.size; $$1++) {
         ItemStack $$2 = this.getItem($$1);
         if ($$2.isEmpty()) {
            this.setItem($$1, $$0.copyAndClear());
            return;
         }
      }
   }

   private void moveItemToOccupiedSlotsWithSameType(ItemStack $$0) {
      for (int $$1 = 0; $$1 < this.size; $$1++) {
         ItemStack $$2 = this.getItem($$1);
         if (ItemStack.isSameItemSameComponents($$2, $$0)) {
            this.moveItemsBetweenStacks($$0, $$2);
            if ($$0.isEmpty()) {
               return;
            }
         }
      }
   }

   private void moveItemsBetweenStacks(ItemStack $$0, ItemStack $$1) {
      int $$2 = this.getMaxStackSize($$1);
      int $$3 = Math.min($$0.getCount(), $$2 - $$1.getCount());
      if ($$3 > 0) {
         $$1.grow($$3);
         $$0.shrink($$3);
         this.setChanged();
      }
   }

   public void fromItemList(TypedInputList<ItemStack> $$0) {
      this.clearContent();

      for (ItemStack $$1 : $$0) {
         this.addItem($$1);
      }
   }

   public void storeAsItemList(TypedOutputList<ItemStack> $$0) {
      for (int $$1 = 0; $$1 < this.getContainerSize(); $$1++) {
         ItemStack $$2 = this.getItem($$1);
         if (!$$2.isEmpty()) {
            $$0.add($$2);
         }
      }
   }

   public NonNullList<ItemStack> getItems() {
      return this.items;
   }
}
