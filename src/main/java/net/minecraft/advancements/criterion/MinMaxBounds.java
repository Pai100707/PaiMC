package net.minecraft.advancements.criterion;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public interface MinMaxBounds<T extends Number & Comparable<T>> {
   SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
   SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));

   MinMaxBounds.Bounds<T> bounds();

   default Optional<T> min() {
      return this.bounds().min;
   }

   default Optional<T> max() {
      return this.bounds().max;
   }

   default boolean isAny() {
      return this.bounds().isAny();
   }

   public record Bounds<T extends Number & Comparable<T>>(Optional<T> min, Optional<T> max) {

      public boolean isAny() {
         return this.min().isEmpty() && this.max().isEmpty();
      }

      public DataResult<MinMaxBounds.Bounds<T>> validateSwappedBoundsInCodec() {
         return this.areSwapped()
            ? DataResult.error(() -> "Swapped bounds in range: " + this.min() + " is higher than " + this.max())
            : DataResult.success(this);
      }

      public boolean areSwapped() {
         return this.min.isPresent() && this.max.isPresent() && this.min.get().compareTo(this.max.get()) > 0;
      }

      public Optional<T> asPoint() {
         Optional<T> $$0 = this.min();
         Optional<T> $$1 = this.max();
         return $$0.equals($$1) ? $$0 : Optional.empty();
      }

      public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> any() {
         return new MinMaxBounds.Bounds<>(Optional.empty(), Optional.empty());
      }

      public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> exactly(T $$0) {
         Optional<T> $$1 = Optional.of($$0);
         return new MinMaxBounds.Bounds<>($$1, $$1);
      }

      public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> between(T $$0, T $$1) {
         return new MinMaxBounds.Bounds<>(Optional.of($$0), Optional.of($$1));
      }

      public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> atLeast(T $$0) {
         return new MinMaxBounds.Bounds<>(Optional.of($$0), Optional.empty());
      }

      public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> atMost(T $$0) {
         return new MinMaxBounds.Bounds<>(Optional.empty(), Optional.of($$0));
      }

      public <U extends Number & Comparable<U>> MinMaxBounds.Bounds<U> map(Function<T, U> $$0) {
         return new MinMaxBounds.Bounds<>(this.min.map($$0), this.max.map($$0));
      }

      static <T extends Number & Comparable<T>> Codec<MinMaxBounds.Bounds<T>> createCodec(Codec<T> $$0) {
         Codec<MinMaxBounds.Bounds<T>> $$1 = RecordCodecBuilder.create(
            $$1x -> $$1x.group($$0.optionalFieldOf("min").forGetter(MinMaxBounds.Bounds::min), $$0.optionalFieldOf("max").forGetter(MinMaxBounds.Bounds::max))
               .apply($$1x, MinMaxBounds.Bounds::new)
         );
         return Codec.either($$1, $$0).xmap($$0x -> (MinMaxBounds.Bounds)$$0x.map($$0xx -> $$0xx, $$0xx -> exactly((T)$$0xx)), $$0x -> {
            Optional<T> $$1x = $$0x.asPoint();
            return $$1x.isPresent() ? Either.right($$1x.get()) : Either.left($$0x);
         });
      }

      static <B extends ByteBuf, T extends Number & Comparable<T>> StreamCodec<B, MinMaxBounds.Bounds<T>> createStreamCodec(final StreamCodec<B, T> $$0) {
         return new StreamCodec<B, MinMaxBounds.Bounds<T>>() {
            private static final int MIN_FLAG = 1;
            private static final int MAX_FLAG = 2;

            public MinMaxBounds.Bounds<T> decode(B $$0x) {
               byte $$1 = $$0.readByte();
               Optional<T> $$2 = ($$1 & 1) != 0 ? Optional.of((T)$$0.decode($$0)) : Optional.empty();
               Optional<T> $$3 = ($$1 & 2) != 0 ? Optional.of((T)$$0.decode($$0)) : Optional.empty();
               return new MinMaxBounds.Bounds<>($$2, $$3);
            }

            public void encode(B $$0x, MinMaxBounds.Bounds<T> $$1) {
               Optional<T> $$2 = $$1.min();
               Optional<T> $$3 = $$1.max();
               $$0.writeByte(($$2.isPresent() ? 1 : 0) | ($$3.isPresent() ? 2 : 0));
               $$2.ifPresent($$2x -> $$0.encode($$0, $$2x));
               $$3.ifPresent($$2x -> $$0.encode($$0, $$2x));
            }
         };
      }

      public static <T extends Number & Comparable<T>> MinMaxBounds.Bounds<T> fromReader(
         StringReader $$0, Function<String, T> $$1, Supplier<DynamicCommandExceptionType> $$2
      ) throws CommandSyntaxException {
         if (!$$0.canRead()) {
            throw MinMaxBounds.ERROR_EMPTY.createWithContext($$0);
         } else {
            int $$3 = $$0.getCursor();

            try {
               Optional<T> $$4 = readNumber($$0, $$1, $$2);
               Optional<T> $$5;
               if ($$0.canRead(2) && $$0.peek() == '.' && $$0.peek(1) == '.') {
                  $$0.skip();
                  $$0.skip();
                  $$5 = readNumber($$0, $$1, $$2);
               } else {
                  $$5 = $$4;
               }

               if ($$4.isEmpty() && $$5.isEmpty()) {
                  throw MinMaxBounds.ERROR_EMPTY.createWithContext($$0);
               } else {
                  return new MinMaxBounds.Bounds<>($$4, $$5);
               }
            } catch (CommandSyntaxException var6) {
               $$0.setCursor($$3);
               throw new CommandSyntaxException(var6.getType(), var6.getRawMessage(), var6.getInput(), $$3);
            }
         }
      }

      private static <T extends Number> Optional<T> readNumber(StringReader $$0, Function<String, T> $$1, Supplier<DynamicCommandExceptionType> $$2) throws CommandSyntaxException {
         int $$3 = $$0.getCursor();

         while ($$0.canRead() && isAllowedInputChar($$0)) {
            $$0.skip();
         }

         String $$4 = $$0.getString().substring($$3, $$0.getCursor());
         if ($$4.isEmpty()) {
            return Optional.empty();
         } else {
            try {
               return Optional.of($$1.apply($$4));
            } catch (NumberFormatException var6) {
               throw $$2.get().createWithContext($$0, $$4);
            }
         }
      }

      private static boolean isAllowedInputChar(StringReader $$0) {
         char $$1 = $$0.peek();
         if (($$1 < '0' || $$1 > '9') && $$1 != '-') {
            return $$1 != '.' ? false : !$$0.canRead(2) || $$0.peek(1) != '.';
         } else {
            return true;
         }
      }
   }

   public record Doubles(MinMaxBounds.Bounds<Double> bounds, MinMaxBounds.Bounds<Double> boundsSqr) implements MinMaxBounds<Double> {
      public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles(MinMaxBounds.Bounds.any());
      public static final Codec<MinMaxBounds.Doubles> CODEC = MinMaxBounds.Bounds.createCodec(Codec.DOUBLE)
         .validate(MinMaxBounds.Bounds::validateSwappedBoundsInCodec)
         .xmap(MinMaxBounds.Doubles::new, MinMaxBounds.Doubles::bounds);
      public static final StreamCodec<ByteBuf, MinMaxBounds.Doubles> STREAM_CODEC = MinMaxBounds.Bounds.createStreamCodec(ByteBufCodecs.DOUBLE)
         .map(MinMaxBounds.Doubles::new, MinMaxBounds.Doubles::bounds);

      private Doubles(MinMaxBounds.Bounds<Double> $$0) {
         this($$0, $$0.map(Mth::square));
      }

      public static MinMaxBounds.Doubles exactly(double $$0) {
         return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.exactly($$0));
      }

      public static MinMaxBounds.Doubles between(double $$0, double $$1) {
         return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.between($$0, $$1));
      }

      public static MinMaxBounds.Doubles atLeast(double $$0) {
         return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.atLeast($$0));
      }

      public static MinMaxBounds.Doubles atMost(double $$0) {
         return new MinMaxBounds.Doubles(MinMaxBounds.Bounds.atMost($$0));
      }

      public boolean matches(double $$0) {
         return this.bounds.min.isPresent() && this.bounds.min.get() > $$0 ? false : this.bounds.max.isEmpty() || !(this.bounds.max.get() < $$0);
      }

      public boolean matchesSqr(double $$0) {
         return this.boundsSqr.min.isPresent() && this.boundsSqr.min.get() > $$0 ? false : this.boundsSqr.max.isEmpty() || !(this.boundsSqr.max.get() < $$0);
      }

      public static MinMaxBounds.Doubles fromReader(StringReader $$0) throws CommandSyntaxException {
         int $$1 = $$0.getCursor();
         MinMaxBounds.Bounds<Double> $$2 = MinMaxBounds.Bounds.fromReader(
            $$0, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble
         );
         if ($$2.areSwapped()) {
            $$0.setCursor($$1);
            throw ERROR_SWAPPED.createWithContext($$0);
         } else {
            return new MinMaxBounds.Doubles($$2);
         }
      }
   }

   public record FloatDegrees(MinMaxBounds.Bounds<Float> bounds) implements MinMaxBounds<Float> {
      public static final MinMaxBounds.FloatDegrees ANY = new MinMaxBounds.FloatDegrees(MinMaxBounds.Bounds.any());
      public static final Codec<MinMaxBounds.FloatDegrees> CODEC = MinMaxBounds.Bounds.createCodec(Codec.FLOAT)
         .xmap(MinMaxBounds.FloatDegrees::new, MinMaxBounds.FloatDegrees::bounds);
      public static final StreamCodec<ByteBuf, MinMaxBounds.FloatDegrees> STREAM_CODEC = MinMaxBounds.Bounds.createStreamCodec(ByteBufCodecs.FLOAT)
         .map(MinMaxBounds.FloatDegrees::new, MinMaxBounds.FloatDegrees::bounds);

      public static MinMaxBounds.FloatDegrees fromReader(StringReader $$0) throws CommandSyntaxException {
         MinMaxBounds.Bounds<Float> $$1 = MinMaxBounds.Bounds.fromReader($$0, Float::parseFloat, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidFloat);
         return new MinMaxBounds.FloatDegrees($$1);
      }
   }

   public record Ints(MinMaxBounds.Bounds<Integer> bounds, MinMaxBounds.Bounds<Long> boundsSqr) implements MinMaxBounds<Integer> {
      public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints(MinMaxBounds.Bounds.any());
      public static final Codec<MinMaxBounds.Ints> CODEC = MinMaxBounds.Bounds.createCodec(Codec.INT)
         .validate(MinMaxBounds.Bounds::validateSwappedBoundsInCodec)
         .xmap(MinMaxBounds.Ints::new, MinMaxBounds.Ints::bounds);
      public static final StreamCodec<ByteBuf, MinMaxBounds.Ints> STREAM_CODEC = MinMaxBounds.Bounds.createStreamCodec(ByteBufCodecs.INT)
         .map(MinMaxBounds.Ints::new, MinMaxBounds.Ints::bounds);

      private Ints(MinMaxBounds.Bounds<Integer> $$0) {
         this($$0, $$0.map($$0x -> Mth.square($$0x.longValue())));
      }

      public static MinMaxBounds.Ints exactly(int $$0) {
         return new MinMaxBounds.Ints(MinMaxBounds.Bounds.exactly($$0));
      }

      public static MinMaxBounds.Ints between(int $$0, int $$1) {
         return new MinMaxBounds.Ints(MinMaxBounds.Bounds.between($$0, $$1));
      }

      public static MinMaxBounds.Ints atLeast(int $$0) {
         return new MinMaxBounds.Ints(MinMaxBounds.Bounds.atLeast($$0));
      }

      public static MinMaxBounds.Ints atMost(int $$0) {
         return new MinMaxBounds.Ints(MinMaxBounds.Bounds.atMost($$0));
      }

      public boolean matches(int $$0) {
         return this.bounds.min.isPresent() && this.bounds.min.get() > $$0 ? false : this.bounds.max.isEmpty() || this.bounds.max.get() >= $$0;
      }

      public boolean matchesSqr(long $$0) {
         return this.boundsSqr.min.isPresent() && this.boundsSqr.min.get() > $$0 ? false : this.boundsSqr.max.isEmpty() || this.boundsSqr.max.get() >= $$0;
      }

      public static MinMaxBounds.Ints fromReader(StringReader $$0) throws CommandSyntaxException {
         int $$1 = $$0.getCursor();
         MinMaxBounds.Bounds<Integer> $$2 = MinMaxBounds.Bounds.fromReader($$0, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt);
         if ($$2.areSwapped()) {
            $$0.setCursor($$1);
            throw ERROR_SWAPPED.createWithContext($$0);
         } else {
            return new MinMaxBounds.Ints($$2);
         }
      }
   }
}
