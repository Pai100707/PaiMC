package net.minecraft.world.inventory;

import net.minecraft.world.item.ItemStack;

public interface ContainerListener {
   void slotChanged(net.minecraft.world.inventory.AbstractContainerMenu var1, int var2, ItemStack var3);

   void dataChanged(net.minecraft.world.inventory.AbstractContainerMenu var1, int var2, int var3);
}
