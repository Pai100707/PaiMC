package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
   @Override
   public Codec<EnchantedItemTrigger.TriggerInstance> codec() {
      return EnchantedItemTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1, int $$2) {
      this.trigger($$0, $$2x -> $$2x.matches($$1, $$2));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, MinMaxBounds.Ints levels)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<EnchantedItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EnchantedItemTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(EnchantedItemTrigger.TriggerInstance::item),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", MinMaxBounds.Ints.ANY).forGetter(EnchantedItemTrigger.TriggerInstance::levels)
            )
            .apply($$0, EnchantedItemTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<EnchantedItemTrigger.TriggerInstance> enchantedItem() {
         return net.minecraft.advancements.CriteriaTriggers.ENCHANTED_ITEM
            .createCriterion(new EnchantedItemTrigger.TriggerInstance(Optional.empty(), Optional.empty(), MinMaxBounds.Ints.ANY));
      }

      public boolean matches(ItemStack $$0, int $$1) {
         return this.item.isPresent() && !this.item.get().test($$0) ? false : this.levels.matches($$1);
      }
   }
}
