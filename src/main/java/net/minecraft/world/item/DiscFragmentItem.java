package net.minecraft.world.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.component.TooltipDisplay;

public class DiscFragmentItem extends net.minecraft.world.item.Item {
   public DiscFragmentItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public void appendHoverText(
      net.minecraft.world.item.ItemStack $$0,
      net.minecraft.world.item.Item.TooltipContext $$1,
      TooltipDisplay $$2,
      Consumer<Component> $$3,
      net.minecraft.world.item.TooltipFlag $$4
   ) {
      $$3.accept(this.getDisplayName().withStyle(ChatFormatting.GRAY));
   }

   public MutableComponent getDisplayName() {
      return Component.translatable(this.descriptionId + ".desc");
   }
}
