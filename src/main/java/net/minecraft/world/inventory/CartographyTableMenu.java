package net.minecraft.world.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   public static final int MAP_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private final net.minecraft.world.inventory.ContainerLevelAccess access;
   long lastSoundTime;
   public final Container container = new SimpleContainer(2) {
      public void setChanged() {
         CartographyTableMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };
   private final net.minecraft.world.inventory.ResultContainer resultContainer = new net.minecraft.world.inventory.ResultContainer() {
      @Override
      public void setChanged() {
         CartographyTableMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };

   public CartographyTableMenu(int $$0, Inventory $$1) {
      this($$0, $$1, net.minecraft.world.inventory.ContainerLevelAccess.NULL);
   }

   public CartographyTableMenu(int $$0, Inventory $$1, final net.minecraft.world.inventory.ContainerLevelAccess $$2) {
      super(net.minecraft.world.inventory.MenuType.CARTOGRAPHY_TABLE, $$0);
      this.access = $$2;
      this.addSlot(new net.minecraft.world.inventory.Slot(this.container, 0, 15, 15) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.has(DataComponents.MAP_ID);
         }
      });
      this.addSlot(new net.minecraft.world.inventory.Slot(this.container, 1, 15, 52) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return $$0.is(Items.PAPER) || $$0.is(Items.MAP) || $$0.is(Items.GLASS_PANE);
         }
      });
      this.addSlot(new net.minecraft.world.inventory.Slot(this.resultContainer, 2, 145, 39) {
         @Override
         public boolean mayPlace(ItemStack $$0) {
            return false;
         }

         @Override
         public void onTake(Player $$0, ItemStack $$1x) {
            ((net.minecraft.world.inventory.Slot)CartographyTableMenu.this.slots.get(0)).remove(1);
            ((net.minecraft.world.inventory.Slot)CartographyTableMenu.this.slots.get(1)).remove(1);
            $$1x.getItem().onCraftedBy($$1x, $$0);
            $$2.execute(($$0x, $$1xx) -> {
               long $$2xx = $$0x.getGameTime();
               if (CartographyTableMenu.this.lastSoundTime != $$2xx) {
                  $$0x.playSound(null, $$1xx, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  CartographyTableMenu.this.lastSoundTime = $$2xx;
               }
            });
            super.onTake($$0, $$1x);
         }
      });
      this.addStandardInventorySlots($$1, 8, 84);
   }

   @Override
   public boolean stillValid(Player $$0) {
      return stillValid(this.access, $$0, Blocks.CARTOGRAPHY_TABLE);
   }

   @Override
   public void slotsChanged(Container $$0) {
      ItemStack $$1 = this.container.getItem(0);
      ItemStack $$2 = this.container.getItem(1);
      ItemStack $$3 = this.resultContainer.getItem(2);
      if ($$3.isEmpty() || !$$1.isEmpty() && !$$2.isEmpty()) {
         if (!$$1.isEmpty() && !$$2.isEmpty()) {
            this.setupResultSlot($$1, $$2, $$3);
         }
      } else {
         this.resultContainer.removeItemNoUpdate(2);
      }
   }

   private void setupResultSlot(ItemStack $$0, ItemStack $$1, ItemStack $$2) {
      this.access.execute(($$3, $$4) -> {
         MapItemSavedData $$5 = MapItem.getSavedData($$0, $$3);
         if ($$5 != null) {
            ItemStack $$6;
            if ($$1.is(Items.PAPER) && !$$5.locked && $$5.scale < 4) {
               $$6 = $$0.copyWithCount(1);
               $$6.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
               this.broadcastChanges();
            } else if ($$1.is(Items.GLASS_PANE) && !$$5.locked) {
               $$6 = $$0.copyWithCount(1);
               $$6.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.LOCK);
               this.broadcastChanges();
            } else {
               if (!$$1.is(Items.MAP)) {
                  this.resultContainer.removeItemNoUpdate(2);
                  this.broadcastChanges();
                  return;
               }

               $$6 = $$0.copyWithCount(2);
               this.broadcastChanges();
            }

            if (!ItemStack.matches($$6, $$2)) {
               this.resultContainer.setItem(2, $$6);
               this.broadcastChanges();
            }
         }
      });
   }

   @Override
   public boolean canTakeItemForPickAll(ItemStack $$0, net.minecraft.world.inventory.Slot $$1) {
      return $$1.container != this.resultContainer && super.canTakeItemForPickAll($$0, $$1);
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 == 2) {
            $$4.getItem().onCraftedBy($$4, $$0);
            if (!this.moveItemStackTo($$4, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            $$3.onQuickCraft($$4, $$2);
         } else if ($$1 != 1 && $$1 != 0) {
            if ($$4.has(DataComponents.MAP_ID)) {
               if (!this.moveItemStackTo($$4, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!$$4.is(Items.PAPER) && !$$4.is(Items.MAP) && !$$4.is(Items.GLASS_PANE)) {
               if ($$1 >= 3 && $$1 < 30) {
                  if (!this.moveItemStackTo($$4, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if ($$1 >= 30 && $$1 < 39 && !this.moveItemStackTo($$4, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo($$4, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo($$4, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if ($$4.isEmpty()) {
            $$3.setByPlayer(ItemStack.EMPTY);
         }

         $$3.setChanged();
         if ($$4.getCount() == $$2.getCount()) {
            return ItemStack.EMPTY;
         }

         $$3.onTake($$0, $$4);
         this.broadcastChanges();
      }

      return $$2;
   }

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.resultContainer.removeItemNoUpdate(2);
      this.access.execute(($$1, $$2) -> this.clearContainer($$0, this.container));
   }
}
