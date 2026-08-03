package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EmptyMapItem extends net.minecraft.world.item.Item {
   public EmptyMapItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      if ($$0 instanceof ServerLevel $$4) {
         $$3.consume(1, $$1);
         $$1.awardStat(Stats.ITEM_USED.get(this));
         $$4.playSound(null, $$1, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, $$1.getSoundSource(), 1.0F, 1.0F);
         net.minecraft.world.item.ItemStack $$6 = net.minecraft.world.item.MapItem.create($$4, $$1.getBlockX(), $$1.getBlockZ(), (byte)0, true, false);
         if ($$3.isEmpty()) {
            return InteractionResult.SUCCESS.heldItemTransformedTo($$6);
         } else {
            if (!$$1.getInventory().add($$6.copy())) {
               $$1.drop($$6, false);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.SUCCESS;
      }
   }
}
