package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
   @Override
   public Codec<ConsumeItemTrigger.TriggerInstance> codec() {
      return ConsumeItemTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<ConsumeItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ConsumeItemTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ConsumeItemTrigger.TriggerInstance::item)
            )
            .apply($$0, ConsumeItemTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<ConsumeItemTrigger.TriggerInstance> usedItem() {
         return net.minecraft.advancements.CriteriaTriggers.CONSUME_ITEM
            .createCriterion(new ConsumeItemTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.Criterion<ConsumeItemTrigger.TriggerInstance> usedItem(HolderGetter<Item> $$0, ItemLike $$1) {
         return usedItem(ItemPredicate.Builder.item().of($$0, $$1.asItem()));
      }

      public static net.minecraft.advancements.Criterion<ConsumeItemTrigger.TriggerInstance> usedItem(ItemPredicate.Builder $$0) {
         return net.minecraft.advancements.CriteriaTriggers.CONSUME_ITEM
            .createCriterion(new ConsumeItemTrigger.TriggerInstance(Optional.empty(), Optional.of($$0.build())));
      }

      public boolean matches(ItemStack $$0) {
         return this.item.isEmpty() || this.item.get().test($$0);
      }
   }
}
