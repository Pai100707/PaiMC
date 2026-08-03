package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;

public class MerchantContainer implements Container {
   private final Merchant merchant;
   private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
   @Nullable
   private MerchantOffer activeOffer;
   private int selectionHint;
   private int futureXp;

   public MerchantContainer(Merchant $$0) {
      this.merchant = $$0;
   }

   public int getContainerSize() {
      return this.itemStacks.size();
   }

   public boolean isEmpty() {
      for (ItemStack $$0 : this.itemStacks) {
         if (!$$0.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int $$0) {
      return (ItemStack)this.itemStacks.get($$0);
   }

   public ItemStack removeItem(int $$0, int $$1) {
      ItemStack $$2 = (ItemStack)this.itemStacks.get($$0);
      if ($$0 == 2 && !$$2.isEmpty()) {
         return ContainerHelper.removeItem(this.itemStacks, $$0, $$2.getCount());
      } else {
         ItemStack $$3 = ContainerHelper.removeItem(this.itemStacks, $$0, $$1);
         if (!$$3.isEmpty() && this.isPaymentSlot($$0)) {
            this.updateSellItem();
         }

         return $$3;
      }
   }

   private boolean isPaymentSlot(int $$0) {
      return $$0 == 0 || $$0 == 1;
   }

   public ItemStack removeItemNoUpdate(int $$0) {
      return ContainerHelper.takeItem(this.itemStacks, $$0);
   }

   public void setItem(int $$0, ItemStack $$1) {
      this.itemStacks.set($$0, $$1);
      $$1.limitSize(this.getMaxStackSize($$1));
      if (this.isPaymentSlot($$0)) {
         this.updateSellItem();
      }
   }

   public boolean stillValid(Player $$0) {
      return this.merchant.getTradingPlayer() == $$0;
   }

   public void setChanged() {
      this.updateSellItem();
   }

   public void updateSellItem() {
      this.activeOffer = null;
      ItemStack $$0;
      ItemStack $$1;
      if (((ItemStack)this.itemStacks.get(0)).isEmpty()) {
         $$0 = (ItemStack)this.itemStacks.get(1);
         $$1 = ItemStack.EMPTY;
      } else {
         $$0 = (ItemStack)this.itemStacks.get(0);
         $$1 = (ItemStack)this.itemStacks.get(1);
      }

      if ($$0.isEmpty()) {
         this.setItem(2, ItemStack.EMPTY);
         this.futureXp = 0;
      } else {
         MerchantOffers $$4 = this.merchant.getOffers();
         if (!$$4.isEmpty()) {
            MerchantOffer $$5 = $$4.getRecipeFor($$0, $$1, this.selectionHint);
            if ($$5 == null || $$5.isOutOfStock()) {
               this.activeOffer = $$5;
               $$5 = $$4.getRecipeFor($$1, $$0, this.selectionHint);
            }

            if ($$5 != null && !$$5.isOutOfStock()) {
               this.activeOffer = $$5;
               this.setItem(2, $$5.assemble());
               this.futureXp = $$5.getXp();
            } else {
               this.setItem(2, ItemStack.EMPTY);
               this.futureXp = 0;
            }
         }

         this.merchant.notifyTradeUpdated(this.getItem(2));
      }
   }

   @Nullable
   public MerchantOffer getActiveOffer() {
      return this.activeOffer;
   }

   public void setSelectionHint(int $$0) {
      this.selectionHint = $$0;
      this.updateSellItem();
   }

   public void clearContent() {
      this.itemStacks.clear();
   }

   public int getFutureXp() {
      return this.futureXp;
   }
}
