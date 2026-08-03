package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import java.util.List;
import java.util.SequencedSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TooltipDisplay(boolean hideTooltip, SequencedSet<DataComponentType<?>> hiddenComponents) {
   private static final Codec<SequencedSet<DataComponentType<?>>> COMPONENT_SET_CODEC = DataComponentType.CODEC
      .listOf()
      .xmap(ReferenceLinkedOpenHashSet::new, List::copyOf);
   public static final Codec<TooltipDisplay> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(TooltipDisplay::hideTooltip),
            COMPONENT_SET_CODEC.optionalFieldOf("hidden_components", ReferenceSortedSets.emptySet()).forGetter(TooltipDisplay::hiddenComponents)
         )
         .apply($$0, TooltipDisplay::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, TooltipDisplay> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      TooltipDisplay::hideTooltip,
      DataComponentType.STREAM_CODEC.apply(ByteBufCodecs.collection(ReferenceLinkedOpenHashSet::new)),
      TooltipDisplay::hiddenComponents,
      TooltipDisplay::new
   );
   public static final TooltipDisplay DEFAULT = new TooltipDisplay(false, ReferenceSortedSets.emptySet());

   public TooltipDisplay withHidden(DataComponentType<?> $$0, boolean $$1) {
      if (this.hiddenComponents.contains($$0) == $$1) {
         return this;
      } else {
         SequencedSet<DataComponentType<?>> $$2 = new ReferenceLinkedOpenHashSet(this.hiddenComponents);
         if ($$1) {
            $$2.add($$0);
         } else {
            $$2.remove($$0);
         }

         return new TooltipDisplay(this.hideTooltip, $$2);
      }
   }

   public boolean shows(DataComponentType<?> $$0) {
      return !this.hideTooltip && !this.hiddenComponents.contains($$0);
   }
}
