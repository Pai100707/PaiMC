package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
   @Override
   public Codec<BrewedPotionTrigger.TriggerInstance> codec() {
      return BrewedPotionTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Holder<Potion> $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Potion>> potion) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<BrewedPotionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(BrewedPotionTrigger.TriggerInstance::player),
               Potion.CODEC.optionalFieldOf("potion").forGetter(BrewedPotionTrigger.TriggerInstance::potion)
            )
            .apply($$0, BrewedPotionTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<BrewedPotionTrigger.TriggerInstance> brewedPotion() {
         return net.minecraft.advancements.CriteriaTriggers.BREWED_POTION
            .createCriterion(new BrewedPotionTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public boolean matches(Holder<Potion> $$0) {
         return !this.potion.isPresent() || this.potion.get().equals($$0);
      }
   }
}
