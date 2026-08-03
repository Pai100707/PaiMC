package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
   @Override
   public Codec<UsedTotemTrigger.TriggerInstance> codec() {
      return UsedTotemTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<UsedTotemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsedTotemTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(UsedTotemTrigger.TriggerInstance::item)
            )
            .apply($$0, UsedTotemTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<UsedTotemTrigger.TriggerInstance> usedTotem(ItemPredicate $$0) {
         return net.minecraft.advancements.CriteriaTriggers.USED_TOTEM
            .createCriterion(new UsedTotemTrigger.TriggerInstance(Optional.empty(), Optional.of($$0)));
      }

      public static net.minecraft.advancements.Criterion<UsedTotemTrigger.TriggerInstance> usedTotem(HolderGetter<Item> $$0, ItemLike $$1) {
         return net.minecraft.advancements.CriteriaTriggers.USED_TOTEM
            .createCriterion(new UsedTotemTrigger.TriggerInstance(Optional.empty(), Optional.of(ItemPredicate.Builder.item().of($$0, $$1).build())));
      }

      public boolean matches(ItemStack $$0) {
         return this.item.isEmpty() || this.item.get().test($$0);
      }
   }
}
