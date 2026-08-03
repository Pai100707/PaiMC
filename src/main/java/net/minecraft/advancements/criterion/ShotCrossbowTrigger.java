package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
   @Override
   public Codec<ShotCrossbowTrigger.TriggerInstance> codec() {
      return ShotCrossbowTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, ItemStack $$1) {
      this.trigger($$0, $$1x -> $$1x.matches($$1));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<ShotCrossbowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ShotCrossbowTrigger.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ShotCrossbowTrigger.TriggerInstance::item)
            )
            .apply($$0, ShotCrossbowTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(Optional<ItemPredicate> $$0) {
         return net.minecraft.advancements.CriteriaTriggers.SHOT_CROSSBOW.createCriterion(new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), $$0));
      }

      public static net.minecraft.advancements.Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(HolderGetter<Item> $$0, ItemLike $$1) {
         return net.minecraft.advancements.CriteriaTriggers.SHOT_CROSSBOW
            .createCriterion(new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), Optional.of(ItemPredicate.Builder.item().of($$0, $$1).build())));
      }

      public boolean matches(ItemStack $$0) {
         return this.item.isEmpty() || this.item.get().test($$0);
      }
   }
}
