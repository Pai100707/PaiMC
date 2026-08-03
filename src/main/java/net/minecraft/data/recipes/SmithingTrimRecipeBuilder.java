package net.minecraft.data.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public class SmithingTrimRecipeBuilder {
   private final RecipeCategory category;
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final Holder<TrimPattern> pattern;
   private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

   public SmithingTrimRecipeBuilder(RecipeCategory $$0, Ingredient $$1, Ingredient $$2, Ingredient $$3, Holder<TrimPattern> $$4) {
      this.category = $$0;
      this.template = $$1;
      this.base = $$2;
      this.addition = $$3;
      this.pattern = $$4;
   }

   public static SmithingTrimRecipeBuilder smithingTrim(Ingredient $$0, Ingredient $$1, Ingredient $$2, Holder<TrimPattern> $$3, RecipeCategory $$4) {
      return new SmithingTrimRecipeBuilder($$4, $$0, $$1, $$2, $$3);
   }

   public SmithingTrimRecipeBuilder unlocks(String $$0, Criterion<?> $$1) {
      this.criteria.put($$0, $$1);
      return this;
   }

   public void save(RecipeOutput $$0, ResourceKey<Recipe<?>> $$1) {
      this.ensureValid($$1);
      Builder $$2 = $$0.advancement()
         .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked($$1))
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe($$1))
         .requirements(Strategy.OR);
      this.criteria.forEach($$2::addCriterion);
      SmithingTrimRecipe $$3 = new SmithingTrimRecipe(this.template, this.base, this.addition, this.pattern);
      $$0.accept($$1, $$3, $$2.build($$1.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceKey<Recipe<?>> $$0) {
      if (this.criteria.isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + $$0.identifier());
      }
   }
}
