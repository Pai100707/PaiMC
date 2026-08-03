package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceResultSlot extends net.minecraft.world.inventory.Slot {
   private final Player player;
   private int removeCount;

   public FurnaceResultSlot(Player $$0, Container $$1, int $$2, int $$3, int $$4) {
      super($$1, $$2, $$3, $$4);
      this.player = $$0;
   }

   @Override
   public boolean mayPlace(ItemStack $$0) {
      return false;
   }

   @Override
   public ItemStack remove(int $$0) {
      if (this.hasItem()) {
         this.removeCount = this.removeCount + Math.min($$0, this.getItem().getCount());
      }

      return super.remove($$0);
   }

   @Override
   public void onTake(Player $$0, ItemStack $$1) {
      this.checkTakeAchievements($$1);
      super.onTake($$0, $$1);
   }

   @Override
   protected void onQuickCraft(ItemStack $$0, int $$1) {
      this.removeCount += $$1;
      this.checkTakeAchievements($$0);
   }

   @Override
   protected void checkTakeAchievements(ItemStack $$0) {
      $$0.onCraftedBy(this.player, this.removeCount);
      if (this.player instanceof ServerPlayer $$1 && this.container instanceof AbstractFurnaceBlockEntity $$2) {
         $$2.awardUsedRecipesAndPopExperience($$1);
      }

      this.removeCount = 0;
   }
}
