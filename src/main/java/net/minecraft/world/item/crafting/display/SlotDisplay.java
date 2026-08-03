package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.block.entity.FuelValues;

public interface SlotDisplay {
   Codec<SlotDisplay> CODEC = BuiltInRegistries.SLOT_DISPLAY.byNameCodec().dispatch(SlotDisplay::type, SlotDisplay.Type::codec);
   StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> STREAM_CODEC = ByteBufCodecs.registry(Registries.SLOT_DISPLAY)
      .dispatch(SlotDisplay::type, SlotDisplay.Type::streamCodec);

   <T> Stream<T> resolve(ContextMap var1, DisplayContentsFactory<T> var2);

   SlotDisplay.Type<? extends SlotDisplay> type();

   default boolean isEnabled(FeatureFlagSet $$0) {
      return true;
   }

   default List<net.minecraft.world.item.ItemStack> resolveForStacks(ContextMap $$0) {
      return this.resolve($$0, SlotDisplay.ItemStackContentsFactory.INSTANCE).toList();
   }

   default net.minecraft.world.item.ItemStack resolveForFirstStack(ContextMap $$0) {
      return this.resolve($$0, SlotDisplay.ItemStackContentsFactory.INSTANCE).findFirst().orElse(net.minecraft.world.item.ItemStack.EMPTY);
   }

   public static class AnyFuel implements SlotDisplay {
      public static final SlotDisplay.AnyFuel INSTANCE = new SlotDisplay.AnyFuel();
      public static final MapCodec<SlotDisplay.AnyFuel> MAP_CODEC = MapCodec.unit(INSTANCE);
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.AnyFuel> STREAM_CODEC = StreamCodec.unit(INSTANCE);
      public static final SlotDisplay.Type<SlotDisplay.AnyFuel> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      private AnyFuel() {
      }

      @Override
      public SlotDisplay.Type<SlotDisplay.AnyFuel> type() {
         return TYPE;
      }

      @Override
      public String toString() {
         return "<any fuel>";
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         if ($$1 instanceof DisplayContentsFactory.ForStacks<T> $$2) {
            FuelValues $$3 = (FuelValues)$$0.getOptional(SlotDisplayContext.FUEL_VALUES);
            if ($$3 != null) {
               return $$3.fuelItems().stream().map($$2::forStack);
            }
         }

         return Stream.empty();
      }
   }

   public record Composite(List<SlotDisplay> contents) implements SlotDisplay {
      public static final MapCodec<SlotDisplay.Composite> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(SlotDisplay.CODEC.listOf().fieldOf("contents").forGetter(SlotDisplay.Composite::contents)).apply($$0, SlotDisplay.Composite::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.Composite> STREAM_CODEC = StreamCodec.composite(
         SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), SlotDisplay.Composite::contents, SlotDisplay.Composite::new
      );
      public static final SlotDisplay.Type<SlotDisplay.Composite> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      @Override
      public SlotDisplay.Type<SlotDisplay.Composite> type() {
         return TYPE;
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         return this.contents.stream().flatMap($$2 -> $$2.resolve($$0, $$1));
      }

      @Override
      public boolean isEnabled(FeatureFlagSet $$0) {
         return this.contents.stream().allMatch($$1 -> $$1.isEnabled($$0));
      }
   }

   public static class Empty implements SlotDisplay {
      public static final SlotDisplay.Empty INSTANCE = new SlotDisplay.Empty();
      public static final MapCodec<SlotDisplay.Empty> MAP_CODEC = MapCodec.unit(INSTANCE);
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.Empty> STREAM_CODEC = StreamCodec.unit(INSTANCE);
      public static final SlotDisplay.Type<SlotDisplay.Empty> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      private Empty() {
      }

      @Override
      public SlotDisplay.Type<SlotDisplay.Empty> type() {
         return TYPE;
      }

      @Override
      public String toString() {
         return "<empty>";
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         return Stream.empty();
      }
   }

