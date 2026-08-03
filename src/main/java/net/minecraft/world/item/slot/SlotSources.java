package net.minecraft.world.item.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.LootContext;

public interface SlotSources {
   Codec<SlotSource> TYPED_CODEC = BuiltInRegistries.SLOT_SOURCE_TYPE.byNameCodec().dispatch(SlotSource::codec, $$0 -> $$0);
   Codec<SlotSource> CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(TYPED_CODEC, GroupSlotSource.INLINE_CODEC));

   static MapCodec<? extends SlotSource> bootstrap(Registry<MapCodec<? extends SlotSource>> $$0) {
      Registry.register($$0, "group", GroupSlotSource.MAP_CODEC);
      Registry.register($$0, "filtered", FilteredSlotSource.MAP_CODEC);
      Registry.register($$0, "limit_slots", LimitSlotSource.MAP_CODEC);
      Registry.register($$0, "slot_range", RangeSlotSource.MAP_CODEC);
      Registry.register($$0, "contents", ContentsSlotSource.MAP_CODEC);
      return (MapCodec<? extends SlotSource>)Registry.register($$0, "empty", EmptySlotSource.MAP_CODEC);
   }

   static Function<LootContext, SlotCollection> group(Collection<? extends SlotSource> $$0) {
      List<SlotSource> $$1 = List.copyOf($$0);

      return switch ($$1.size()) {
         case 0 -> $$0x -> SlotCollection.EMPTY;
         case 1 -> $$1.getFirst()::provide;
         case 2 -> {
            SlotSource $$2 = $$1.get(0);
            SlotSource $$3 = $$1.get(1);
            yield $$2x -> SlotCollection.concat($$2.provide($$2x), $$3.provide($$2x));
         }
         default -> $$1x -> {
            List<SlotCollection> $$2x = new ArrayList<>();

            for (SlotSource $$3x : $$1) {
               $$2x.add($$3x.provide($$1x));
            }

            return SlotCollection.concat($$2x);
         };
      };
   }
}
