package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface ContainerSynchronizer {
   void sendInitialData(net.minecraft.world.inventory.AbstractContainerMenu var1, List<ItemStack> var2, ItemStack var3, int[] var4);

   void sendSlotChange(net.minecraft.world.inventory.AbstractContainerMenu var1, int var2, ItemStack var3);

   void sendCarriedChange(net.minecraft.world.inventory.AbstractContainerMenu var1, ItemStack var2);

   void sendDataChange(net.minecraft.world.inventory.AbstractContainerMenu var1, int var2, int var3);

   net.minecraft.world.inventory.RemoteSlot createSlot();
}
