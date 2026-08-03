package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceBlockEntity extends AbstractFurnaceBlockEntity {
   private static final Component DEFAULT_NAME = Component.translatable("container.furnace");

   public FurnaceBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.FURNACE, $$0, $$1, RecipeType.SMELTING);
   }

   @Override
   protected Component getDefaultName() {
      return DEFAULT_NAME;
   }

   @Override
   protected AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return new FurnaceMenu($$0, $$1, this, this.dataAccess);
   }
}
