package net.minecraft.world.item.crafting;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class RepairItemRecipe extends CustomRecipe {
   public RepairItemRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   
   private static Pair<net.minecraft.world.item.ItemStack, net.minecraft.world.item.ItemStack> getItemsToCombine(CraftingInput $$0) {
      if ($$0.ingredientCount() != 2) {
         return null;
      } else {
         net.minecraft.world.item.ItemStack $$1 = null;

         for (int $$2 = 0; $$2 < $$0.size(); $$2++) {
            net.minecraft.world.item.ItemStack $$3 = $$0.getItem($$2);
            if (!$$3.isEmpty()) {
               if ($$1 != null) {
                  return canCombine($$1, $$3) ? Pair.of($$1, $$3) : null;
               }

               $$1 = $$3;
            }
         }

         return null;
      }
   }

   private static boolean canCombine(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.ItemStack $$1) {
      return $$1.is($$0.getItem())
         && $$0.getCount() == 1
         && $$1.getCount() == 1
         && $$0.has(DataComponents.MAX_DAMAGE)
         && $$1.has(DataComponents.MAX_DAMAGE)
         && $$0.has(DataComponents.DAMAGE)
         && $$1.has(DataComponents.DAMAGE);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      return getItemsToCombine($$0) != null;
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      Pair<net.minecraft.world.item.ItemStack, net.minecraft.world.item.ItemStack> $$2 = getItemsToCombine($$0);
      if ($$2 == null) {
         return net.minecraft.world.item.ItemStack.EMPTY;
      } else {
         net.minecraft.world.item.ItemStack $$3 = (net.minecraft.world.item.ItemStack)$$2.getFirst();
         net.minecraft.world.item.ItemStack $$4 = (net.minecraft.world.item.ItemStack)$$2.getSecond();
         int $$5 = Math.max($$3.getMaxDamage(), $$4.getMaxDamage());
         int $$6 = $$3.getMaxDamage() - $$3.getDamageValue();
         int $$7 = $$4.getMaxDamage() - $$4.getDamageValue();
         int $$8 = $$6 + $$7 + $$5 * 5 / 100;
         net.minecraft.world.item.ItemStack $$9 = new net.minecraft.world.item.ItemStack($$3.getItem());
         $$9.set(DataComponents.MAX_DAMAGE, $$5);
         $$9.setDamageValue(Math.max($$5 - $$8, 0));
         ItemEnchantments $$10 = EnchantmentHelper.getEnchantmentsForCrafting($$3);
         ItemEnchantments $$11 = EnchantmentHelper.getEnchantmentsForCrafting($$4);
         EnchantmentHelper.updateEnchantments(
            $$9, $$3x -> $$1.lookupOrThrow(Registries.ENCHANTMENT).listElements().filter($$0xx -> $$0xx.is(EnchantmentTags.CURSE)).forEach($$3xx -> {
               int $$4x = Math.max($$10.getLevel($$3xx), $$11.getLevel($$3xx));
               if ($$4x > 0) {
                  $$3x.upgrade($$3xx, $$4x);
               }
            })
         );
         return $$9;
      }
   }

   @Override
   public RecipeSerializer<RepairItemRecipe> getSerializer() {
      return RecipeSerializer.REPAIR_ITEM;
   }
}
