package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;

public class PlayerHeadItem extends net.minecraft.world.item.StandingAndWallBlockItem {
   public PlayerHeadItem(Block $$0, Block $$1, net.minecraft.world.item.Item.Properties $$2) {
      super($$0, $$1, Direction.DOWN, $$2);
   }

   @Override
   public Component getName(net.minecraft.world.item.ItemStack $$0) {
      ResolvableProfile $$1 = (ResolvableProfile)$$0.get(DataComponents.PROFILE);
      return (Component)($$1 != null && $$1.name().isPresent()
         ? Component.translatable(this.descriptionId + ".named", new Object[]{$$1.name().get()})
         : super.getName($$0));
   }
}
