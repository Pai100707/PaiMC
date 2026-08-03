package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ContainerSingleItem extends Container {
   ItemStack getTheItem();

   default ItemStack splitTheItem(int $$0) {
      return this.getTheItem().split($$0);
   }

   void setTheItem(ItemStack var1);

   default ItemStack removeTheItem() {
      return this.splitTheItem(this.getMaxStackSize());
   }

   default int getContainerSize() {
      return 1;
   }

   default boolean isEmpty() {
      return this.getTheItem().isEmpty();
   }

   default void clearContent() {
      this.removeTheItem();
   }

   default ItemStack removeItemNoUpdate(int $$0) {
      return this.removeItem($$0, this.getMaxStackSize());
   }

   default ItemStack getItem(int $$0) {
      return $$0 == 0 ? this.getTheItem() : ItemStack.EMPTY;
   }

   default ItemStack removeItem(int $$0, int $$1) {
      return $$0 != 0 ? ItemStack.EMPTY : this.splitTheItem($$1);
   }

   default void setItem(int $$0, ItemStack $$1) {
      if ($$0 == 0) {
         this.setTheItem($$1);
      }
   }

   public interface BlockContainerSingleItem extends net.minecraft.world.ticks.ContainerSingleItem {
      BlockEntity getContainerBlockEntity();

      default boolean stillValid(Player $$0) {
         return Container.stillValidBlockEntity(this.getContainerBlockEntity(), $$0);
      }
   }
}
