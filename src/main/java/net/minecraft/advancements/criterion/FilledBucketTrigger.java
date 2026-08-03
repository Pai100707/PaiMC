package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FilledBucketTrigger extends SimpleCriterionTrigger<FilledBucketTrigger.TriggerInstance> {
   @Override
   public Codec<FilledBucketTrigger.TriggerInstance> codec() {
      return FilledBucketTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<FilledBucketTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(FilledBucketTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(FilledBucketTrigger.TriggerInstance::item)
            )
            .apply($$0, FilledBucketTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<FilledBucketTrigger.TriggerInstance> filledBucket(ItemPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.FILLED_BUCKET
            .createCriterion(new FilledBucketTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.build())));
      }

      public boolean matches(ItemStack $$0) {
         return !this.item.isPresent() || this.item.get().test($$0);
      }
   }
}
