package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils {
   public static InteractionResult startUsingInstantly(Level $$0, Player $$1, InteractionHand $$2) {
      $$1.startUsingItem($$2);
      return InteractionResult.CONSUME;
   }

   public static net.minecraft.world.item.ItemStack createFilledResult(
      net.minecraft.world.item.ItemStack $$0, Player $$1, net.minecraft.world.item.ItemStack $$2, boolean $$3
   ) {
      boolean $$4 = $$1.hasInfiniteMaterials();
      if ($$3 && $$4) {
         if (!$$1.getInventory().contains($$2)) {
            $$1.getInventory().add($$2);
         }

         return $$0;
      } else {
         $$0.consume(1, $$1);
         if ($$0.isEmpty()) {
            return $$2;
         } else {
            if (!$$1.getInventory().add($$2)) {
               $$1.drop($$2, false);
            }

            return $$0;
         }
      }
   }

   public static net.minecraft.world.item.ItemStack createFilledResult(
      net.minecraft.world.item.ItemStack $$0, Player $$1, net.minecraft.world.item.ItemStack $$2
   ) {
      return createFilledResult($$0, $$1, $$2, true);
   }

   public static void onContainerDestroyed(ItemEntity $$0, Iterable<net.minecraft.world.item.ItemStack> $$1) {
      Level $$2 = $$0.level();
      if (!$$2.isClientSide()) {
         $$1.forEach($$2x -> $$2.addFreshEntity(new ItemEntity($$2, $$0.getX(), $$0.getY(), $$0.getZ(), $$2x)));
      }
   }
}
