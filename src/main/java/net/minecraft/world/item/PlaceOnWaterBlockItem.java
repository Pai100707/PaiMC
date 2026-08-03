package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class PlaceOnWaterBlockItem extends net.minecraft.world.item.BlockItem {
   public PlaceOnWaterBlockItem(Block $$0, net.minecraft.world.item.Item.Properties $$1) {
      super($$0, $$1);
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      return InteractionResult.PASS;
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      BlockHitResult $$3 = getPlayerPOVHitResult($$0, $$1, Fluid.SOURCE_ONLY);
      BlockHitResult $$4 = $$3.withPosition($$3.getBlockPos().above());
      return super.useOn(new UseOnContext($$1, $$2, $$4));
   }
}
