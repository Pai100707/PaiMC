package net.minecraft.world.item.slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ItemPredicate;

public class FilteredSlotSource extends TransformedSlotSource {
   public static final MapCodec<FilteredSlotSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(ItemPredicate.CODEC.fieldOf("item_filter").forGetter($$0x -> $$0x.filter)).apply($$0, FilteredSlotSource::new)
   );
   private final ItemPredicate filter;

   private FilteredSlotSource(SlotSource $$0, ItemPredicate $$1) {
      super($$0);
      this.filter = $$1;
   }

   @Override
   public MapCodec<FilteredSlotSource> codec() {
      return MAP_CODEC;
   }

   @Override
   protected SlotCollection transform(SlotCollection $$0) {
      return $$0.filter(this.filter);
   }
}
