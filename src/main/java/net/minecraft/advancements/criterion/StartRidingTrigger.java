package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

public class StartRidingTrigger extends SimpleCriterionTrigger<StartRidingTrigger.TriggerInstance> {
   @Override
   public Codec<StartRidingTrigger.TriggerInstance> codec() {
      return StartRidingTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0) {
      this.trigger($$0, $$0x -> true);
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<StartRidingTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(StartRidingTrigger.TriggerInstance::player))
            .apply($$0, StartRidingTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<StartRidingTrigger.TriggerInstance> playerStartsRiding(EntityPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.START_RIDING_TRIGGER
            .createCriterion(new StartRidingTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap($$0))));
      }
   }
}
