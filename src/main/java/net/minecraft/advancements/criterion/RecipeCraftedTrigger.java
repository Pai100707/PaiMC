package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
   @Override
   public Codec<RecipeCraftedTrigger.TriggerInstance> codec() {
      return RecipeCraftedTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ResourceKey<Recipe<?>> $$1, List<ItemStack> $$2) {
      this.trigger($$0, $$2x -> $$2x.matches($$1, $$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipeId, List<ItemPredicate> ingredients)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<RecipeCraftedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeCraftedTrigger.TriggerInstance::player),
               Recipe.KEY_CODEC.fieldOf("recipe_id").forGetter(RecipeCraftedTrigger.TriggerInstance::recipeId),
               ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(RecipeCraftedTrigger.TriggerInstance::ingredients)
            )
            .apply($$0, RecipeCraftedTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(
         ResourceKey<Recipe<?>> $$0, List<ItemPredicate.Builder> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.RECIPE_CRAFTED
            .createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), $$0, $$1.stream().map(ItemPredicate.Builder::build).toList()));
      }

      public static net.minecraft.advancements.Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceKey<Recipe<?>> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.RECIPE_CRAFTED
            .createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), $$0, List.of()));
      }

      public static net.minecraft.advancements.Criterion<RecipeCraftedTrigger.TriggerInstance> crafterCraftedItem(ResourceKey<Recipe<?>> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.CRAFTER_RECIPE_CRAFTED
            .createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), $$0, List.of()));
      }

      boolean matches(ResourceKey<Recipe<?>> $$0, List<ItemStack> $$1) {
         if ($$0 != this.recipeId) {
            return false;
         } else {
            List<ItemStack> $$2 = new ArrayList<>($$1);

            for (ItemPredicate $$3 : this.ingredients) {
               boolean $$4 = false;
               Iterator<ItemStack> $$5 = $$2.iterator();

               while ($$5.hasNext()) {
                  if ($$3.test($$5.next())) {
                     $$5.remove();
                     $$4 = true;
                     break;
                  }
               }

               if (!$$4) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
