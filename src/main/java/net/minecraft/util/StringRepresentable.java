package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface StringRepresentable {
   int PRE_BUILT_MAP_THRESHOLD = 16;

   String getSerializedName();

   static <E extends Enum<E> & net.minecraft.util.StringRepresentable> net.minecraft.util.StringRepresentable.EnumCodec<E> fromEnum(Supplier<E[]> $$0) {
      return fromEnumWithMapping($$0, $$0x -> $$0x);
   }

   static <E extends Enum<E> & net.minecraft.util.StringRepresentable> net.minecraft.util.StringRepresentable.EnumCodec<E> fromEnumWithMapping(
      Supplier<E[]> $$0, Function<String, String> $$1
   ) {
      E[] $$2 = (E[])$$0.get();
      Function<String, E> $$3 = createNameLookup($$2, $$1x -> $$1.apply(((net.minecraft.util.StringRepresentable)$$1x).getSerializedName()));
      return new net.minecraft.util.StringRepresentable.EnumCodec<>($$2, $$3);
   }

   static <T extends net.minecraft.util.StringRepresentable> Codec<T> fromValues(Supplier<T[]> $$0) {
      T[] $$1 = (T[])$$0.get();
      Function<String, T> $$2 = createNameLookup($$1);
      ToIntFunction<T> $$3 = net.minecraft.util.Util.createIndexLookup(Arrays.asList($$1));
      return new net.minecraft.util.StringRepresentable.StringRepresentableCodec<>($$1, $$2, $$3);
   }

   static <T extends net.minecraft.util.StringRepresentable> Function<String, T> createNameLookup(T[] $$0) {
      return createNameLookup($$0, net.minecraft.util.StringRepresentable::getSerializedName);
   }

   static <T> Function<String, T> createNameLookup(T[] $$0, Function<T, String> $$1) {
      if ($$0.length > 16) {
         Map<String, T> $$2 = Arrays.<T>stream($$0).collect(Collectors.toMap($$1, $$0x -> (T)$$0x));
         return $$2::get;
      } else {
         return $$2x -> {
            for (T $$3 : $$0) {
               if ($$1.apply($$3).equals($$2x)) {
                  return $$3;
               }
            }

            return null;
         };
      }
   }

   static Keyable keys(final net.minecraft.util.StringRepresentable[] $$0) {
      return new Keyable() {
         public <T> Stream<T> keys(DynamicOps<T> $$0x) {
            return Arrays.stream($$0).map(net.minecraft.util.StringRepresentable::getSerializedName).map($$0::createString);
         }
      };
   }

   public static class EnumCodec<E extends Enum<E> & net.minecraft.util.StringRepresentable>
      extends net.minecraft.util.StringRepresentable.StringRepresentableCodec<E> {
      private final Function<String, E> resolver;

      public EnumCodec(E[] $$0, Function<String, E> $$1) {
         super($$0, $$1, $$0x -> ((Enum)$$0x).ordinal());
         this.resolver = $$1;
      }

      
      public E byName(String $$0) {
         return this.resolver.apply($$0);
      }

      public E byName(String $$0, E $$1) {
         return Objects.requireNonNullElse(this.byName($$0), $$1);
      }

      public E byName(String $$0, Supplier<? extends E> $$1) {
         return Objects.requireNonNullElseGet(this.byName($$0), $$1);
      }
   }

   public static class StringRepresentableCodec<S extends net.minecraft.util.StringRepresentable> implements Codec<S> {
      private final Codec<S> codec;

      public StringRepresentableCodec(S[] $$0, Function<String, S> $$1, ToIntFunction<S> $$2) {
         this.codec = net.minecraft.util.ExtraCodecs.orCompressed(
            Codec.stringResolver(net.minecraft.util.StringRepresentable::getSerializedName, $$1),
            net.minecraft.util.ExtraCodecs.idResolverCodec($$2, $$1x -> $$1x >= 0 && $$1x < $$0.length ? $$0[$$1x] : null, -1)
         );
      }

      public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> $$0, T $$1) {
         return this.codec.decode($$0, $$1);
      }

      public <T> DataResult<T> encode(S $$0, DynamicOps<T> $$1, T $$2) {
         return this.codec.encode($$0, $$1, $$2);
      }
   }
}
