package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
   @Override
   public Codec<ConstructBeaconTrigger.TriggerInstance> codec() {
      return ConstructBeaconTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, int $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints level) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<ConstructBeaconTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ConstructBeaconTrigger.TriggerInstance::player),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("level", MinMaxBounds.Ints.ANY).forGetter(ConstructBeaconTrigger.TriggerInstance::level)
            )
            .apply($$0, ConstructBeaconTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<ConstructBeaconTrigger.TriggerInstance> constructedBeacon() {
         return net.minecraft.advancements.CriteriaTriggers.CONSTRUCT_BEACON
            .createCriterion(new ConstructBeaconTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY));
      }

      public static net.minecraft.advancements.Criterion<ConstructBeaconTrigger.TriggerInstance> constructedBeacon(MinMaxBounds.Ints $$0) {
         return net.minecraft.advancements.CriteriaTriggers.CONSTRUCT_BEACON.createCriterion(new ConstructBeaconTrigger.TriggerInstance(Optional.empty(), $$0));
      }

      public boolean matches(int $$0) {
         return this.level.matches($$0);
      }
   }
}
