package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
   @Override
   public Codec<TameAnimalTrigger.TriggerInstance> codec() {
      return TameAnimalTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Animal $$1) {
      LootContext $$2 = EntityPredicate.createContext($$0, $$1);
      this.trigger($$0, $$1x -> $$1x.matches($$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entity)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<TameAnimalTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TameAnimalTrigger.TriggerInstance::player),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TameAnimalTrigger.TriggerInstance::entity)
            )
            .apply($$0, TameAnimalTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<TameAnimalTrigger.TriggerInstance> tamedAnimal() {
         return net.minecraft.advancements.CriteriaTriggers.TAME_ANIMAL
            .createCriterion(new TameAnimalTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<TameAnimalTrigger.TriggerInstance> tamedAnimal(EntityPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.TAME_ANIMAL
            .createCriterion(new TameAnimalTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap($$0))));
      }

      public boolean matches(LootContext $$0) {
         return this.entity.isEmpty() || this.entity.get().matches($$0);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.entity, "entity");
      }
   }
}
