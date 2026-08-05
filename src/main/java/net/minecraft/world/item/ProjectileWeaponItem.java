package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public abstract class ProjectileWeaponItem extends net.minecraft.world.item.Item {
   public static final Predicate<net.minecraft.world.item.ItemStack> ARROW_ONLY = $$0 -> $$0.is(ItemTags.ARROWS);
   public static final Predicate<net.minecraft.world.item.ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or(
      $$0 -> $$0.is(net.minecraft.world.item.Items.FIREWORK_ROCKET)
   );

   public ProjectileWeaponItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   public Predicate<net.minecraft.world.item.ItemStack> getSupportedHeldProjectiles() {
      return this.getAllSupportedProjectiles();
   }

   public abstract Predicate<net.minecraft.world.item.ItemStack> getAllSupportedProjectiles();

   public static net.minecraft.world.item.ItemStack getHeldProjectile(LivingEntity $$0, Predicate<net.minecraft.world.item.ItemStack> $$1) {
      if ($$1.test($$0.getItemInHand(InteractionHand.OFF_HAND))) {
         return $$0.getItemInHand(InteractionHand.OFF_HAND);
      } else {
         return $$1.test($$0.getItemInHand(InteractionHand.MAIN_HAND))
            ? $$0.getItemInHand(InteractionHand.MAIN_HAND)
            : net.minecraft.world.item.ItemStack.EMPTY;
      }
   }

   public abstract int getDefaultProjectileRange();

   protected void shoot(
      ServerLevel $$0,
      LivingEntity $$1,
      InteractionHand $$2,
      net.minecraft.world.item.ItemStack $$3,
      List<net.minecraft.world.item.ItemStack> $$4,
      float $$5,
      float $$6,
      boolean $$7,
      LivingEntity $$8
   ) {
      float $$9 = EnchantmentHelper.processProjectileSpread($$0, $$3, $$1, 0.0F);
      float $$10 = $$4.size() == 1 ? 0.0F : 2.0F * $$9 / ($$4.size() - 1);
      float $$11 = ($$4.size() - 1) % 2 * $$10 / 2.0F;
      float $$12 = 1.0F;

      for (int $$13 = 0; $$13 < $$4.size(); $$13++) {
         net.minecraft.world.item.ItemStack $$14 = $$4.get($$13);
         if (!$$14.isEmpty()) {
            float $$15 = $$11 + $$12 * (($$13 + 1) / 2) * $$10;
            $$12 = -$$12;
            int $$16 = $$13;
            Projectile.spawnProjectile(
               this.createProjectile($$0, $$1, $$3, $$14, $$7), $$0, $$14, $$6x -> this.shootProjectile($$1, $$6x, $$16, $$5, $$6, $$15, $$8)
            );
            $$3.hurtAndBreak(this.getDurabilityUse($$14), $$1, $$2.asEquipmentSlot());
            if ($$3.isEmpty()) {
               break;
            }
         }
      }
   }

   protected int getDurabilityUse(net.minecraft.world.item.ItemStack $$0) {
      return 1;
   }

   protected abstract void shootProjectile(LivingEntity var1, Projectile var2, int var3, float var4, float var5, float var6, LivingEntity var7);

   protected Projectile createProjectile(
      Level $$0, LivingEntity $$1, net.minecraft.world.item.ItemStack $$2, net.minecraft.world.item.ItemStack $$3, boolean $$4
   ) {
      net.minecraft.world.item.ArrowItem $$6 = $$3.getItem() instanceof net.minecraft.world.item.ArrowItem $$5
         ? $$5
         : (net.minecraft.world.item.ArrowItem)net.minecraft.world.item.Items.ARROW;
      AbstractArrow $$7 = $$6.createArrow($$0, $$3, $$1, $$2);
      if ($$4) {
         $$7.setCritArrow(true);
      }

      return $$7;
   }

   protected static List<net.minecraft.world.item.ItemStack> draw(
      net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1, LivingEntity $$2
   ) {
      if ($$1.isEmpty()) {
         return List.of();
      } else {
         int $$4 = $$2.level() instanceof ServerLevel $$3 ? EnchantmentHelper.processProjectileCount($$3, $$0, $$2, 1) : 1;
         List<net.minecraft.world.item.ItemStack> $$5 = new ArrayList<>($$4);
         net.minecraft.world.item.ItemStack $$6 = $$1.copy();

         for (int $$7 = 0; $$7 < $$4; $$7++) {
            net.minecraft.world.item.ItemStack $$8 = useAmmo($$0, $$7 == 0 ? $$1 : $$6, $$2, $$7 > 0);
            if (!$$8.isEmpty()) {
               $$5.add($$8);
            }
         }

         return $$5;
      }
   }

   protected static net.minecraft.world.item.ItemStack useAmmo(
      net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1, LivingEntity $$2, boolean $$3
   ) {
      int $$5 = !$$3 && !$$2.hasInfiniteMaterials() && $$2.level() instanceof ServerLevel $$4 ? EnchantmentHelper.processAmmoUse($$4, $$0, $$1, 1) : 0;
      if ($$5 > $$1.getCount()) {
         return net.minecraft.world.item.ItemStack.EMPTY;
      } else if ($$5 == 0) {
         net.minecraft.world.item.ItemStack $$6 = $$1.copyWithCount(1);
         $$6.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
         return $$6;
      } else {
         net.minecraft.world.item.ItemStack $$7 = $$1.split($$5);
         if ($$1.isEmpty() && $$2 instanceof Player $$8) {
            $$8.getInventory().removeItem($$1);
         }

         return $$7;
      }
   }
}
