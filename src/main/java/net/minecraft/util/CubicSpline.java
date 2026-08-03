package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C, I extends net.minecraft.util.BoundedFloatFunction<C>> extends net.minecraft.util.BoundedFloatFunction<C> {
   @net.minecraft.util.VisibleForDebug
   String parityString();

   net.minecraft.util.CubicSpline<C, I> mapAll(net.minecraft.util.CubicSpline.CoordinateVisitor<I> var1);

   static <C, I extends net.minecraft.util.BoundedFloatFunction<C>> Codec<net.minecraft.util.CubicSpline<C, I>> codec(Codec<I> $$0) {
      MutableObject<Codec<net.minecraft.util.CubicSpline<C, I>>> $$1 = new MutableObject();

      record Point<C, I extends net.minecraft.util.BoundedFloatFunction<C>>(float location, net.minecraft.util.CubicSpline<C, I> value, float derivative) {
      }

      Codec<Point<C, I>> $$2 = RecordCodecBuilder.create(
         $$1x -> $$1x.group(
               Codec.FLOAT.fieldOf("location").forGetter(Point::location),
               Codec.lazyInitialized($$1).fieldOf("value").forGetter(Point::value),
               Codec.FLOAT.fieldOf("derivative").forGetter(Point::derivative)
            )
            .apply($$1x, ($$0xx, $$1xx, $$2x) -> new Point($$0xx, $$1xx, $$2x))
      );
      Codec<net.minecraft.util.CubicSpline.Multipoint<C, I>> $$3 = RecordCodecBuilder.create(
         $$2x -> $$2x.group(
               $$0.fieldOf("coordinate").forGetter(net.minecraft.util.CubicSpline.Multipoint::coordinate),
               net.minecraft.util.ExtraCodecs.nonEmptyList($$2.listOf())
                  .fieldOf("points")
                  .forGetter(
                     $$0xx -> IntStream.range(0, $$0xx.locations.length)
                        .mapToObj(
                           $$1xx -> new Point(
                              $$0xx.locations()[$$1xx], (net.minecraft.util.CubicSpline<C, I>)$$0xx.values().get($$1xx), $$0xx.derivatives()[$$1xx]
                           )
                        )
                        .toList()
                  )
            )
            .apply($$2x, ($$0xx, $$1xx) -> {
               float[] $$2xx = new float[$$1xx.size()];
               com.google.common.collect.ImmutableList.Builder<net.minecraft.util.CubicSpline<C, I>> $$3x = ImmutableList.builder();
               float[] $$4 = new float[$$1xx.size()];

               for (int $$5 = 0; $$5 < $$1xx.size(); $$5++) {
                  Point<C, I> $$6 = (Point<C, I>)$$1xx.get($$5);
                  $$2xx[$$5] = $$6.location();
                  $$3x.add($$6.value());
                  $$4[$$5] = $$6.derivative();
               }

               return net.minecraft.util.CubicSpline.Multipoint.create((I)$$0xx, $$2xx, $$3x.build(), $$4);
            })
      );
      $$1.setValue(
         Codec.either(Codec.FLOAT, $$3)
            .xmap(
               $$0x -> (net.minecraft.util.CubicSpline)$$0x.map(net.minecraft.util.CubicSpline.Constant::new, $$0xx -> $$0xx),
               $$0x -> $$0x instanceof net.minecraft.util.CubicSpline.Constant<C, I> $$1x
                  ? Either.left($$1x.value())
                  : Either.right((net.minecraft.util.CubicSpline.Multipoint)$$0x)
            )
      );
      return (Codec<net.minecraft.util.CubicSpline<C, I>>)$$1.get();
   }

   static <C, I extends net.minecraft.util.BoundedFloatFunction<C>> net.minecraft.util.CubicSpline<C, I> constant(float $$0) {
      return new net.minecraft.util.CubicSpline.Constant<>($$0);
   }

   static <C, I extends net.minecraft.util.BoundedFloatFunction<C>> net.minecraft.util.CubicSpline.Builder<C, I> builder(I $$0) {
      return new net.minecraft.util.CubicSpline.Builder<>($$0);
   }

   static <C, I extends net.minecraft.util.BoundedFloatFunction<C>> net.minecraft.util.CubicSpline.Builder<C, I> builder(
      I $$0, net.minecraft.util.BoundedFloatFunction<Float> $$1
   ) {
      return new net.minecraft.util.CubicSpline.Builder<>($$0, $$1);
   }

   public static final class Builder<C, I extends net.minecraft.util.BoundedFloatFunction<C>> {
      private final I coordinate;
      private final net.minecraft.util.BoundedFloatFunction<Float> valueTransformer;
      private final FloatList locations = new FloatArrayList();
      private final List<net.minecraft.util.CubicSpline<C, I>> values = Lists.newArrayList();
      private final FloatList derivatives = new FloatArrayList();

      protected Builder(I $$0) {
         this($$0, net.minecraft.util.BoundedFloatFunction.IDENTITY);
      }

      protected Builder(I $$0, net.minecraft.util.BoundedFloatFunction<Float> $$1) {
         this.coordinate = $$0;
         this.valueTransformer = $$1;
      }

      public net.minecraft.util.CubicSpline.Builder<C, I> addPoint(float $$0, float $$1) {
         return this.addPoint($$0, new net.minecraft.util.CubicSpline.Constant<>(this.valueTransformer.apply($$1)), 0.0F);
      }

      public net.minecraft.util.CubicSpline.Builder<C, I> addPoint(float $$0, float $$1, float $$2) {
         return this.addPoint($$0, new net.minecraft.util.CubicSpline.Constant<>(this.valueTransformer.apply($$1)), $$2);
      }

      public net.minecraft.util.CubicSpline.Builder<C, I> addPoint(float $$0, net.minecraft.util.CubicSpline<C, I> $$1) {
         return this.addPoint($$0, $$1, 0.0F);
      }

      private net.minecraft.util.CubicSpline.Builder<C, I> addPoint(float $$0, net.minecraft.util.CubicSpline<C, I> $$1, float $$2) {
         if (!this.locations.isEmpty() && $$0 <= this.locations.getFloat(this.locations.size() - 1)) {
            throw new IllegalArgumentException("Please register points in ascending order");
         } else {
            this.locations.add($$0);
            this.values.add($$1);
            this.derivatives.add($$2);
            return this;
         }
      }

      public net.minecraft.util.CubicSpline<C, I> build() {
         if (this.locations.isEmpty()) {
            throw new IllegalStateException("No elements added");
         } else {
            return net.minecraft.util.CubicSpline.Multipoint.create(
               this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray()
            );
         }
      }
   }

   @net.minecraft.util.VisibleForDebug
   public record Constant<C, I extends net.minecraft.util.BoundedFloatFunction<C>>(float value) implements net.minecraft.util.CubicSpline<C, I> {
      @Override
      public float apply(C $$0) {
         return this.value;
      }

      @Override
      public String parityString() {
         return String.format(Locale.ROOT, "k=%.3f", this.value);
      }

      @Override
      public float minValue() {
         return this.value;
      }

      @Override
      public float maxValue() {
         return this.value;
      }

      @Override
      public net.minecraft.util.CubicSpline<C, I> mapAll(net.minecraft.util.CubicSpline.CoordinateVisitor<I> $$0) {
         return this;
      }
   }

   public interface CoordinateVisitor<I> {
      I visit(I var1);
   }

   @net.minecraft.util.VisibleForDebug
   public record Multipoint<C, I extends net.minecraft.util.BoundedFloatFunction<C>>(
      I coordinate, float[] locations, List<net.minecraft.util.CubicSpline<C, I>> values, float[] derivatives, float minValue, float maxValue
   ) implements net.minecraft.util.CubicSpline<C, I> {

      public Multipoint(I coordinate, float[] locations, List<net.minecraft.util.CubicSpline<C, I>> values, float[] derivatives, float minValue, float maxValue) {
         validateSizes(locations, values, derivatives);
         this.coordinate = coordinate;
         this.locations = locations;
         this.values = values;
         this.derivatives = derivatives;
         this.minValue = minValue;
         this.maxValue = maxValue;
      }

      static <C, I extends net.minecraft.util.BoundedFloatFunction<C>> net.minecraft.util.CubicSpline.Multipoint<C, I> create(
         I $$0, float[] $$1, List<net.minecraft.util.CubicSpline<C, I>> $$2, float[] $$3
      ) {
         validateSizes($$1, $$2, $$3);
         int $$4 = $$1.length - 1;
         float $$5 = Float.POSITIVE_INFINITY;
         float $$6 = Float.NEGATIVE_INFINITY;
         float $$7 = $$0.minValue();
         float $$8 = $$0.maxValue();
         if ($$7 < $$1[0]) {
            float $$9 = linearExtend($$7, $$1, $$2.get(0).minValue(), $$3, 0);
            float $$10 = linearExtend($$7, $$1, $$2.get(0).maxValue(), $$3, 0);
            $$5 = Math.min($$5, Math.min($$9, $$10));
            $$6 = Math.max($$6, Math.max($$9, $$10));
         }

         if ($$8 > $$1[$$4]) {
            float $$11 = linearExtend($$8, $$1, $$2.get($$4).minValue(), $$3, $$4);
            float $$12 = linearExtend($$8, $$1, $$2.get($$4).maxValue(), $$3, $$4);
            $$5 = Math.min($$5, Math.min($$11, $$12));
            $$6 = Math.max($$6, Math.max($$11, $$12));
         }

         for (net.minecraft.util.CubicSpline<C, I> $$13 : $$2) {
            $$5 = Math.min($$5, $$13.minValue());
            $$6 = Math.max($$6, $$13.maxValue());
         }

         for (int $$14 = 0; $$14 < $$4; $$14++) {
            float $$15 = $$1[$$14];
            float $$16 = $$1[$$14 + 1];
            float $$17 = $$16 - $$15;
            net.minecraft.util.CubicSpline<C, I> $$18 = $$2.get($$14);
            net.minecraft.util.CubicSpline<C, I> $$19 = $$2.get($$14 + 1);
            float $$20 = $$18.minValue();
            float $$21 = $$18.maxValue();
            float $$22 = $$19.minValue();
            float $$23 = $$19.maxValue();
            float $$24 = $$3[$$14];
            float $$25 = $$3[$$14 + 1];
            if ($$24 != 0.0F || $$25 != 0.0F) {
               float $$26 = $$24 * $$17;
               float $$27 = $$25 * $$17;
               float $$28 = Math.min($$20, $$22);
               float $$29 = Math.max($$21, $$23);
               float $$30 = $$26 - $$23 + $$20;
               float $$31 = $$26 - $$22 + $$21;
               float $$32 = -$$27 + $$22 - $$21;
               float $$33 = -$$27 + $$23 - $$20;
               float $$34 = Math.min($$30, $$32);
               float $$35 = Math.max($$31, $$33);
               $$5 = Math.min($$5, $$28 + 0.25F * $$34);
               $$6 = Math.max($$6, $$29 + 0.25F * $$35);
            }
         }

         return new net.minecraft.util.CubicSpline.Multipoint<>($$0, $$1, $$2, $$3, $$5, $$6);
      }

      private static float linearExtend(float $$0, float[] $$1, float $$2, float[] $$3, int $$4) {
         float $$5 = $$3[$$4];
         return $$5 == 0.0F ? $$2 : $$2 + $$5 * ($$0 - $$1[$$4]);
      }

      private static <C, I extends net.minecraft.util.BoundedFloatFunction<C>> void validateSizes(
         float[] $$0, List<net.minecraft.util.CubicSpline<C, I>> $$1, float[] $$2
      ) {
         if ($$0.length != $$1.size() || $$0.length != $$2.length) {
            throw new IllegalArgumentException("All lengths must be equal, got: " + $$0.length + " " + $$1.size() + " " + $$2.length);
         } else if ($$0.length == 0) {
            throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
         }
      }

      @Override
      public float apply(C $$0) {
         float $$1 = this.coordinate.apply($$0);
         int $$2 = findIntervalStart(this.locations, $$1);
         int $$3 = this.locations.length - 1;
         if ($$2 < 0) {
            return linearExtend($$1, this.locations, this.values.get(0).apply($$0), this.derivatives, 0);
         } else if ($$2 == $$3) {
            return linearExtend($$1, this.locations, this.values.get($$3).apply($$0), this.derivatives, $$3);
         } else {
            float $$4 = this.locations[$$2];
            float $$5 = this.locations[$$2 + 1];
            float $$6 = ($$1 - $$4) / ($$5 - $$4);
            net.minecraft.util.BoundedFloatFunction<C> $$7 = (net.minecraft.util.BoundedFloatFunction<C>)this.values.get($$2);
            net.minecraft.util.BoundedFloatFunction<C> $$8 = (net.minecraft.util.BoundedFloatFunction<C>)this.values.get($$2 + 1);
            float $$9 = this.derivatives[$$2];
            float $$10 = this.derivatives[$$2 + 1];
            float $$11 = $$7.apply($$0);
            float $$12 = $$8.apply($$0);
            float $$13 = $$9 * ($$5 - $$4) - ($$12 - $$11);
            float $$14 = -$$10 * ($$5 - $$4) + ($$12 - $$11);
            return net.minecraft.util.Mth.lerp($$6, $$11, $$12) + $$6 * (1.0F - $$6) * net.minecraft.util.Mth.lerp($$6, $$13, $$14);
         }
      }

      private static int findIntervalStart(float[] $$0, float $$1) {
         return net.minecraft.util.Mth.binarySearch(0, $$0.length, $$2 -> $$1 < $$0[$$2]) - 1;
      }

      @VisibleForTesting
      @Override
      public String parityString() {
         return "Spline{coordinate="
            + this.coordinate
            + ", locations="
            + this.toString(this.locations)
            + ", derivatives="
            + this.toString(this.derivatives)
            + ", values="
            + this.values.stream().map(net.minecraft.util.CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]"))
            + "}";
      }

      private String toString(float[] $$0) {
         return "["
            + IntStream.range(0, $$0.length)
               .mapToDouble($$1 -> $$0[$$1])
               .mapToObj($$0x -> String.format(Locale.ROOT, "%.3f", $$0x))
               .collect(Collectors.joining(", "))
            + "]";
      }

      @Override
      public net.minecraft.util.CubicSpline<C, I> mapAll(net.minecraft.util.CubicSpline.CoordinateVisitor<I> $$0) {
         return create($$0.visit(this.coordinate), this.locations, this.values().stream().map($$1 -> $$1.mapAll($$0)).toList(), this.derivatives);
      }
   }
}
