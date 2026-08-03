package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
   @Override
   public Codec<ChangeDimensionTrigger.TriggerInstance> codec() {
      return ChangeDimensionTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ResourceKey<Level> $$1, ResourceKey<Level> $$2) {
      this.trigger($$0, $$2x -> $$2x.matches($$1, $$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Level>> from, Optional<ResourceKey<Level>> to)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<ChangeDimensionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ChangeDimensionTrigger.TriggerInstance::player),
               ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("from").forGetter(ChangeDimensionTrigger.TriggerInstance::from),
               ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("to").forGetter(ChangeDimensionTrigger.TriggerInstance::to)
            )
            .apply($$0, ChangeDimensionTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension() {
         return net.minecraft.advancements.CriteriaTriggers.CHANGED_DIMENSION
            .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension(
         ResourceKey<Level> $$0, ResourceKey<Level> $$1
      ) {
         return net.minecraft.advancements.CriteriaTriggers.CHANGED_DIMENSION
            .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.of($$0), Optional.of($$1)));
      }

      public static net.minecraft.advancements.Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionTo(ResourceKey<Level> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.CHANGED_DIMENSION
            .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of($$0)));
      }

      public static net.minecraft.advancements.Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionFrom(ResourceKey<Level> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.CHANGED_DIMENSION
            .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.of($$0), Optional.empty()));
      }

      public boolean matches(ResourceKey<Level> $$0, ResourceKey<Level> $$1) {
         return this.from.isPresent() && this.from.get() != $$0 ? false : !this.to.isPresent() || this.to.get() == $$1;
      }
   }
}
