package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
   @Override
   public Codec<BredAnimalsTrigger.TriggerInstance> codec() {
      return BredAnimalsTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Animal $$1, Animal $$2, AgeableMob $$3) {
      LootContext $$4 = EntityPredicate.createContext($$0, $$1);
      LootContext $$5 = EntityPredicate.createContext($$0, $$2);
      LootContext $$6 = $$3 != null ? EntityPredicate.createContext($$0, $$3) : null;
      this.trigger($$0, $$3x -> $$3x.matches($$4, $$5, $$6));
   }

   public record TriggerInstance(
      Optional<ContextAwarePredicate> player,
      Optional<ContextAwarePredicate> parent,
      Optional<ContextAwarePredicate> partner,
      Optional<ContextAwarePredicate> child
   ) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<BredAnimalsTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(BredAnimalsTrigger.TriggerInstance::player),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent").forGetter(BredAnimalsTrigger.TriggerInstance::parent),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("partner").forGetter(BredAnimalsTrigger.TriggerInstance::partner),
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("child").forGetter(BredAnimalsTrigger.TriggerInstance::child)
            )
            .apply($$0, BredAnimalsTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals() {
         return net.minecraft.advancements.CriteriaTriggers.BRED_ANIMALS
            .createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(EntityPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.BRED_ANIMALS
            .createCriterion(
               new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap($$0)))
            );
      }

      public static net.minecraft.advancements.Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(
         Optional<EntityPredicate> $$0, Optional<EntityPredicate> $$1, Optional<EntityPredicate> $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.BRED_ANIMALS
            .createCriterion(
               new BredAnimalsTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0), EntityPredicate.wrap($$1), EntityPredicate.wrap($$2))
            );
      }

      public boolean matches(LootContext $$0, LootContext $$1, LootContext $$2) {
         return !this.child.isPresent() || $$2 != null && this.child.get().matches($$2)
            ? matches(this.parent, $$0) && matches(this.partner, $$1) || matches(this.parent, $$1) && matches(this.partner, $$0)
            : false;
      }

      private static boolean matches(Optional<ContextAwarePredicate> $$0, LootContext $$1) {
         return $$0.isEmpty() || $$0.get().matches($$1);
      }

      @Override
      public void validate(CriterionValidator $$0) {
         SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
         $$0.validateEntity(this.parent, "parent");
         $$0.validateEntity(this.partner, "partner");
         $$0.validateEntity(this.child, "child");
      }
   }
}
