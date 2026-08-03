package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipProvider;
import org.slf4j.Logger;

public record BannerPatternLayers(List<BannerPatternLayers.Layer> layers) implements TooltipProvider {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final BannerPatternLayers EMPTY = new BannerPatternLayers(List.of());
   public static final Codec<BannerPatternLayers> CODEC = BannerPatternLayers.Layer.CODEC.listOf().xmap(BannerPatternLayers::new, BannerPatternLayers::layers);
   public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers> STREAM_CODEC = BannerPatternLayers.Layer.STREAM_CODEC
      .apply(ByteBufCodecs.list())
      .map(BannerPatternLayers::new, BannerPatternLayers::layers);

   public BannerPatternLayers removeLast() {
      return new BannerPatternLayers(List.copyOf(this.layers.subList(0, this.layers.size() - 1)));
   }

   public void addToTooltip(TooltipContext $$0, Consumer<Component> $$1, TooltipFlag $$2, DataComponentGetter $$3) {
      for (int $$4 = 0; $$4 < Math.min(this.layers().size(), 6); $$4++) {
         $$1.accept(this.layers().get($$4).description().withStyle(ChatFormatting.GRAY));
      }
   }

   public static class Builder {
      private final com.google.common.collect.ImmutableList.Builder<BannerPatternLayers.Layer> layers = ImmutableList.builder();

      @Deprecated
      public BannerPatternLayers.Builder addIfRegistered(HolderGetter<BannerPattern> $$0, ResourceKey<BannerPattern> $$1, DyeColor $$2) {
         Optional<Reference<BannerPattern>> $$3 = $$0.get($$1);
         if ($$3.isEmpty()) {
            BannerPatternLayers.LOGGER.warn("Unable to find banner pattern with id: '{}'", $$1.identifier());
            return this;
         } else {
            return this.add((Holder<BannerPattern>)$$3.get(), $$2);
         }
      }

      public BannerPatternLayers.Builder add(Holder<BannerPattern> $$0, DyeColor $$1) {
         return this.add(new BannerPatternLayers.Layer($$0, $$1));
      }

      public BannerPatternLayers.Builder add(BannerPatternLayers.Layer $$0) {
         this.layers.add($$0);
         return this;
      }

      public BannerPatternLayers.Builder addAll(BannerPatternLayers $$0) {
         this.layers.addAll($$0.layers);
         return this;
      }

      public BannerPatternLayers build() {
         return new BannerPatternLayers(this.layers.build());
      }
   }

   public record Layer(Holder<BannerPattern> pattern, DyeColor color) {
      public static final Codec<BannerPatternLayers.Layer> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               BannerPattern.CODEC.fieldOf("pattern").forGetter(BannerPatternLayers.Layer::pattern),
               DyeColor.CODEC.fieldOf("color").forGetter(BannerPatternLayers.Layer::color)
            )
            .apply($$0, BannerPatternLayers.Layer::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers.Layer> STREAM_CODEC = StreamCodec.composite(
         BannerPattern.STREAM_CODEC,
         BannerPatternLayers.Layer::pattern,
         DyeColor.STREAM_CODEC,
         BannerPatternLayers.Layer::color,
         BannerPatternLayers.Layer::new
      );

      public MutableComponent description() {
         String $$0 = ((BannerPattern)this.pattern.value()).translationKey();
         return Component.translatable($$0 + "." + this.color.getName());
      }
   }
}
