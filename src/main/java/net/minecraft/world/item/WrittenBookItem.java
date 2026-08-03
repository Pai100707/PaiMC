package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WrittenBookItem extends net.minecraft.world.item.Item {
   public WrittenBookItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      $$1.openItemGui($$3, $$2);
      $$1.awardStat(Stats.ITEM_USED.get(this));
      return InteractionResult.SUCCESS;
   }
}
