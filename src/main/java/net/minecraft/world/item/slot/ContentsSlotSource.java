package net.minecraft.world.item.slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;

public class ContentsSlotSource extends TransformedSlotSource {
   public static final MapCodec<ContentsSlotSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(ContainerComponentManipulators.CODEC.fieldOf("component").forGetter($$0x -> $$0x.component))
         .apply($$0, ContentsSlotSource::new)
   );
   private final ContainerComponentManipulator<?> component;

   private ContentsSlotSource(SlotSource $$0, ContainerComponentManipulator<?> $$1) {
      super($$0);
      this.component = $$1;
   }

   @Override
   public MapCodec<ContentsSlotSource> codec() {
      return MAP_CODEC;
   }

   @Override
   protected SlotCollection transform(SlotCollection $$0) {
      return $$0.flatMap(this.component::getSlots);
   }
}
