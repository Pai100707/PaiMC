package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe extends CustomRecipe {
   private static final Ingredient PAPER_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.PAPER);
   private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER);
   private static final Ingredient STAR_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.FIREWORK_STAR);

   public FireworkRocketRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      if ($$0.ingredientCount() < 2) {
         return false;
      } else {
         boolean $$2 = false;
         int $$3 = 0;

         for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
            net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
            if (!$$5.isEmpty()) {
               if (PAPER_INGREDIENT.test($$5)) {
                  if ($$2) {
                     return false;
                  }

                  $$2 = true;
               } else if (GUNPOWDER_INGREDIENT.test($$5)) {
                  if (++$$3 > 3) {
                     return false;
                  }
               } else if (!STAR_INGREDIENT.test($$5)) {
                  return false;
               }
            }
         }

         return $$2 && $$3 >= 1;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      List<FireworkExplosion> $$2 = new ArrayList<>();
      int $$3 = 0;

      for (int $$4 = 0; $$4 < $$0.size(); $$4++) {
         net.minecraft.world.item.ItemStack $$5 = $$0.getItem($$4);
         if (!$$5.isEmpty()) {
            if (GUNPOWDER_INGREDIENT.test($$5)) {
               $$3++;
            } else if (STAR_INGREDIENT.test($$5)) {
               FireworkExplosion $$6 = (FireworkExplosion)$$5.get(DataComponents.FIREWORK_EXPLOSION);
               if ($$6 != null) {
                  $$2.add($$6);
               }
            }
         }
      }

      net.minecraft.world.item.ItemStack $$7 = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET, 3);
      $$7.set(DataComponents.FIREWORKS, new Fireworks($$3, $$2));
      return $$7;
   }

   @Override
   public RecipeSerializer<FireworkRocketRecipe> getSerializer() {
      return RecipeSerializer.FIREWORK_ROCKET;
   }
}
