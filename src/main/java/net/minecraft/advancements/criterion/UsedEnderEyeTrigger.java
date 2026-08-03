package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
   @Override
   public Codec<UsedEnderEyeTrigger.TriggerInstance> codec() {
      return UsedEnderEyeTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, BlockPos $$1) {
      double $$2 = $$0.getX() - $$1.getX();
      double $$3 = $$0.getZ() - $$1.getZ();
      double $$4 = $$2 * $$2 + $$3 * $$3;
      this.trigger($$0, $$1x -> $$1x.matches($$4));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Doubles distance) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<UsedEnderEyeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsedEnderEyeTrigger.TriggerInstance::player),
               MinMaxBounds.Doubles.CODEC.optionalFieldOf("distance", MinMaxBounds.Doubles.ANY).forGetter(UsedEnderEyeTrigger.TriggerInstance::distance)
            )
            .apply($$0, UsedEnderEyeTrigger.TriggerInstance::new)
      );

      public boolean matches(double $$0) {
         return this.distance.matchesSqr($$0);
      }
   }
}
