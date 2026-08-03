package net.minecraft.data.recipes;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public interface RecipeOutput {
   void accept(ResourceKey<Recipe<?>> var1, Recipe<?> var2, @Nullable AdvancementHolder var3);

   Builder advancement();

   void includeRootAdvancement();
}
