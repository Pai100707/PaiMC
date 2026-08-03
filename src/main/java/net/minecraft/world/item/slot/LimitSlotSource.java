package net.minecraft.world.item.slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public class LimitSlotSource extends TransformedSlotSource {
   public static final MapCodec<LimitSlotSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(ExtraCodecs.POSITIVE_INT.fieldOf("limit").forGetter($$0x -> $$0x.limit)).apply($$0, LimitSlotSource::new)
   );
   private final int limit;

   private LimitSlotSource(SlotSource $$0, int $$1) {
      super($$0);
      this.limit = $$1;
   }

   @Override
   public MapCodec<LimitSlotSource> codec() {
      return MAP_CODEC;
   }

   @Override
   protected SlotCollection transform(SlotCollection $$0) {
      return $$0.limit(this.limit);
   }
}
