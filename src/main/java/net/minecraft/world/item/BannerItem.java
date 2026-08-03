package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.Validate;

public class BannerItem extends net.minecraft.world.item.StandingAndWallBlockItem {
   public BannerItem(Block $$0, Block $$1, net.minecraft.world.item.Item.Properties $$2) {
      super($$0, $$1, Direction.DOWN, $$2);
      Validate.isInstanceOf(AbstractBannerBlock.class, $$0);
      Validate.isInstanceOf(AbstractBannerBlock.class, $$1);
   }

   public net.minecraft.world.item.DyeColor getColor() {
      return ((AbstractBannerBlock)this.getBlock()).getColor();
   }
}
