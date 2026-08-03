package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class TippedArrowItem extends net.minecraft.world.item.ArrowItem {
   public TippedArrowItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public net.minecraft.world.item.ItemStack getDefaultInstance() {
      net.minecraft.world.item.ItemStack $$0 = super.getDefaultInstance();
      $$0.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
      return $$0;
   }

   @Override
   public Component getName(net.minecraft.world.item.ItemStack $$0) {
      PotionContents $$1 = (PotionContents)$$0.get(DataComponents.POTION_CONTENTS);
      return $$1 != null ? $$1.getName(this.descriptionId + ".effect.") : super.getName($$0);
   }
}
