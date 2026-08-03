package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

public class ShufflingList<U> implements Iterable<U> {
   protected final List<ShufflingList.WeightedEntry<U>> entries;
   private final RandomSource random = RandomSource.create();

   public ShufflingList() {
      this.entries = Lists.newArrayList();
   }

   private ShufflingList(List<ShufflingList.WeightedEntry<U>> $$0) {
      this.entries = Lists.newArrayList($$0);
   }

   public static <U> Codec<ShufflingList<U>> codec(Codec<U> $$0) {
      return ShufflingList.WeightedEntry.codec($$0).listOf().xmap(ShufflingList::new, $$0x -> $$0x.entries);
   }

   public ShufflingList<U> add(U $$0, int $$1) {
      this.entries.add(new ShufflingList.WeightedEntry<>($$0, $$1));
      return this;
   }

   public ShufflingList<U> shuffle() {
      this.entries.forEach($$0 -> $$0.setRandom(this.random.nextFloat()));
      this.entries.sort(Comparator.comparingDouble(ShufflingList.WeightedEntry::getRandWeight));
      return this;
   }

   public Stream<U> stream() {
      return this.entries.stream().map(ShufflingList.WeightedEntry::getData);
   }

   @Override
   public Iterator<U> iterator() {
      return Iterators.transform(this.entries.iterator(), ShufflingList.WeightedEntry::getData);
   }

   @Override
   public String toString() {
      return "ShufflingList[" + this.entries + "]";
   }

   public static class WeightedEntry<T> {
      final T data;
      final int weight;
      private double randWeight;

      WeightedEntry(T $$0, int $$1) {
         this.weight = $$1;
         this.data = $$0;
      }

      private double getRandWeight() {
         return this.randWeight;
      }

      void setRandom(float $$0) {
         this.randWeight = -Math.pow($$0, 1.0F / this.weight);
      }

      public T getData() {
         return this.data;
      }

      public int getWeight() {
         return this.weight;
      }

      @Override
      public String toString() {
         return this.weight + ":" + this.data;
      }

      public static <E> Codec<ShufflingList.WeightedEntry<E>> codec(final Codec<E> $$0) {
         return new Codec<ShufflingList.WeightedEntry<E>>() {
            public <T> DataResult<Pair<ShufflingList.WeightedEntry<E>, T>> decode(DynamicOps<T> $$0x, T $$1) {
               Dynamic<T> $$2 = new Dynamic($$0, $$1);
               return $$2.get("data")
                  .flatMap($$0::parse)
                  .map($$1x -> new ShufflingList.WeightedEntry<>($$1x, $$2.get("weight").asInt(1)))
                  .map($$1x -> Pair.of($$1x, $$0.empty()));
            }

            public <T> DataResult<T> encode(ShufflingList.WeightedEntry<E> $$0x, DynamicOps<T> $$1, T $$2) {
               return $$1.mapBuilder().add("weight", $$1.createInt($$0.weight)).add("data", $$0.encodeStart($$1, $$0.data)).build($$2);
            }
         };
      }
   }
}
