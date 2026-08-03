package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
   @Override
   public Codec<ItemDurabilityTrigger.TriggerInstance> codec() {
      return ItemDurabilityTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1, int $$2) {
      this.trigger($$0, $$2x -> $$2x.matches($$1, $$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, MinMaxBounds.Ints durability, MinMaxBounds.Ints delta)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<ItemDurabilityTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ItemDurabilityTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ItemDurabilityTrigger.TriggerInstance::item),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", MinMaxBounds.Ints.ANY).forGetter(ItemDurabilityTrigger.TriggerInstance::durability),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("delta", MinMaxBounds.Ints.ANY).forGetter(ItemDurabilityTrigger.TriggerInstance::delta)
            )
            .apply($$0, ItemDurabilityTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(
         Optional<ItemPredicate> $$0, MinMaxBounds.Ints $$1
      ) {
         return changedDurability(Optional.empty(), $$0, $$1);
      }

      public static net.minecraft.advancements.Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(
         Optional<ContextAwarePredicate> $$0, Optional<ItemPredicate> $$1, MinMaxBounds.Ints $$2
      ) {
         return net.minecraft.advancements.CriteriaTriggers.ITEM_DURABILITY_CHANGED
            .createCriterion(new ItemDurabilityTrigger.TriggerInstance($$0, $$1, $$2, MinMaxBounds.Ints.ANY));
      }

      public boolean matches(ItemStack $$0, int $$1) {
         if (this.item.isPresent() && !this.item.get().test($$0)) {
            return false;
         } else {
            return !this.durability.matches($$0.getMaxDamage() - $$1) ? false : this.delta.matches($$0.getDamageValue() - $$1);
         }
      }
   }
}
