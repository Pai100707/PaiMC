package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class SpearMobsTrigger extends SimpleCriterionTrigger<SpearMobsTrigger.TriggerInstance> {
   @Override
   public Codec<SpearMobsTrigger.TriggerInstance> codec() {
      return SpearMobsTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, int $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Integer> count) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<SpearMobsTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SpearMobsTrigger.TriggerInstance::player),
               ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(SpearMobsTrigger.TriggerInstance::count)
            )
            .apply($$0, SpearMobsTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<SpearMobsTrigger.TriggerInstance> spearMobs(int $$0) {
         return net.minecraft.advancements.CriteriaTriggers.SPEAR_MOBS_TRIGGER
            .createCriterion(new SpearMobsTrigger.TriggerInstance(Optional.empty(), Optional.of($$0)));
      }

      public boolean matches(int $$0) {
         return this.count.isEmpty() || $$0 >= this.count.get();
      }
   }
}
