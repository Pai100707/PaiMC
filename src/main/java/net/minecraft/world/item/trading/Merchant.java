package net.minecraft.world.item.trading;

import java.util.OptionalInt;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;

public interface Merchant {
   void setTradingPlayer(Player var1);

   
   Player getTradingPlayer();

   MerchantOffers getOffers();

   void overrideOffers(MerchantOffers var1);

   void notifyTrade(MerchantOffer var1);

   void notifyTradeUpdated(net.minecraft.world.item.ItemStack var1);

   int getVillagerXp();

   void overrideXp(int var1);

   boolean showProgressBar();

   SoundEvent getNotifyTradeSound();

   default boolean canRestock() {
      return false;
   }

   default void openTradingScreen(Player $$0, Component $$1, int $$2) {
      OptionalInt $$3 = $$0.openMenu(new SimpleMenuProvider(($$0x, $$1x, $$2x) -> new MerchantMenu($$0x, $$1x, this), $$1));
      if ($$3.isPresent()) {
         MerchantOffers $$4 = this.getOffers();
         if (!$$4.isEmpty()) {
            $$0.sendMerchantOffers($$3.getAsInt(), $$4, $$2, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
         }
      }
   }

   boolean isClientSide();

   boolean stillValid(Player var1);
}
