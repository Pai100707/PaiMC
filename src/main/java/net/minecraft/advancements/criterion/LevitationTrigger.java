package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
   @Override
   public Codec<LevitationTrigger.TriggerInstance> codec() {
      return LevitationTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Vec3 $$1, int $$2) {
      this.trigger($$0, $$3 -> $$3.matches($$0, $$1, $$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DistancePredicate> distance, MinMaxBounds.Ints duration)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<LevitationTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LevitationTrigger.TriggerInstance::player),
               DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(LevitationTrigger.TriggerInstance::distance),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", MinMaxBounds.Ints.ANY).forGetter(LevitationTrigger.TriggerInstance::duration)
            )
            .apply($$0, LevitationTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<LevitationTrigger.TriggerInstance> levitated(DistancePredicate $$0) {
         return net.minecraft.advancements.CriteriaTriggers.LEVITATION
            .createCriterion(new LevitationTrigger.TriggerInstance(Optional.empty(), Optional.of($$0), MinMaxBounds.Ints.ANY));
      }

      public boolean matches(ServerPlayer $$0, Vec3 $$1, int $$2) {
         return this.distance.isPresent() && !this.distance.get().matches($$1.x, $$1.y, $$1.z, $$0.getX(), $$0.getY(), $$0.getZ())
            ? false
            : this.duration.matches($$2);
      }
   }
}
