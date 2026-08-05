package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;

public class ItemStackLinkedSet {
   private static final Strategy<? super net.minecraft.world.item.ItemStack> TYPE_AND_TAG = new Strategy<net.minecraft.world.item.ItemStack>() {
      public int hashCode(net.minecraft.world.item.ItemStack $$0) {
         return net.minecraft.world.item.ItemStack.hashItemAndComponents($$0);
      }

      public boolean equals(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
         return $$0 == $$1
            || $$0 != null && $$1 != null && $$0.isEmpty() == $$1.isEmpty() && net.minecraft.world.item.ItemStack.isSameItemSameComponents($$0, $$1);
      }
   };

   public static Set<net.minecraft.world.item.ItemStack> createTypeAndComponentsSet() {
      return new ObjectLinkedOpenCustomHashSet(TYPE_AND_TAG);
   }
}
