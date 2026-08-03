package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
   public static final Codec<net.minecraft.util.InclusiveRange<Integer>> INT = codec(Codec.INT);

   public InclusiveRange(T minInclusive, T maxInclusive) {
      if (minInclusive.compareTo(maxInclusive) > 0) {
         throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
      } else {
         this.minInclusive = minInclusive;
         this.maxInclusive = maxInclusive;
      }
   }

   public InclusiveRange(T $$0) {
      this($$0, $$0);
   }

   public static <T extends Comparable<T>> Codec<net.minecraft.util.InclusiveRange<T>> codec(Codec<T> $$0) {
      return net.minecraft.util.ExtraCodecs.intervalCodec(
         $$0,
         "min_inclusive",
         "max_inclusive",
         net.minecraft.util.InclusiveRange::create,
         net.minecraft.util.InclusiveRange::minInclusive,
         net.minecraft.util.InclusiveRange::maxInclusive
      );
   }

   public static <T extends Comparable<T>> Codec<net.minecraft.util.InclusiveRange<T>> codec(Codec<T> $$0, T $$1, T $$2) {
      return codec($$0)
         .validate(
            $$2x -> {
               if ($$2x.minInclusive().compareTo($$1) < 0) {
                  return DataResult.error(() -> "Range limit too low, expected at least " + $$1 + " [" + $$2x.minInclusive() + "-" + $$2x.maxInclusive() + "]");
               } else {
                  return $$2x.maxInclusive().compareTo($$2) > 0
                     ? DataResult.error(() -> "Range limit too high, expected at most " + $$2 + " [" + $$2x.minInclusive() + "-" + $$2x.maxInclusive() + "]")
                     : DataResult.success($$2x);
               }
            }
         );
   }

   public static <T extends Comparable<T>> DataResult<net.minecraft.util.InclusiveRange<T>> create(T $$0, T $$1) {
      return $$0.compareTo($$1) <= 0
         ? DataResult.success(new net.minecraft.util.InclusiveRange($$0, $$1))
         : DataResult.error(() -> "min_inclusive must be less than or equal to max_inclusive");
   }

   public <S extends Comparable<S>> net.minecraft.util.InclusiveRange<S> map(Function<? super T, ? extends S> $$0) {
      return new net.minecraft.util.InclusiveRange<>((S)$$0.apply(this.minInclusive), (S)$$0.apply(this.maxInclusive));
   }

   public boolean isValueInRange(T $$0) {
      return $$0.compareTo(this.minInclusive) >= 0 && $$0.compareTo(this.maxInclusive) <= 0;
   }

   public boolean contains(net.minecraft.util.InclusiveRange<T> $$0) {
      return $$0.minInclusive().compareTo(this.minInclusive) >= 0 && $$0.maxInclusive.compareTo(this.maxInclusive) <= 0;
   }

   @Override
   public String toString() {
      return "[" + this.minInclusive + ", " + this.maxInclusive + "]";
   }
}
