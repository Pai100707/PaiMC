package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MenuConstructor {
   
   net.minecraft.world.inventory.AbstractContainerMenu createMenu(int var1, Inventory var2, Player var3);
}
