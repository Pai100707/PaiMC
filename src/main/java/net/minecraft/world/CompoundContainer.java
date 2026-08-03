package net.minecraft.world;

import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompoundContainer implements Container {
   private final Container container1;
   private final Container container2;

   public CompoundContainer(Container $$0, Container $$1) {
      this.container1 = $$0;
      this.container2 = $$1;
   }

   @Override
   public int getContainerSize() {
      return this.container1.getContainerSize() + this.container2.getContainerSize();
   }

   @Override
   public boolean isEmpty() {
      return this.container1.isEmpty() && this.container2.isEmpty();
   }

   public boolean contains(Container $$0) {
      return this.container1 == $$0 || this.container2 == $$0;
   }

   @Override
   public ItemStack getItem(int $$0) {
      return $$0 >= this.container1.getContainerSize() ? this.container2.getItem($$0 - this.container1.getContainerSize()) : this.container1.getItem($$0);
   }

   @Override
   public ItemStack removeItem(int $$0, int $$1) {
      return $$0 >= this.container1.getContainerSize()
         ? this.container2.removeItem($$0 - this.container1.getContainerSize(), $$1)
         : this.container1.removeItem($$0, $$1);
   }

   @Override
   public ItemStack removeItemNoUpdate(int $$0) {
      return $$0 >= this.container1.getContainerSize()
         ? this.container2.removeItemNoUpdate($$0 - this.container1.getContainerSize())
         : this.container1.removeItemNoUpdate($$0);
   }

   @Override
   public void setItem(int $$0, ItemStack $$1) {
      if ($$0 >= this.container1.getContainerSize()) {
         this.container2.setItem($$0 - this.container1.getContainerSize(), $$1);
      } else {
         this.container1.setItem($$0, $$1);
      }
   }

   @Override
   public int getMaxStackSize() {
      return this.container1.getMaxStackSize();
   }

   @Override
   public void setChanged() {
      this.container1.setChanged();
      this.container2.setChanged();
   }

   @Override
   public boolean stillValid(Player $$0) {
      return this.container1.stillValid($$0) && this.container2.stillValid($$0);
   }

   @Override
   public void startOpen(ContainerUser $$0) {
      this.container1.startOpen($$0);
      this.container2.startOpen($$0);
   }

   @Override
   public void stopOpen(ContainerUser $$0) {
      this.container1.stopOpen($$0);
      this.container2.stopOpen($$0);
   }

   @Override
   public boolean canPlaceItem(int $$0, ItemStack $$1) {
      return $$0 >= this.container1.getContainerSize()
         ? this.container2.canPlaceItem($$0 - this.container1.getContainerSize(), $$1)
         : this.container1.canPlaceItem($$0, $$1);
   }

   @Override
   public void clearContent() {
      this.container1.clearContent();
      this.container2.clearContent();
   }
}
