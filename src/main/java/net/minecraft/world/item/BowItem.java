package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class BowItem extends net.minecraft.world.item.ProjectileWeaponItem {
   public static final int MAX_DRAW_DURATION = 20;
   public static final int DEFAULT_RANGE = 15;

   public BowItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public boolean releaseUsing(net.minecraft.world.item.ItemStack $$0, Level $$1, LivingEntity $$2, int $$3) {
      if (!($$2 instanceof Player $$4)) {
         return false;
      } else {
         net.minecraft.world.item.ItemStack $$5 = $$4.getProjectile($$0);
         if ($$5.isEmpty()) {
            return false;
         } else {
            int $$6 = this.getUseDuration($$0, $$2) - $$3;
            float $$7 = getPowerForTime($$6);
            if ($$7 < 0.1) {
               return false;
            } else {
               List<net.minecraft.world.item.ItemStack> $$8 = draw($$0, $$5, $$4);
               if ($$1 instanceof ServerLevel $$9 && !$$8.isEmpty()) {
                  this.shoot($$9, $$4, $$4.getUsedItemHand(), $$0, $$8, $$7 * 3.0F, 1.0F, $$7 == 1.0F, null);
               }

               $$1.playSound(
                  null,
                  $$4.getX(),
                  $$4.getY(),
                  $$4.getZ(),
                  SoundEvents.ARROW_SHOOT,
                  SoundSource.PLAYERS,
                  1.0F,
                  1.0F / ($$1.getRandom().nextFloat() * 0.4F + 1.2F) + $$7 * 0.5F
               );
               $$4.awardStat(Stats.ITEM_USED.get(this));
               return true;
            }
         }
      }
   }

   @Override
   protected void shootProjectile(LivingEntity $$0, Projectile $$1, int $$2, float $$3, float $$4, float $$5, LivingEntity $$6) {
      $$1.shootFromRotation($$0, $$0.getXRot(), $$0.getYRot() + $$5, 0.0F, $$3, $$4);
   }

   public static float getPowerForTime(int $$0) {
      float $$1 = $$0 / 20.0F;
      $$1 = ($$1 * $$1 + $$1 * 2.0F) / 3.0F;
      if ($$1 > 1.0F) {
         $$1 = 1.0F;
      }

      return $$1;
   }

   @Override
   public int getUseDuration(net.minecraft.world.item.ItemStack $$0, LivingEntity $$1) {
      return 72000;
   }

   @Override
   public net.minecraft.world.item.ItemUseAnimation getUseAnimation(net.minecraft.world.item.ItemStack $$0) {
      return net.minecraft.world.item.ItemUseAnimation.BOW;
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      boolean $$4 = !$$1.getProjectile($$3).isEmpty();
      if (!$$1.hasInfiniteMaterials() && !$$4) {
         return InteractionResult.FAIL;
      } else {
         $$1.startUsingItem($$2);
         return InteractionResult.CONSUME;
      }
   }

   @Override
   public Predicate<net.minecraft.world.item.ItemStack> getAllSupportedProjectiles() {
      return ARROW_ONLY;
   }

   @Override
   public int getDefaultProjectileRange() {
      return 15;
   }
}
