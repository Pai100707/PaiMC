package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public class FireworkStarRecipe extends CustomRecipe {
   private static final Map<net.minecraft.world.item.Item, FireworkExplosion.Shape> SHAPE_BY_ITEM = Map.of(
      net.minecraft.world.item.Items.FIRE_CHARGE,
      FireworkExplosion.Shape.LARGE_BALL,
      net.minecraft.world.item.Items.FEATHER,
      FireworkExplosion.Shape.BURST,
      net.minecraft.world.item.Items.GOLD_NUGGET,
      FireworkExplosion.Shape.STAR,
      net.minecraft.world.item.Items.SKELETON_SKULL,
      FireworkExplosion.Shape.CREEPER,
      net.minecraft.world.item.Items.WITHER_SKELETON_SKULL,
      FireworkExplosion.Shape.CREEPER,
      net.minecraft.world.item.Items.CREEPER_HEAD,
      FireworkExplosion.Shape.CREEPER,
      net.minecraft.world.item.Items.PLAYER_HEAD,
      FireworkExplosion.Shape.CREEPER,
      net.minecraft.world.item.Items.DRAGON_HEAD,
      FireworkExplosion.Shape.CREEPER,
      net.minecraft.world.item.Items.ZOMBIE_HEAD,
      FireworkExplosion.Shape.CREEPER,
      net.minecraft.world.item.Items.PIGLIN_HEAD,
      FireworkExplosion.Shape.CREEPER
   );
   private static final Ingredient TRAIL_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.DIAMOND);
   private static final Ingredient TWINKLE_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST);
   private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER);

   public FireworkStarRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      if ($$0.ingredientCount() < 2) {
         return false;
      } else {
         boolean $$2 = false;
         boolean $$3 = false;
         boolean $$4 = false;
         boolean $$5 = false;
         boolean $$6 = false;

         for (int $$7 = 0; $$7 < $$0.size(); $$7++) {
            net.minecraft.world.item.ItemStack $$8 = $$0.getItem($$7);
            if (!$$8.isEmpty()) {
               if (SHAPE_BY_ITEM.containsKey($$8.getItem())) {
                  if ($$4) {
                     return false;
                  }

                  $$4 = true;
               } else if (TWINKLE_INGREDIENT.test($$8)) {
                  if ($$6) {
                     return false;
                  }

                  $$6 = true;
               } else if (TRAIL_INGREDIENT.test($$8)) {
                  if ($$5) {
                     return false;
                  }

                  $$5 = true;
               } else if (GUNPOWDER_INGREDIENT.test($$8)) {
                  if ($$2) {
                     return false;
                  }

                  $$2 = true;
               } else {
                  if (!($$8.getItem() instanceof net.minecraft.world.item.DyeItem)) {
                     return false;
                  }

                  $$3 = true;
               }
            }
         }

         return $$2 && $$3;
      }
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      FireworkExplosion.Shape $$2 = FireworkExplosion.Shape.SMALL_BALL;
      boolean $$3 = false;
      boolean $$4 = false;
      IntList $$5 = new IntArrayList();

      for (int $$6 = 0; $$6 < $$0.size(); $$6++) {
         net.minecraft.world.item.ItemStack $$7 = $$0.getItem($$6);
         if (!$$7.isEmpty()) {
            FireworkExplosion.Shape $$8 = SHAPE_BY_ITEM.get($$7.getItem());
            if ($$8 != null) {
               $$2 = $$8;
            } else if (TWINKLE_INGREDIENT.test($$7)) {
               $$3 = true;
            } else if (TRAIL_INGREDIENT.test($$7)) {
               $$4 = true;
            } else if ($$7.getItem() instanceof net.minecraft.world.item.DyeItem $$9) {
               $$5.add($$9.getDyeColor().getFireworkColor());
            }
         }
      }

      net.minecraft.world.item.ItemStack $$10 = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_STAR);
      $$10.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion($$2, $$5, IntList.of(), $$4, $$3));
      return $$10;
   }

   @Override
   public RecipeSerializer<FireworkStarRecipe> getSerializer() {
      return RecipeSerializer.FIREWORK_STAR;
   }
}
