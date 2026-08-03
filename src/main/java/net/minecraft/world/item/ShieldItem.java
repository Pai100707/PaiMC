package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

public class ShieldItem extends net.minecraft.world.item.Item {
   public ShieldItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public Component getName(net.minecraft.world.item.ItemStack $$0) {
      net.minecraft.world.item.DyeColor $$1 = (net.minecraft.world.item.DyeColor)$$0.get(DataComponents.BASE_COLOR);
      return (Component)($$1 != null ? Component.translatable(this.descriptionId + "." + $$1.getName()) : super.getName($$0));
   }
}
