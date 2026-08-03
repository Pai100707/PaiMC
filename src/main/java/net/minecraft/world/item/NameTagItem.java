package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class NameTagItem extends net.minecraft.world.item.Item {
   public NameTagItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult interactLivingEntity(net.minecraft.world.item.ItemStack $$0, Player $$1, LivingEntity $$2, InteractionHand $$3) {
      Component $$4 = (Component)$$0.get(DataComponents.CUSTOM_NAME);
      if ($$4 != null && $$2.getType().canSerialize()) {
         if (!$$1.level().isClientSide() && $$2.isAlive()) {
            $$2.setCustomName($$4);
            if ($$2 instanceof Mob $$5) {
               $$5.setPersistenceRequired();
            }

            $$0.shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }
}
