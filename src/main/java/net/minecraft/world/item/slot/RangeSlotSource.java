package net.minecraft.world.item.slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextArg;

public class RangeSlotSource implements SlotSource {
   public static final MapCodec<RangeSlotSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            LootContextArg.ENTITY_OR_BLOCK.fieldOf("source").forGetter($$0x -> $$0x.source),
            SlotRanges.CODEC.fieldOf("slots").forGetter($$0x -> $$0x.slotRange)
         )
         .apply($$0, RangeSlotSource::new)
   );
   private final LootContextArg<Object> source;
   private final SlotRange slotRange;

   private RangeSlotSource(LootContextArg<Object> $$0, SlotRange $$1) {
      this.source = $$0;
      this.slotRange = $$1;
   }

   @Override
   public MapCodec<RangeSlotSource> codec() {
      return MAP_CODEC;
   }

   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(this.source.contextParam());
   }

   @Override
   public final SlotCollection provide(LootContext $$0) {
      return this.source.get($$0) instanceof SlotProvider $$2 ? $$2.getSlotsFromRange(this.slotRange.slots()) : SlotCollection.EMPTY;
   }
}
