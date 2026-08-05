package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class WeightedList<E> {
   private static final int FLAT_THRESHOLD = 64;
   private final int totalWeight;
   private final List<Weighted<E>> items;
   
   private final WeightedList.Selector<E> selector;

   WeightedList(List<? extends Weighted<E>> $$0) {
      this.items = List.copyOf($$0);
      this.totalWeight = WeightedRandom.getTotalWeight($$0, Weighted::weight);
      if (this.totalWeight == 0) {
         this.selector = null;
      } else if (this.totalWeight < 64) {
         this.selector = new WeightedList.Flat<>(this.items, this.totalWeight);
      } else {
         this.selector = new WeightedList.Compact<>(this.items);
      }
   }

   public static <E> WeightedList<E> of() {
      return new WeightedList<>(List.of());
   }

   public static <E> WeightedList<E> of(E $$0) {
      return new WeightedList<>(List.of(new Weighted<>($$0, 1)));
   }

   @SafeVarargs
   public static <E> WeightedList<E> of(Weighted<E>... $$0) {
      return new WeightedList<>(List.of($$0));
   }

   public static <E> WeightedList<E> of(List<Weighted<E>> $$0) {
      return new WeightedList<>($$0);
   }

   public static <E> WeightedList.Builder<E> builder() {
      return new WeightedList.Builder<>();
   }

   public boolean isEmpty() {
      return this.items.isEmpty();
   }

   public <T> WeightedList<T> map(Function<E, T> $$0) {
      return new WeightedList(Lists.transform(this.items, $$1 -> $$1.map($$0)));
   }

   public Optional<E> getRandom(net.minecraft.util.RandomSource $$0) {
      if (this.selector == null) {
         return Optional.empty();
      } else {
         int $$1 = $$0.nextInt(this.totalWeight);
         return Optional.of(this.selector.get($$1));
      }
   }

   public E getRandomOrThrow(net.minecraft.util.RandomSource $$0) {
      if (this.selector == null) {
         throw new IllegalStateException("Weighted list has no elements");
      } else {
         int $$1 = $$0.nextInt(this.totalWeight);
         return this.selector.get($$1);
      }
   }

   public List<Weighted<E>> unwrap() {
      return this.items;
   }

   public static <E> Codec<WeightedList<E>> codec(Codec<E> $$0) {
      return Weighted.codec($$0).listOf().xmap(WeightedList::of, WeightedList::unwrap);
   }

   public static <E> Codec<WeightedList<E>> codec(MapCodec<E> $$0) {
      return Weighted.codec($$0).listOf().xmap(WeightedList::of, WeightedList::unwrap);
   }

   public static <E> Codec<WeightedList<E>> nonEmptyCodec(Codec<E> $$0) {
      return net.minecraft.util.ExtraCodecs.nonEmptyList(Weighted.codec($$0).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
   }

   public static <E> Codec<WeightedList<E>> nonEmptyCodec(MapCodec<E> $$0) {
      return net.minecraft.util.ExtraCodecs.nonEmptyList(Weighted.codec($$0).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
   }

   public static <E, B extends ByteBuf> StreamCodec<B, WeightedList<E>> streamCodec(StreamCodec<B, E> $$0) {
      return Weighted.streamCodec($$0).apply(ByteBufCodecs.list()).map(WeightedList::of, WeightedList::unwrap);
   }

   public boolean contains(E $$0) {
      for (Weighted<E> $$1 : this.items) {
         if ($$1.value().equals($$0)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof WeightedList<?> $$1) ? false : this.totalWeight == $$1.totalWeight && Objects.equals(this.items, $$1.items);
      }
   }

   @Override
   public int hashCode() {
      int $$0 = this.totalWeight;
      return 31 * $$0 + this.items.hashCode();
   }

   public static class Builder<E> {
      private final com.google.common.collect.ImmutableList.Builder<Weighted<E>> result = ImmutableList.builder();

      public WeightedList.Builder<E> add(E $$0) {
         return this.add($$0, 1);
      }

      public WeightedList.Builder<E> add(E $$0, int $$1) {
         this.result.add(new Weighted<>($$0, $$1));
         return this;
      }

      public WeightedList<E> build() {
         return new WeightedList<>(this.result.build());
      }
   }

   static class Compact<E> implements WeightedList.Selector<E> {
      private final Weighted<?>[] entries;

      Compact(List<Weighted<E>> $$0) {
         this.entries = $$0.toArray(Weighted[]::new);
      }

      @Override
      public E get(int $$0) {
         for (Weighted<?> $$1 : this.entries) {
            $$0 -= $$1.weight();
            if ($$0 < 0) {
               return (E)$$1.value();
            }
         }

         throw new IllegalStateException($$0 + " exceeded total weight");
      }
   }

   static class Flat<E> implements WeightedList.Selector<E> {
      private final Object[] entries;

      Flat(List<Weighted<E>> $$0, int $$1) {
         this.entries = new Object[$$1];
         int $$2 = 0;

         for (Weighted<E> $$3 : $$0) {
            int $$4 = $$3.weight();
            Arrays.fill(this.entries, $$2, $$2 + $$4, $$3.value());
            $$2 += $$4;
         }
      }

      @Override
      public E get(int $$0) {
         return (E)this.entries[$$0];
      }
   }

   interface Selector<E> {
      E get(int var1);
   }
}
