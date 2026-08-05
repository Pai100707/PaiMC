package net.minecraft.data.recipes;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
   Identifier ROOT_RECIPE_ADVANCEMENT = Identifier.withDefaultNamespace("recipes/root");

   RecipeBuilder unlockedBy(String var1, Criterion<?> var2);

   RecipeBuilder group(String var1);

   Item getResult();

   void save(RecipeOutput var1, ResourceKey<Recipe<?>> var2);

   default void save(RecipeOutput $$0) {
      this.save($$0, ResourceKey.create(Registries.RECIPE, getDefaultRecipeId(this.getResult())));
   }

   default void save(RecipeOutput $$0, String $$1) {
      Identifier $$2 = getDefaultRecipeId(this.getResult());
      Identifier $$3 = Identifier.parse($$1);
      if ($$3.equals($$2)) {
         throw new IllegalStateException("Recipe " + $$1 + " should remove its 'save' argument as it is equal to default one");
      } else {
         this.save($$0, ResourceKey.create(Registries.RECIPE, $$3));
      }
   }

   static Identifier getDefaultRecipeId(ItemLike $$0) {
      return BuiltInRegistries.ITEM.getKey($$0.asItem());
   }

   static CraftingBookCategory determineBookCategory(RecipeCategory $$0) {
      return switch ($$0) {
         case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
         case TOOLS, COMBAT -> CraftingBookCategory.EQUIPMENT;
         case REDSTONE -> CraftingBookCategory.REDSTONE;
         default -> CraftingBookCategory.MISC;
      };
   }
}
