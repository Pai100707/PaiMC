package net.minecraft.world.level.block.entity;

import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public interface ListBackedContainer extends Container {
   NonNullList<ItemStack> getItems();

   default int count() {
      return (int)this.getItems().stream().filter(Predicate.not(ItemStack::isEmpty)).count();
   }

   default int getContainerSize() {
      return this.getItems().size();
   }

   default void clearContent() {
      this.getItems().clear();
   }

   default boolean isEmpty() {
      return this.getItems().stream().allMatch(ItemStack::isEmpty);
   }

   default ItemStack getItem(int $$0) {
      return (ItemStack)this.getItems().get($$0);
   }

   default ItemStack removeItem(int $$0, int $$1) {
      ItemStack $$2 = ContainerHelper.removeItem(this.getItems(), $$0, $$1);
      if (!$$2.isEmpty()) {
         this.setChanged();
      }

      return $$2;
   }

   default ItemStack removeItemNoUpdate(int $$0) {
      return ContainerHelper.removeItem(this.getItems(), $$0, this.getMaxStackSize());
   }

   default boolean canPlaceItem(int $$0, ItemStack $$1) {
      return this.acceptsItemType($$1) && (this.getItem($$0).isEmpty() || this.getItem($$0).getCount() < this.getMaxStackSize($$1));
   }

   default boolean acceptsItemType(ItemStack $$0) {
      return true;
   }

   default void setItem(int $$0, ItemStack $$1) {
      this.setItemNoUpdate($$0, $$1);
      this.setChanged();
   }

   default void setItemNoUpdate(int $$0, ItemStack $$1) {
      this.getItems().set($$0, $$1);
      $$1.limitSize(this.getMaxStackSize($$1));
   }
}
