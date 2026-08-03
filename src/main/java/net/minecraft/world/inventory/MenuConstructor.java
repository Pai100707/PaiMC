package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface MenuConstructor {
   @Nullable
   net.minecraft.world.inventory.AbstractContainerMenu createMenu(int var1, Inventory var2, Player var3);
}
