package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.item.crafting.TransmuteResult;
import org.jspecify.annotations.Nullable;

public class TransmuteRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Holder<Item> result;
   private final Ingredient input;
   private final Ingredient material;
   private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
   @Nullable
   private String group;

   private TransmuteRecipeBuilder(RecipeCategory $$0, Holder<Item> $$1, Ingredient $$2, Ingredient $$3) {
      this.category = $$0;
      this.result = $$1;
      this.input = $$2;
      this.material = $$3;
   }

   public static TransmuteRecipeBuilder transmute(RecipeCategory $$0, Ingredient $$1, Ingredient $$2, Item $$3) {
      return new TransmuteRecipeBuilder($$0, $$3.builtInRegistryHolder(), $$1, $$2);
   }

   public TransmuteRecipeBuilder unlockedBy(String $$0, Criterion<?> $$1) {
      this.criteria.put($$0, $$1);
      return this;
   }

   public TransmuteRecipeBuilder group(@Nullable String $$0) {
      this.group = $$0;
      return this;
   }

   @Override
   public Item getResult() {
      return (Item)this.result.value();
   }

   @Override
   public void save(RecipeOutput $$0, ResourceKey<Recipe<?>> $$1) {
      this.ensureValid($$1);
      Builder $$2 = $$0.advancement()
         .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked($$1))
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe($$1))
         .requirements(Strategy.OR);
      this.criteria.forEach($$2::addCriterion);
      TransmuteRecipe $$3 = new TransmuteRecipe(
         Objects.requireNonNullElse(this.group, ""),
         RecipeBuilder.determineBookCategory(this.category),
         this.input,
         this.material,
         new TransmuteResult((Item)this.result.value())
      );
      $$0.accept($$1, $$3, $$2.build($$1.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceKey<Recipe<?>> $$0) {
      if (this.criteria.isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + $$0.identifier());
      }
   }
}
