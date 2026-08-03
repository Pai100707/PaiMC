package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
   @Override
   public Codec<DistanceTrigger.TriggerInstance> codec() {
      return DistanceTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Vec3 $$1) {
      Vec3 $$2 = $$0.position();
      this.trigger($$0, $$3 -> $$3.matches($$0.level(), $$1, $$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<DistanceTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(DistanceTrigger.TriggerInstance::player),
               LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(DistanceTrigger.TriggerInstance::startPosition),
               DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(DistanceTrigger.TriggerInstance::distance)
            )
            .apply($$0, DistanceTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<DistanceTrigger.TriggerInstance> fallFromHeight(
         EntityPredicate.Builder $$0, DistancePredicate $$1, LocationPredicate.Builder $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.FALL_FROM_HEIGHT
            .createCriterion(new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap($$0)), Optional.of($$2.build()), Optional.of($$1)));
      }

      public static net.minecraft.advancements.Criterion<DistanceTrigger.TriggerInstance> rideEntityInLava(EntityPredicate.Builder $$0, DistancePredicate $$1) {
         return net.minecraft.advancements.CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER
            .createCriterion(new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap($$0)), Optional.empty(), Optional.of($$1)));
      }

      public static net.minecraft.advancements.Criterion<DistanceTrigger.TriggerInstance> travelledThroughNether(DistancePredicate $$0) {
         return net.minecraft.advancements.CriteriaTriggers.NETHER_TRAVEL
            .createCriterion(new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of($$0)));
      }

      public boolean matches(ServerLevel $$0, Vec3 $$1, Vec3 $$2) {
         return this.startPosition.isPresent() && !this.startPosition.get().matches($$0, $$1.x, $$1.y, $$1.z)
            ? false
            : !this.distance.isPresent() || this.distance.get().matches($$1.x, $$1.y, $$1.z, $$2.x, $$2.y, $$2.z);
      }
   }
}
