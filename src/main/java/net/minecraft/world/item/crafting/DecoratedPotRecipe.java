package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

public class DecoratedPotRecipe extends CustomRecipe {
   public DecoratedPotRecipe(CraftingBookCategory $$0) {
      super($$0);
   }

   private static net.minecraft.world.item.ItemStack back(CraftingInput $$0) {
      return $$0.getItem(1, 0);
   }

   private static net.minecraft.world.item.ItemStack left(CraftingInput $$0) {
      return $$0.getItem(0, 1);
   }

   private static net.minecraft.world.item.ItemStack right(CraftingInput $$0) {
      return $$0.getItem(2, 1);
   }

   private static net.minecraft.world.item.ItemStack front(CraftingInput $$0) {
      return $$0.getItem(1, 2);
   }

   public boolean matches(CraftingInput $$0, Level $$1) {
      return $$0.width() == 3 && $$0.height() == 3 && $$0.ingredientCount() == 4
         ? back($$0).is(ItemTags.DECORATED_POT_INGREDIENTS)
            && left($$0).is(ItemTags.DECORATED_POT_INGREDIENTS)
            && right($$0).is(ItemTags.DECORATED_POT_INGREDIENTS)
            && front($$0).is(ItemTags.DECORATED_POT_INGREDIENTS)
         : false;
   }

   public net.minecraft.world.item.ItemStack assemble(CraftingInput $$0, Provider $$1) {
      PotDecorations $$2 = new PotDecorations(back($$0).getItem(), left($$0).getItem(), right($$0).getItem(), front($$0).getItem());
      return DecoratedPotBlockEntity.createDecoratedPotItem($$2);
   }

   @Override
   public RecipeSerializer<DecoratedPotRecipe> getSerializer() {
      return RecipeSerializer.DECORATED_POT_RECIPE;
   }
}