   public record ItemSlotDisplay(Holder<net.minecraft.world.item.Item> item) implements SlotDisplay {
      public static final MapCodec<SlotDisplay.ItemSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(net.minecraft.world.item.Item.CODEC.fieldOf("item").forGetter(SlotDisplay.ItemSlotDisplay::item))
            .apply($$0, SlotDisplay.ItemSlotDisplay::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.ItemSlotDisplay> STREAM_CODEC = StreamCodec.composite(
         net.minecraft.world.item.Item.STREAM_CODEC, SlotDisplay.ItemSlotDisplay::item, SlotDisplay.ItemSlotDisplay::new
      );
      public static final SlotDisplay.Type<SlotDisplay.ItemSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      public ItemSlotDisplay(net.minecraft.world.item.Item $$0) {
         this($$0.builtInRegistryHolder());
      }

      @Override
      public SlotDisplay.Type<SlotDisplay.ItemSlotDisplay> type() {
         return TYPE;
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         return $$1 instanceof DisplayContentsFactory.ForStacks<T> $$2 ? Stream.of($$2.forStack(this.item)) : Stream.empty();
      }

      @Override
      public boolean isEnabled(FeatureFlagSet $$0) {
         return ((net.minecraft.world.item.Item)this.item.value()).isEnabled($$0);
      }
   }

   public static class ItemStackContentsFactory implements DisplayContentsFactory.ForStacks<net.minecraft.world.item.ItemStack> {
      public static final SlotDisplay.ItemStackContentsFactory INSTANCE = new SlotDisplay.ItemStackContentsFactory();

      public net.minecraft.world.item.ItemStack forStack(net.minecraft.world.item.ItemStack $$0) {
         return $$0;
      }
   }

   public record ItemStackSlotDisplay(net.minecraft.world.item.ItemStack stack) implements SlotDisplay {
      public static final MapCodec<SlotDisplay.ItemStackSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(net.minecraft.world.item.ItemStack.STRICT_CODEC.fieldOf("item").forGetter(SlotDisplay.ItemStackSlotDisplay::stack))
            .apply($$0, SlotDisplay.ItemStackSlotDisplay::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.ItemStackSlotDisplay> STREAM_CODEC = StreamCodec.composite(
         net.minecraft.world.item.ItemStack.STREAM_CODEC, SlotDisplay.ItemStackSlotDisplay::stack, SlotDisplay.ItemStackSlotDisplay::new
      );
      public static final SlotDisplay.Type<SlotDisplay.ItemStackSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      @Override
      public SlotDisplay.Type<SlotDisplay.ItemStackSlotDisplay> type() {
         return TYPE;
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         return $$1 instanceof DisplayContentsFactory.ForStacks<T> $$2 ? Stream.of($$2.forStack(this.stack)) : Stream.empty();
      }

      @Override
      public boolean equals(Object $$0) {
         return this == $$0 || $$0 instanceof SlotDisplay.ItemStackSlotDisplay $$1 && net.minecraft.world.item.ItemStack.matches(this.stack, $$1.stack);
      }

      @Override
      public boolean isEnabled(FeatureFlagSet $$0) {
         return this.stack.getItem().isEnabled($$0);
      }
   }

   public record SmithingTrimDemoSlotDisplay(SlotDisplay base, SlotDisplay material, Holder<TrimPattern> pattern) implements SlotDisplay {
      public static final MapCodec<SlotDisplay.SmithingTrimDemoSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               SlotDisplay.CODEC.fieldOf("base").forGetter(SlotDisplay.SmithingTrimDemoSlotDisplay::base),
               SlotDisplay.CODEC.fieldOf("material").forGetter(SlotDisplay.SmithingTrimDemoSlotDisplay::material),
               TrimPattern.CODEC.fieldOf("pattern").forGetter(SlotDisplay.SmithingTrimDemoSlotDisplay::pattern)
            )
            .apply($$0, SlotDisplay.SmithingTrimDemoSlotDisplay::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.SmithingTrimDemoSlotDisplay> STREAM_CODEC = StreamCodec.composite(
         SlotDisplay.STREAM_CODEC,
         SlotDisplay.SmithingTrimDemoSlotDisplay::base,
         SlotDisplay.STREAM_CODEC,
         SlotDisplay.SmithingTrimDemoSlotDisplay::material,
         TrimPattern.STREAM_CODEC,
         SlotDisplay.SmithingTrimDemoSlotDisplay::pattern,
         SlotDisplay.SmithingTrimDemoSlotDisplay::new
      );
      public static final SlotDisplay.Type<SlotDisplay.SmithingTrimDemoSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      @Override
      public SlotDisplay.Type<SlotDisplay.SmithingTrimDemoSlotDisplay> type() {
         return TYPE;
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         if ($$1 instanceof DisplayContentsFactory.ForStacks<T> $$2) {
            Provider $$3 = (Provider)$$0.getOptional(SlotDisplayContext.REGISTRIES);
            if ($$3 != null) {
               RandomSource $$4 = RandomSource.create(System.identityHashCode(this));
               List<net.minecraft.world.item.ItemStack> $$5 = this.base.resolveForStacks($$0);
               if ($$5.isEmpty()) {
                  return Stream.empty();
               }

               List<net.minecraft.world.item.ItemStack> $$6 = this.material.resolveForStacks($$0);
               if ($$6.isEmpty()) {
                  return Stream.empty();
               }

               return Stream.<net.minecraft.world.item.ItemStack>generate(() -> {
                  net.minecraft.world.item.ItemStack $$4x = (net.minecraft.world.item.ItemStack)Util.getRandom($$5, $$4);
                  net.minecraft.world.item.ItemStack $$5x = (net.minecraft.world.item.ItemStack)Util.getRandom($$6, $$4);
                  return SmithingTrimRecipe.applyTrim($$3, $$4x, $$5x, this.pattern);
               }).limit(256L).filter($$0x -> !$$0x.isEmpty()).limit(16L).map($$2::forStack);
            }
         }

         return Stream.empty();
      }
   }

