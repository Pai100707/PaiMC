package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.CrafterBlock;

public class CrafterMenu extends net.minecraft.world.inventory.AbstractContainerMenu implements net.minecraft.world.inventory.ContainerListener {
   protected static final int SLOT_COUNT = 9;
   private static final int INV_SLOT_START = 9;
   private static final int INV_SLOT_END = 36;
   private static final int USE_ROW_SLOT_START = 36;
   private static final int USE_ROW_SLOT_END = 45;
   private final net.minecraft.world.inventory.ResultContainer resultContainer = new net.minecraft.world.inventory.ResultContainer();
   private final net.minecraft.world.inventory.ContainerData containerData;
   private final Player player;
   private final net.minecraft.world.inventory.CraftingContainer container;

   public CrafterMenu(int $$0, Inventory $$1) {
      super(net.minecraft.world.inventory.MenuType.CRAFTER_3x3, $$0);
      this.player = $$1.player;
      this.containerData = new net.minecraft.world.inventory.SimpleContainerData(10);
      this.container = new net.minecraft.world.inventory.TransientCraftingContainer(this, 3, 3);
      this.addSlots($$1);
   }

   public CrafterMenu(int $$0, Inventory $$1, net.minecraft.world.inventory.CraftingContainer $$2, net.minecraft.world.inventory.ContainerData $$3) {
      super(net.minecraft.world.inventory.MenuType.CRAFTER_3x3, $$0);
      this.player = $$1.player;
      this.containerData = $$3;
      this.container = $$2;
      checkContainerSize($$2, 9);
      $$2.startOpen($$1.player);
      this.addSlots($$1);
      this.addSlotListener(this);
   }

   private void addSlots(Inventory $$0) {
      for (int $$1 = 0; $$1 < 3; $$1++) {
         for (int $$2 = 0; $$2 < 3; $$2++) {
            int $$3 = $$2 + $$1 * 3;
            this.addSlot(new net.minecraft.world.inventory.CrafterSlot(this.container, $$3, 26 + $$2 * 18, 17 + $$1 * 18, this));
         }
      }

      this.addStandardInventorySlots($$0, 8, 84);
      this.addSlot(new net.minecraft.world.inventory.NonInteractiveResultSlot(this.resultContainer, 0, 134, 35));
      this.addDataSlots(this.containerData);
      this.refreshRecipeResult();
   }

   public void setSlotState(int $$0, boolean $$1) {
      net.minecraft.world.inventory.CrafterSlot $$2 = (net.minecraft.world.inventory.CrafterSlot)this.getSlot($$0);
      this.containerData.set($$2.index, $$1 ? 0 : 1);
      this.broadcastChanges();
   }

   public boolean isSlotDisabled(int $$0) {
      return $$0 > -1 && $$0 < 9 ? this.containerData.get($$0) == 1 : false;
   }

   public boolean isPowered() {
      return this.containerData.get(9) == 1;
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 < 9) {
            if (!this.moveItemStackTo($$4, 9, 45, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo($$4, 0, 9, false)) {
            return ItemStack.EMPTY;
         }

         if ($$4.isEmpty()) {
            $$3.set(ItemStack.EMPTY);
         } else {
            $$3.setChanged();
         }

         if ($$4.getCount() == $$2.getCount()) {
            return ItemStack.EMPTY;
         }

         $$3.onTake($$0, $$4);
      }

      return $$2;
   }

   @Override
   public boolean stillValid(Player $$0) {
      return this.container.stillValid($$0);
   }

   private void refreshRecipeResult() {
      if (this.player instanceof ServerPlayer $$0) {
         ServerLevel $$1 = $$0.level();
         CraftingInput $$2 = this.container.asCraftInput();
         ItemStack $$3 = CrafterBlock.getPotentialResults($$1, $$2)
            .map($$2x -> ((CraftingRecipe)$$2x.value()).assemble($$2, $$1.registryAccess()))
            .orElse(ItemStack.EMPTY);
         this.resultContainer.setItem(0, $$3);
      }
   }

   public Container getContainer() {
      return this.container;
   }

   @Override
   public void slotChanged(net.minecraft.world.inventory.AbstractContainerMenu $$0, int $$1, ItemStack $$2) {
      this.refreshRecipeResult();
   }

   @Override
   public void dataChanged(net.minecraft.world.inventory.AbstractContainerMenu $$0, int $$1, int $$2) {
   }
}
