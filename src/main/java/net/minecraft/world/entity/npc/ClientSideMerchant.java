package net.minecraft.world.entity.npc;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientSideMerchant implements Merchant {
   private final Player source;
   private MerchantOffers offers = new MerchantOffers();
   private int xp;

   public ClientSideMerchant(Player $$0) {
      this.source = $$0;
   }

   public Player getTradingPlayer() {
      return this.source;
   }

   public void setTradingPlayer(Player $$0) {
   }

   public MerchantOffers getOffers() {
      return this.offers;
   }

   public void overrideOffers(MerchantOffers $$0) {
      this.offers = $$0;
   }

   public void notifyTrade(MerchantOffer $$0) {
      $$0.increaseUses();
   }

   public void notifyTradeUpdated(ItemStack $$0) {
   }

   public boolean isClientSide() {
      return this.source.level().isClientSide();
   }

   public boolean stillValid(Player $$0) {
      return this.source == $$0;
   }

   public int getVillagerXp() {
      return this.xp;
   }

   public void overrideXp(int $$0) {
      this.xp = $$0;
   }

   public boolean showProgressBar() {
      return true;
   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.VILLAGER_YES;
   }
}
