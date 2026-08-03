package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;

public class ComponentSerialization {
   public static final Codec<Component> CODEC = Codec.recursive("Component", ComponentSerialization::createCodec);
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Component> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Optional<Component>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(
      ByteBufCodecs::optional
   );
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Component> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(
      CODEC
   );
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Optional<Component>> TRUSTED_OPTIONAL_STREAM_CODEC = TRUSTED_STREAM_CODEC.apply(
      ByteBufCodecs::optional
   );
   public static final StreamCodec<ByteBuf, Component> TRUSTED_CONTEXT_FREE_STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(CODEC);

   public static Codec<Component> flatRestrictedCodec(final int $$0) {
      return new Codec<Component>() {
         public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> $$0x, T $$1) {
            return ComponentSerialization.CODEC
               .decode($$0, $$1)
               .flatMap(
                  $$2 -> this.isTooLarge($$0, (Component)$$2.getFirst())
                     ? DataResult.error(() -> "Component was too large: greater than max size " + $$0)
                     : DataResult.success($$2)
               );
         }

         public <T> DataResult<T> encode(Component $$0x, DynamicOps<T> $$1, T $$2) {
            return ComponentSerialization.CODEC.encodeStart($$1, $$0);
         }

         private <T> boolean isTooLarge(DynamicOps<T> $$0x, Component $$1) {
            DataResult<JsonElement> $$2 = ComponentSerialization.CODEC.encodeStart(asJsonOps($$0), $$1);
            return $$2.isSuccess() && GsonHelper.encodesLongerThan((JsonElement)$$2.getOrThrow(), $$0);
         }

         private static <T> DynamicOps<JsonElement> asJsonOps(DynamicOps<T> $$0x) {
            return (DynamicOps<JsonElement>)($$0 instanceof RegistryOps<T> $$1 ? $$1.withParent(JsonOps.INSTANCE) : JsonOps.INSTANCE);
         }
      };
   }

   private static MutableComponent createFromList(List<Component> $$0) {
      MutableComponent $$1 = $$0.get(0).copy();

      for (int $$2 = 1; $$2 < $$0.size(); $$2++) {
         $$1.append($$0.get($$2));
      }

      return $$1;
   }

   public static <T> MapCodec<T> createLegacyComponentMatcher(
      LateBoundIdMapper<String, MapCodec<? extends T>> $$0, Function<T, MapCodec<? extends T>> $$1, String $$2
   ) {
      MapCodec<T> $$3 = new ComponentSerialization.FuzzyCodec<>($$0.values(), $$1);
      MapCodec<T> $$4 = $$0.codec(Codec.STRING).dispatchMap($$2, $$1, $$0x -> $$0x);
      MapCodec<T> $$5 = new ComponentSerialization.StrictEither<>($$2, $$4, $$3);
      return ExtraCodecs.orCompressed($$5, $$4);
   }

   private static Codec<Component> createCodec(Codec<Component> $$0) {
      LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> $$1 = new LateBoundIdMapper();
      bootstrap($$1);
      MapCodec<ComponentContents> $$2 = createLegacyComponentMatcher($$1, ComponentContents::codec, "type");
      Codec<Component> $$3 = RecordCodecBuilder.create(
         $$2x -> $$2x.group(
               $$2.forGetter(Component::getContents),
               ExtraCodecs.nonEmptyList($$0.listOf()).optionalFieldOf("extra", List.of()).forGetter(Component::getSiblings),
               Style.Serializer.MAP_CODEC.forGetter(Component::getStyle)
            )
            .apply($$2x, MutableComponent::new)
      );
      return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList($$0.listOf())), $$3)
         .xmap(
            $$0x -> (Component)$$0x.map($$0xx -> (Component)$$0xx.map(Component::literal, ComponentSerialization::createFromList), $$0xx -> $$0xx), $$0x -> {
               String $$1x = $$0x.tryCollapseToString();
               return $$1x != null ? Either.left(Either.left($$1x)) : Either.right($$0x);
            }
         );
   }

   private static void bootstrap(LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> $$0) {
      $$0.put("text", PlainTextContents.MAP_CODEC);
      $$0.put("translatable", TranslatableContents.MAP_CODEC);
      $$0.put("keybind", KeybindContents.MAP_CODEC);
      $$0.put("score", ScoreContents.MAP_CODEC);
      $$0.put("selector", SelectorContents.MAP_CODEC);
      $$0.put("nbt", NbtContents.MAP_CODEC);
      $$0.put("object", ObjectContents.MAP_CODEC);
   }

   static class FuzzyCodec<T> extends MapCodec<T> {
      private final Collection<MapCodec<? extends T>> codecs;
      private final Function<T, ? extends MapEncoder<? extends T>> encoderGetter;

      public FuzzyCodec(Collection<MapCodec<? extends T>> $$0, Function<T, ? extends MapEncoder<? extends T>> $$1) {
         this.codecs = $$0;
         this.encoderGetter = $$1;
      }

      public <S> DataResult<T> decode(DynamicOps<S> $$0, MapLike<S> $$1) {
         for (MapDecoder<? extends T> $$2 : this.codecs) {
            DataResult<? extends T> $$3 = $$2.decode($$0, $$1);
            if ($$3.result().isPresent()) {
               return (DataResult<T>)$$3;
            }
         }

         return DataResult.error(() -> "No matching codec found");
      }

      public <S> RecordBuilder<S> encode(T $$0, DynamicOps<S> $$1, RecordBuilder<S> $$2) {
         MapEncoder<T> $$3 = (MapEncoder<T>)this.encoderGetter.apply($$0);
         return $$3.encode($$0, $$1, $$2);
      }

      public <S> Stream<S> keys(DynamicOps<S> $$0) {
         return this.codecs.stream().flatMap($$1 -> $$1.keys($$0)).distinct();
      }

      public String toString() {
         return "FuzzyCodec[" + this.codecs + "]";
      }
   }

   static class StrictEither<T> extends MapCodec<T> {
      private final String typeFieldName;
      private final MapCodec<T> typed;
      private final MapCodec<T> fuzzy;

      public StrictEither(String $$0, MapCodec<T> $$1, MapCodec<T> $$2) {
         this.typeFieldName = $$0;
         this.typed = $$1;
         this.fuzzy = $$2;
      }

      public <O> DataResult<T> decode(DynamicOps<O> $$0, MapLike<O> $$1) {
         return $$1.get(this.typeFieldName) != null ? this.typed.decode($$0, $$1) : this.fuzzy.decode($$0, $$1);
      }

      public <O> RecordBuilder<O> encode(T $$0, DynamicOps<O> $$1, RecordBuilder<O> $$2) {
         return this.fuzzy.encode($$0, $$1, $$2);
      }

      public <T1> Stream<T1> keys(DynamicOps<T1> $$0) {
         return Stream.concat(this.typed.keys($$0), this.fuzzy.keys($$0)).distinct();
      }
   }
}
