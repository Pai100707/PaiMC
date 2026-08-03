package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DispenserBlockEntity extends RandomizableContainerBlockEntity {
   public static final int CONTAINER_SIZE = 9;
   private static final Component DEFAULT_NAME = Component.translatable("container.dispenser");
   private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

   protected DispenserBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
      super($$0, $$1, $$2);
   }

   public DispenserBlockEntity(BlockPos $$0, BlockState $$1) {
      this(BlockEntityType.DISPENSER, $$0, $$1);
   }

   public int getContainerSize() {
      return 9;
   }

   public int getRandomSlot(RandomSource $$0) {
      this.unpackLootTable(null);
      int $$1 = -1;
      int $$2 = 1;

      for (int $$3 = 0; $$3 < this.items.size(); $$3++) {
         if (!((ItemStack)this.items.get($$3)).isEmpty() && $$0.nextInt($$2++) == 0) {
            $$1 = $$3;
         }
      }

      return $$1;
   }

   public ItemStack insertItem(ItemStack $$0) {
      int $$1 = this.getMaxStackSize($$0);

      for (int $$2 = 0; $$2 < this.items.size(); $$2++) {
         ItemStack $$3 = (ItemStack)this.items.get($$2);
         if ($$3.isEmpty() || ItemStack.isSameItemSameComponents($$0, $$3)) {
            int $$4 = Math.min($$0.getCount(), $$1 - $$3.getCount());
            if ($$4 > 0) {
               if ($$3.isEmpty()) {
                  this.setItem($$2, $$0.split($$4));
               } else {
                  $$0.shrink($$4);
                  $$3.grow($$4);
               }
            }

            if ($$0.isEmpty()) {
               break;
            }
         }
      }

      return $$0;
   }

   @Override
   protected Component getDefaultName() {
      return DEFAULT_NAME;
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable($$0)) {
         ContainerHelper.loadAllItems($$0, this.items);
      }
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.trySaveLootTable($$0)) {
         ContainerHelper.saveAllItems($$0, this.items);
      }
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> $$0) {
      this.items = $$0;
   }

   @Override
   protected AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return new DispenserMenu($$0, $$1, this);
   }
}
