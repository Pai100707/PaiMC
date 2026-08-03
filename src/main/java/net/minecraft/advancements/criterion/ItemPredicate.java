package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemPredicate(Optional<HolderSet<Item>> items, MinMaxBounds.Ints count, DataComponentMatchers components) implements Predicate<ItemStack> {
   public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count),
            DataComponentMatchers.CODEC.forGetter(ItemPredicate::components)
         )
         .apply($$0, ItemPredicate::new)
   );

   public boolean test(ItemStack $$0) {
      if (this.items.isPresent() && !$$0.is(this.items.get())) {
         return false;
      } else {
         return !this.count.matches($$0.getCount()) ? false : this.components.test((DataComponentGetter)$$0);
      }
   }

   public static class Builder {
      private Optional<HolderSet<Item>> items = Optional.empty();
      private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
      private DataComponentMatchers components = DataComponentMatchers.ANY;

      public static ItemPredicate.Builder item() {
         return new ItemPredicate.Builder();
      }

      public ItemPredicate.Builder of(HolderGetter<Item> $$0, ItemLike... $$1) {
         this.items = Optional.of(HolderSet.direct($$0x -> $$0x.asItem().builtInRegistryHolder(), $$1));
         return this;
      }

      public ItemPredicate.Builder of(HolderGetter<Item> $$0, TagKey<Item> $$1) {
         this.items = Optional.of($$0.getOrThrow($$1));
         return this;
      }

      public ItemPredicate.Builder withCount(MinMaxBounds.Ints $$0) {
         this.count = $$0;
         return this;
      }

      public ItemPredicate.Builder withComponents(DataComponentMatchers $$0) {
         this.components = $$0;
         return this;
      }

      public ItemPredicate build() {
         return new ItemPredicate(this.items, this.count, this.components);
      }
   }
}
