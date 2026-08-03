package net.minecraft.world.damagesource;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CombatRules {
   public static final float MAX_ARMOR = 20.0F;
   public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
   public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
   public static final float MIN_ARMOR_RATIO = 0.2F;
   private static final int NUM_ARMOR_ITEMS = 4;

   public static float getDamageAfterAbsorb(LivingEntity $$0, float $$1, net.minecraft.world.damagesource.DamageSource $$2, float $$3, float $$4) {
      float $$5 = 2.0F + $$4 / 4.0F;
      float $$6 = Mth.clamp($$3 - $$1 / $$5, $$3 * 0.2F, 20.0F);
      float $$7 = $$6 / 25.0F;
      ItemStack $$8 = $$2.getWeaponItem();
      float $$10;
      if ($$8 != null && $$0.level() instanceof ServerLevel $$9) {
         $$10 = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness($$9, $$8, $$0, $$2, $$7), 0.0F, 1.0F);
      } else {
         $$10 = $$7;
      }

      float $$12 = 1.0F - $$10;
      return $$1 * $$12;
   }

   public static float getDamageAfterMagicAbsorb(float $$0, float $$1) {
      float $$2 = Mth.clamp($$1, 0.0F, 20.0F);
      return $$0 * (1.0F - $$2 / 25.0F);
   }
}
