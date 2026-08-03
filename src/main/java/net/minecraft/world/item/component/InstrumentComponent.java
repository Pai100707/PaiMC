package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record InstrumentComponent(net.minecraft.world.item.EitherHolder<net.minecraft.world.item.Instrument> instrument) implements TooltipProvider {
   public static final Codec<InstrumentComponent> CODEC = net.minecraft.world.item.EitherHolder.codec(
         Registries.INSTRUMENT, net.minecraft.world.item.Instrument.CODEC
      )
      .xmap(InstrumentComponent::new, InstrumentComponent::instrument);
   public static final StreamCodec<RegistryFriendlyByteBuf, InstrumentComponent> STREAM_CODEC = net.minecraft.world.item.EitherHolder.streamCodec(
         Registries.INSTRUMENT, net.minecraft.world.item.Instrument.STREAM_CODEC
      )
      .map(InstrumentComponent::new, InstrumentComponent::instrument);

   public InstrumentComponent(Holder<net.minecraft.world.item.Instrument> $$0) {
      this(new net.minecraft.world.item.EitherHolder<>($$0));
   }

   @Deprecated
   public InstrumentComponent(ResourceKey<net.minecraft.world.item.Instrument> $$0) {
      this(new net.minecraft.world.item.EitherHolder<>($$0));
   }

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      Provider $$4 = $$0.registries();
      if ($$4 != null) {
         this.unwrap($$4)
            .ifPresent(
               $$1x -> {
                  Component $$2x = ComponentUtils.mergeStyles(
                     ((net.minecraft.world.item.Instrument)$$1x.value()).description(), Style.EMPTY.withColor(ChatFormatting.GRAY)
                  );
                  $$1.accept($$2x);
               }
            );
      }
   }

   public Optional<Holder<net.minecraft.world.item.Instrument>> unwrap(Provider $$0) {
      return this.instrument.unwrap($$0);
   }
}
