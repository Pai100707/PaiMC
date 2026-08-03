package net.minecraft.world.item;

import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.level.Level;

public class SplashPotionItem extends net.minecraft.world.item.ThrowablePotionItem {
   public SplashPotionItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      $$0.playSound(
         null,
         $$1.getX(),
         $$1.getY(),
         $$1.getZ(),
         SoundEvents.SPLASH_POTION_THROW,
         SoundSource.PLAYERS,
         0.5F,
         0.4F / ($$0.getRandom().nextFloat() * 0.4F + 0.8F)
      );
      return super.use($$0, $$1, $$2);
   }

   @Override
   protected AbstractThrownPotion createPotion(ServerLevel $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2) {
      return new ThrownSplashPotion($$0, $$1, $$2);
   }

   @Override
   protected AbstractThrownPotion createPotion(Level $$0, Position $$1, net.minecraft.world.item.ItemStack $$2) {
      return new ThrownSplashPotion($$0, $$1.x(), $$1.y(), $$1.z(), $$2);
   }
}
