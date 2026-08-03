package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
   @Override
   public Codec<RecipeUnlockedTrigger.TriggerInstance> codec() {
      return RecipeUnlockedTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, RecipeHolder<?> $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public static net.minecraft.advancements.Criterion<RecipeUnlockedTrigger.TriggerInstance> unlocked(ResourceKey<Recipe<?>> $$0) {
      return net.minecraft.advancements.CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new RecipeUnlockedTrigger.TriggerInstance(Optional.empty(), $$0));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipe) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<RecipeUnlockedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeUnlockedTrigger.TriggerInstance::player),
               Recipe.KEY_CODEC.fieldOf("recipe").forGetter(RecipeUnlockedTrigger.TriggerInstance::recipe)
            )
            .apply($$0, RecipeUnlockedTrigger.TriggerInstance::new)
      );

      public boolean matches(RecipeHolder<?> $$0) {
         return this.recipe == $$0.id();
      }
   }
}
