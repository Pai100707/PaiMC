package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class InkSacItem extends net.minecraft.world.item.Item implements net.minecraft.world.item.SignApplicator {
   public InkSacItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public boolean tryApplyToSign(Level $$0, SignBlockEntity $$1, boolean $$2, Player $$3) {
      if ($$1.updateText($$0x -> $$0x.setHasGlowingText(false), $$2)) {
         $$0.playSound(null, $$1.getBlockPos(), SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}
