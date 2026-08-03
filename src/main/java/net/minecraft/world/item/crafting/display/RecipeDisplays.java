package net.minecraft.world.item.crafting.display;

import net.minecraft.core.Registry;

public class RecipeDisplays {
   public static RecipeDisplay.Type<?> bootstrap(Registry<RecipeDisplay.Type<?>> $$0) {
      Registry.register($$0, "crafting_shapeless", ShapelessCraftingRecipeDisplay.TYPE);
      Registry.register($$0, "crafting_shaped", ShapedCraftingRecipeDisplay.TYPE);
      Registry.register($$0, "furnace", FurnaceRecipeDisplay.TYPE);
      Registry.register($$0, "stonecutter", StonecutterRecipeDisplay.TYPE);
      return (RecipeDisplay.Type<?>)Registry.register($$0, "smithing", SmithingRecipeDisplay.TYPE);
   }
}