   public record TagSlotDisplay(TagKey<net.minecraft.world.item.Item> tag) implements SlotDisplay {
      public static final MapCodec<SlotDisplay.TagSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(SlotDisplay.TagSlotDisplay::tag)).apply($$0, SlotDisplay.TagSlotDisplay::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.TagSlotDisplay> STREAM_CODEC = StreamCodec.composite(
         TagKey.streamCodec(Registries.ITEM), SlotDisplay.TagSlotDisplay::tag, SlotDisplay.TagSlotDisplay::new
      );
      public static final SlotDisplay.Type<SlotDisplay.TagSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      @Override
      public SlotDisplay.Type<SlotDisplay.TagSlotDisplay> type() {
         return TYPE;
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         if ($$1 instanceof DisplayContentsFactory.ForStacks<T> $$2) {
            Provider $$3 = (Provider)$$0.getOptional(SlotDisplayContext.REGISTRIES);
            if ($$3 != null) {
               return $$3.lookupOrThrow(Registries.ITEM).get(this.tag).map($$1x -> $$1x.stream().map($$2::forStack)).stream().flatMap($$0x -> $$0x);
            }
         }

         return Stream.empty();
      }
   }

   public record Type<T extends SlotDisplay>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
   }

   public record WithRemainder(SlotDisplay input, SlotDisplay remainder) implements SlotDisplay {
      public static final MapCodec<SlotDisplay.WithRemainder> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               SlotDisplay.CODEC.fieldOf("input").forGetter(SlotDisplay.WithRemainder::input),
               SlotDisplay.CODEC.fieldOf("remainder").forGetter(SlotDisplay.WithRemainder::remainder)
            )
            .apply($$0, SlotDisplay.WithRemainder::new)
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, SlotDisplay.WithRemainder> STREAM_CODEC = StreamCodec.composite(
         SlotDisplay.STREAM_CODEC,
         SlotDisplay.WithRemainder::input,
         SlotDisplay.STREAM_CODEC,
         SlotDisplay.WithRemainder::remainder,
         SlotDisplay.WithRemainder::new
      );
      public static final SlotDisplay.Type<SlotDisplay.WithRemainder> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

      @Override
      public SlotDisplay.Type<SlotDisplay.WithRemainder> type() {
         return TYPE;
      }

      @Override
      public <T> Stream<T> resolve(ContextMap $$0, DisplayContentsFactory<T> $$1) {
         if ($$1 instanceof DisplayContentsFactory.ForRemainders<T> $$2) {
            List<T> $$3 = this.remainder.resolve($$0, $$1).toList();
            return this.input.resolve($$0, $$1).map($$2x -> $$2.addRemainder((T)$$2x, $$3));
         } else {
            return this.input.resolve($$0, $$1);
         }
      }

      @Override
      public boolean isEnabled(FeatureFlagSet $$0) {
         return this.input.isEnabled($$0) && this.remainder.isEnabled($$0);
      }
   }
}
