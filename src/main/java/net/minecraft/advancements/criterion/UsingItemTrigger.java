package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
   @Override
   public Codec<UsingItemTrigger.TriggerInstance> codec() {
      return UsingItemTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<UsingItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsingItemTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(UsingItemTrigger.TriggerInstance::item)
            )
            .apply($$0, UsingItemTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<UsingItemTrigger.TriggerInstance> lookingAt(EntityPredicate.Builder $$0, ItemPredicate.Builder $$1) {
         return net.minecraft.advancements.CriteriaTriggers.USING_ITEM
            .createCriterion(new UsingItemTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap($$0)), Optional.of($$1.build())));
      }

      public boolean matches(ItemStack $$0) {
         return !this.item.isPresent() || this.item.get().test($$0);
      }
   }
}
