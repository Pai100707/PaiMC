package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class MerchantMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
   protected static final int PAYMENT1_SLOT = 0;
   protected static final int PAYMENT2_SLOT = 1;
   protected static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private static final int SELLSLOT1_X = 136;
   private static final int SELLSLOT2_X = 162;
   private static final int BUYSLOT_X = 220;
   private static final int ROW_Y = 37;
   private final Merchant trader;
   private final net.minecraft.world.inventory.MerchantContainer tradeContainer;
   private int merchantLevel;
   private boolean showProgressBar;
   private boolean canRestock;

   public MerchantMenu(int $$0, Inventory $$1) {
      this($$0, $$1, new ClientSideMerchant($$1.player));
   }

   public MerchantMenu(int $$0, Inventory $$1, Merchant $$2) {
      super(net.minecraft.world.inventory.MenuType.MERCHANT, $$0);
      this.trader = $$2;
      this.tradeContainer = new net.minecraft.world.inventory.MerchantContainer($$2);
      this.addSlot(new net.minecraft.world.inventory.Slot(this.tradeContainer, 0, 136, 37));
      this.addSlot(new net.minecraft.world.inventory.Slot(this.tradeContainer, 1, 162, 37));
      this.addSlot(new net.minecraft.world.inventory.MerchantResultSlot($$1.player, $$2, this.tradeContainer, 2, 220, 37));
      this.addStandardInventorySlots($$1, 108, 84);
   }

   public void setShowProgressBar(boolean $$0) {
      this.showProgressBar = $$0;
   }

   @Override
   public void slotsChanged(Container $$0) {
      this.tradeContainer.updateSellItem();
      super.slotsChanged($$0);
   }

   public void setSelectionHint(int $$0) {
      this.tradeContainer.setSelectionHint($$0);
   }

   @Override
   public boolean stillValid(Player $$0) {
      return this.trader.stillValid($$0);
   }

   public int getTraderXp() {
      return this.trader.getVillagerXp();
   }

   public int getFutureTraderXp() {
      return this.tradeContainer.getFutureXp();
   }

   public void setXp(int $$0) {
      this.trader.overrideXp($$0);
   }

   public int getTraderLevel() {
      return this.merchantLevel;
   }

   public void setMerchantLevel(int $$0) {
      this.merchantLevel = $$0;
   }

   public void setCanRestock(boolean $$0) {
      this.canRestock = $$0;
   }

   public boolean canRestock() {
      return this.canRestock;
   }

   @Override
   public boolean canTakeItemForPickAll(ItemStack $$0, net.minecraft.world.inventory.Slot $$1) {
      return false;
   }

   @Override
   public ItemStack quickMoveStack(Player $$0, int $$1) {
      ItemStack $$2 = ItemStack.EMPTY;
      net.minecraft.world.inventory.Slot $$3 = (net.minecraft.world.inventory.Slot)this.slots.get($$1);
      if ($$3 != null && $$3.hasItem()) {
         ItemStack $$4 = $$3.getItem();
         $$2 = $$4.copy();
         if ($$1 == 2) {
            if (!this.moveItemStackTo($$4, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            $$3.onQuickCraft($$4, $$2);
            this.playTradeSound();
         } else if ($$1 != 0 && $$1 != 1) {
            if ($$1 >= 3 && $$1 < 30) {
               if (!this.moveItemStackTo($$4, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if ($$1 >= 30 && $$1 < 39 && !this.moveItemStackTo($$4, 3, 30, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo($$4, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if ($$4.isEmpty()) {
            $$3.setByPlayer(ItemStack.EMPTY);
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

   private void playTradeSound() {
      if (!this.trader.isClientSide()) {
         Entity $$0 = (Entity)this.trader;
         $$0.level().playLocalSound($$0.getX(), $$0.getY(), $$0.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
      }
   }

   @Override
   public void removed(Player $$0) {
      super.removed($$0);
      this.trader.setTradingPlayer(null);
      if (!this.trader.isClientSide()) {
         if (!$$0.isAlive() || $$0 instanceof ServerPlayer && ((ServerPlayer)$$0).hasDisconnected()) {
            ItemStack $$1 = this.tradeContainer.removeItemNoUpdate(0);
            if (!$$1.isEmpty()) {
               $$0.drop($$1, false);
            }

            $$1 = this.tradeContainer.removeItemNoUpdate(1);
            if (!$$1.isEmpty()) {
               $$0.drop($$1, false);
            }
         } else if ($$0 instanceof ServerPlayer) {
            $$0.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(0));
            $$0.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(1));
         }
      }
   }

   public void tryMoveItems(int $$0) {
      if ($$0 >= 0 && this.getOffers().size() > $$0) {
         ItemStack $$1 = this.tradeContainer.getItem(0);
         if (!$$1.isEmpty()) {
            if (!this.moveItemStackTo($$1, 3, 39, true)) {
               return;
            }

            this.tradeContainer.setItem(0, $$1);
         }

         ItemStack $$2 = this.tradeContainer.getItem(1);
         if (!$$2.isEmpty()) {
            if (!this.moveItemStackTo($$2, 3, 39, true)) {
               return;
            }

            this.tradeContainer.setItem(1, $$2);
         }

         if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
            MerchantOffer $$3 = (MerchantOffer)this.getOffers().get($$0);
            this.moveFromInventoryToPaymentSlot(0, $$3.getItemCostA());
            $$3.getItemCostB().ifPresent($$0x -> this.moveFromInventoryToPaymentSlot(1, $$0x));
         }
      }
   }

   private void moveFromInventoryToPaymentSlot(int $$0, ItemCost $$1) {
      for (int $$2 = 3; $$2 < 39; $$2++) {
         ItemStack $$3 = ((net.minecraft.world.inventory.Slot)this.slots.get($$2)).getItem();
         if (!$$3.isEmpty() && $$1.test($$3)) {
            ItemStack $$4 = this.tradeContainer.getItem($$0);
            if ($$4.isEmpty() || ItemStack.isSameItemSameComponents($$3, $$4)) {
               int $$5 = $$3.getMaxStackSize();
               int $$6 = Math.min($$5 - $$4.getCount(), $$3.getCount());
               ItemStack $$7 = $$3.copyWithCount($$4.getCount() + $$6);
               $$3.shrink($$6);
               this.tradeContainer.setItem($$0, $$7);
               if ($$7.getCount() >= $$5) {
                  break;
               }
            }
         }
      }
   }

   public void setOffers(MerchantOffers $$0) {
      this.trader.overrideOffers($$0);
   }

   public MerchantOffers getOffers() {
      return this.trader.getOffers();
   }

   public boolean showProgressBar() {
      return this.showProgressBar;
   }
}
