package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Direct;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
   private static final Codec<DensityFunction> CODEC = BuiltInRegistries.DENSITY_FUNCTION_TYPE
      .byNameCodec()
      .dispatch($$0 -> $$0.codec().codec(), Function.identity());
   protected static final double MAX_REASONABLE_NOISE_VALUE = 1000000.0;
   static final Codec<Double> NOISE_VALUE_CODEC = Codec.doubleRange(-1000000.0, 1000000.0);
   public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(NOISE_VALUE_CODEC, CODEC)
      .xmap(
         $$0 -> (DensityFunction)$$0.map(DensityFunctions::constant, Function.identity()),
         $$0 -> $$0 instanceof DensityFunctions.Constant $$1 ? Either.left($$1.value()) : Either.right($$0)
      );

   public static MapCodec<? extends DensityFunction> bootstrap(Registry<MapCodec<? extends DensityFunction>> $$0) {
      register($$0, "blend_alpha", DensityFunctions.BlendAlpha.CODEC);
      register($$0, "blend_offset", DensityFunctions.BlendOffset.CODEC);
      register($$0, "beardifier", DensityFunctions.BeardifierMarker.CODEC);
      register($$0, "old_blended_noise", BlendedNoise.CODEC);

      for (DensityFunctions.Marker.Type $$1 : DensityFunctions.Marker.Type.values()) {
         register($$0, $$1.getSerializedName(), $$1.codec);
      }

      register($$0, "noise", DensityFunctions.Noise.CODEC);
      register($$0, "end_islands", DensityFunctions.EndIslandDensityFunction.CODEC);
      register($$0, "weird_scaled_sampler", DensityFunctions.WeirdScaledSampler.CODEC);
      register($$0, "shifted_noise", DensityFunctions.ShiftedNoise.CODEC);
      register($$0, "range_choice", DensityFunctions.RangeChoice.CODEC);
      register($$0, "shift_a", DensityFunctions.ShiftA.CODEC);
      register($$0, "shift_b", DensityFunctions.ShiftB.CODEC);
      register($$0, "shift", DensityFunctions.Shift.CODEC);
      register($$0, "blend_density", DensityFunctions.BlendDensity.CODEC);
      register($$0, "clamp", DensityFunctions.Clamp.CODEC);

      for (DensityFunctions.Mapped.Type $$2 : DensityFunctions.Mapped.Type.values()) {
         register($$0, $$2.getSerializedName(), $$2.codec);
      }

      for (DensityFunctions.TwoArgumentSimpleFunction.Type $$3 : DensityFunctions.TwoArgumentSimpleFunction.Type.values()) {
         register($$0, $$3.getSerializedName(), $$3.codec);
      }

      register($$0, "spline", DensityFunctions.Spline.CODEC);
      register($$0, "constant", DensityFunctions.Constant.CODEC);
      register($$0, "y_clamped_gradient", DensityFunctions.YClampedGradient.CODEC);
      return register($$0, "find_top_surface", DensityFunctions.FindTopSurface.CODEC);
   }

   private static MapCodec<? extends DensityFunction> register(
      Registry<MapCodec<? extends DensityFunction>> $$0, String $$1, KeyDispatchDataCodec<? extends DensityFunction> $$2
   ) {
      return (MapCodec<? extends DensityFunction>)Registry.register($$0, $$1, $$2.codec());
   }

   static <A, O> KeyDispatchDataCodec<O> singleArgumentCodec(Codec<A> $$0, Function<A, O> $$1, Function<O, A> $$2) {
      return KeyDispatchDataCodec.of($$0.fieldOf("argument").xmap($$1, $$2));
   }

   static <O> KeyDispatchDataCodec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> $$0, Function<O, DensityFunction> $$1) {
      return singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, $$0, $$1);
   }

   static <O> KeyDispatchDataCodec<O> doubleFunctionArgumentCodec(
      BiFunction<DensityFunction, DensityFunction, O> $$0, Function<O, DensityFunction> $$1, Function<O, DensityFunction> $$2
   ) {
      return KeyDispatchDataCodec.of(
         RecordCodecBuilder.mapCodec(
            $$3 -> $$3.group(
                  DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter($$1),
                  DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter($$2)
               )
               .apply($$3, $$0)
         )
      );
   }

   static <O> KeyDispatchDataCodec<O> makeCodec(MapCodec<O> $$0) {
      return KeyDispatchDataCodec.of($$0);
   }

   private DensityFunctions() {
   }

   public static DensityFunction interpolated(DensityFunction $$0) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Interpolated, $$0);
   }

   public static DensityFunction flatCache(DensityFunction $$0) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.FlatCache, $$0);
   }

   public static DensityFunction cache2d(DensityFunction $$0) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Cache2D, $$0);
   }

   public static DensityFunction cacheOnce(DensityFunction $$0) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheOnce, $$0);
   }

   public static DensityFunction cacheAllInCell(DensityFunction $$0) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheAllInCell, $$0);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> $$0, @Deprecated double $$1, double $$2, double $$3, double $$4) {
      return mapFromUnitTo(new DensityFunctions.Noise(new DensityFunction.NoiseHolder($$0), $$1, $$2), $$3, $$4);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> $$0, double $$1, double $$2, double $$3) {
      return mappedNoise($$0, 1.0, $$1, $$2, $$3);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> $$0, double $$1, double $$2) {
      return mappedNoise($$0, 1.0, 1.0, $$1, $$2);
   }

   public static DensityFunction shiftedNoise2d(DensityFunction $$0, DensityFunction $$1, double $$2, Holder<NormalNoise.NoiseParameters> $$3) {
      return new DensityFunctions.ShiftedNoise($$0, zero(), $$1, $$2, 0.0, new DensityFunction.NoiseHolder($$3));
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> $$0) {
      return noise($$0, 1.0, 1.0);
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> $$0, double $$1, double $$2) {
      return new DensityFunctions.Noise(new DensityFunction.NoiseHolder($$0), $$1, $$2);
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> $$0, double $$1) {
      return noise($$0, 1.0, $$1);
   }

   public static DensityFunction rangeChoice(DensityFunction $$0, double $$1, double $$2, DensityFunction $$3, DensityFunction $$4) {
      return new DensityFunctions.RangeChoice($$0, $$1, $$2, $$3, $$4);
   }

   public static DensityFunction shiftA(Holder<NormalNoise.NoiseParameters> $$0) {
      return new DensityFunctions.ShiftA(new DensityFunction.NoiseHolder($$0));
   }

   public static DensityFunction shiftB(Holder<NormalNoise.NoiseParameters> $$0) {
      return new DensityFunctions.ShiftB(new DensityFunction.NoiseHolder($$0));
   }

   public static DensityFunction shift(Holder<NormalNoise.NoiseParameters> $$0) {
      return new DensityFunctions.Shift(new DensityFunction.NoiseHolder($$0));
   }

   public static DensityFunction blendDensity(DensityFunction $$0) {
      return new DensityFunctions.BlendDensity($$0);
   }

   public static DensityFunction endIslands(long $$0) {
      return new DensityFunctions.EndIslandDensityFunction($$0);
   }

   public static DensityFunction weirdScaledSampler(
      DensityFunction $$0, Holder<NormalNoise.NoiseParameters> $$1, DensityFunctions.WeirdScaledSampler.RarityValueMapper $$2
   ) {
      return new DensityFunctions.WeirdScaledSampler($$0, new DensityFunction.NoiseHolder($$1), $$2);
   }

   public static DensityFunction add(DensityFunction $$0, DensityFunction $$1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, $$0, $$1);
   }

   public static DensityFunction mul(DensityFunction $$0, DensityFunction $$1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL, $$0, $$1);
   }

   public static DensityFunction min(DensityFunction $$0, DensityFunction $$1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MIN, $$0, $$1);
   }

   public static DensityFunction max(DensityFunction $$0, DensityFunction $$1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MAX, $$0, $$1);
   }

   public static DensityFunction spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> $$0) {
      return new DensityFunctions.Spline($$0);
   }

   public static DensityFunction zero() {
      return DensityFunctions.Constant.ZERO;
   }

   public static DensityFunction constant(double $$0) {
      return new DensityFunctions.Constant($$0);
   }

   public static DensityFunction yClampedGradient(int $$0, int $$1, double $$2, double $$3) {
      return new DensityFunctions.YClampedGradient($$0, $$1, $$2, $$3);
   }

   public static DensityFunction map(DensityFunction $$0, DensityFunctions.Mapped.Type $$1) {
      return DensityFunctions.Mapped.create($$1, $$0);
   }

   private static DensityFunction mapFromUnitTo(DensityFunction $$0, double $$1, double $$2) {
      double $$3 = ($$1 + $$2) * 0.5;
      double $$4 = ($$2 - $$1) * 0.5;
      return add(constant($$3), mul(constant($$4), $$0));
   }

   public static DensityFunction blendAlpha() {
      return DensityFunctions.BlendAlpha.INSTANCE;
   }

   public static DensityFunction blendOffset() {
      return DensityFunctions.BlendOffset.INSTANCE;
   }

   public static DensityFunction lerp(DensityFunction $$0, DensityFunction $$1, DensityFunction $$2) {
      if ($$1 instanceof DensityFunctions.Constant $$3) {
         return lerp($$0, $$3.value, $$2);
      } else {
         DensityFunction $$4 = cacheOnce($$0);
         DensityFunction $$5 = add(mul($$4, constant(-1.0)), constant(1.0));
         return add(mul($$1, $$5), mul($$2, $$4));
      }
   }

   public static DensityFunction lerp(DensityFunction $$0, double $$1, DensityFunction $$2) {
      return add(mul($$0, add($$2, constant(-$$1))), constant($$1));
   }

   public static DensityFunction findTopSurface(DensityFunction $$0, DensityFunction $$1, int $$2, int $$3) {
      return new DensityFunctions.FindTopSurface($$0, $$1, $$2, $$3);
   }

   record Ap2(DensityFunctions.TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue)
      implements DensityFunctions.TwoArgumentSimpleFunction {
      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         double $$1 = this.argument1.compute($$0);

         return switch (this.type) {
            case ADD -> $$1 + this.argument2.compute($$0);
            case MUL -> $$1 == 0.0 ? 0.0 : $$1 * this.argument2.compute($$0);
            case MIN -> $$1 < this.argument2.minValue() ? $$1 : Math.min($$1, this.argument2.compute($$0));
            case MAX -> $$1 > this.argument2.maxValue() ? $$1 : Math.max($$1, this.argument2.compute($$0));
         };
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         this.argument1.fillArray($$0, $$1);
         switch (this.type) {
            case ADD:
               double[] $$2 = new double[$$0.length];
               this.argument2.fillArray($$2, $$1);

               for (int $$3 = 0; $$3 < $$0.length; $$3++) {
                  $$0[$$3] += $$2[$$3];
               }
               break;
            case MUL:
               for (int $$4 = 0; $$4 < $$0.length; $$4++) {
                  double $$5 = $$0[$$4];
                  $$0[$$4] = $$5 == 0.0 ? 0.0 : $$5 * this.argument2.compute($$1.forIndex($$4));
               }
               break;
            case MIN:
               double $$6 = this.argument2.minValue();

               for (int $$7 = 0; $$7 < $$0.length; $$7++) {
                  double $$8 = $$0[$$7];
                  $$0[$$7] = $$8 < $$6 ? $$8 : Math.min($$8, this.argument2.compute($$1.forIndex($$7)));
               }
               break;
            case MAX:
               double $$9 = this.argument2.maxValue();

               for (int $$10 = 0; $$10 < $$0.length; $$10++) {
                  double $$11 = $$0[$$10];
                  $$0[$$10] = $$11 > $$9 ? $$11 : Math.max($$11, this.argument2.compute($$1.forIndex($$10)));
               }
         }
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(DensityFunctions.TwoArgumentSimpleFunction.create(this.type, this.argument1.mapAll($$0), this.argument2.mapAll($$0)));
      }
   }

   protected static enum BeardifierMarker implements DensityFunctions.BeardifierOrMarker {
      INSTANCE;

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return 0.0;
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         Arrays.fill($$0, 0.0);
      }

      @Override
      public double minValue() {
         return 0.0;
      }

      @Override
      public double maxValue() {
         return 0.0;
      }
   }

   public interface BeardifierOrMarker extends DensityFunction.SimpleFunction {
      KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(DensityFunctions.BeardifierMarker.INSTANCE));

      @Override
      default KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
      INSTANCE;

      public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return 1.0;
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         Arrays.fill($$0, 1.0);
      }

      @Override
      public double minValue() {
         return 1.0;
      }

      @Override
      public double maxValue() {
         return 1.0;
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
      static final KeyDispatchDataCodec<DensityFunctions.BlendDensity> CODEC = DensityFunctions.singleFunctionArgumentCodec(
         DensityFunctions.BlendDensity::new, DensityFunctions.BlendDensity::input
      );

      @Override
      public double transform(DensityFunction.FunctionContext $$0, double $$1) {
         return $$0.getBlender().blendDensity($$0, $$1);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.BlendDensity(this.input.mapAll($$0)));
      }

      @Override
      public double minValue() {
         return Double.NEGATIVE_INFINITY;
      }

      @Override
      public double maxValue() {
         return Double.POSITIVE_INFINITY;
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static enum BlendOffset implements DensityFunction.SimpleFunction {
      INSTANCE;

      public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return 0.0;
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         Arrays.fill($$0, 0.0);
      }

      @Override
      public double minValue() {
         return 0.0;
      }

      @Override
      public double maxValue() {
         return 0.0;
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected record Clamp(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
      private static final MapCodec<DensityFunctions.Clamp> DATA_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(DensityFunctions.Clamp::input),
               DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min").forGetter(DensityFunctions.Clamp::minValue),
               DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max").forGetter(DensityFunctions.Clamp::maxValue)
            )
            .apply($$0, DensityFunctions.Clamp::new)
      );
      public static final KeyDispatchDataCodec<DensityFunctions.Clamp> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double transform(double $$0) {
         return Mth.clamp($$0, this.minValue, this.maxValue);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return new DensityFunctions.Clamp(this.input.mapAll($$0), this.minValue, this.maxValue);
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   record Constant(double value) implements DensityFunction.SimpleFunction {
      static final KeyDispatchDataCodec<DensityFunctions.Constant> CODEC = DensityFunctions.singleArgumentCodec(
         DensityFunctions.NOISE_VALUE_CODEC, DensityFunctions.Constant::new, DensityFunctions.Constant::value
      );
      static final DensityFunctions.Constant ZERO = new DensityFunctions.Constant(0.0);

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return this.value;
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         Arrays.fill($$0, this.value);
      }

      @Override
      public double minValue() {
         return this.value;
      }

      @Override
      public double maxValue() {
         return this.value;
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
      public static final KeyDispatchDataCodec<DensityFunctions.EndIslandDensityFunction> CODEC = KeyDispatchDataCodec.of(
         MapCodec.unit(new DensityFunctions.EndIslandDensityFunction(0L))
      );
      private static final float ISLAND_THRESHOLD = -0.9F;
      private final SimplexNoise islandNoise;

      public EndIslandDensityFunction(long $$0) {
         RandomSource $$1 = new LegacyRandomSource($$0);
         $$1.consumeCount(17292);
         this.islandNoise = new SimplexNoise($$1);
      }

      private static float getHeightValue(SimplexNoise $$0, int $$1, int $$2) {
         int $$3 = $$1 / 2;
         int $$4 = $$2 / 2;
         int $$5 = $$1 % 2;
         int $$6 = $$2 % 2;
         float $$7 = 100.0F - Mth.sqrt($$1 * $$1 + $$2 * $$2) * 8.0F;
         $$7 = Mth.clamp($$7, -100.0F, 80.0F);

         for (int $$8 = -12; $$8 <= 12; $$8++) {
            for (int $$9 = -12; $$9 <= 12; $$9++) {
               long $$10 = $$3 + $$8;
               long $$11 = $$4 + $$9;
               if ($$10 * $$10 + $$11 * $$11 > 4096L && $$0.getValue($$10, $$11) < -0.9F) {
                  float $$12 = (Mth.abs((float)$$10) * 3439.0F + Mth.abs((float)$$11) * 147.0F) % 13.0F + 9.0F;
                  float $$13 = $$5 - $$8 * 2;
                  float $$14 = $$6 - $$9 * 2;
                  float $$15 = 100.0F - Mth.sqrt($$13 * $$13 + $$14 * $$14) * $$12;
                  $$15 = Mth.clamp($$15, -100.0F, 80.0F);
                  $$7 = Math.max($$7, $$15);
               }
            }
         }

         return $$7;
      }

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return (getHeightValue(this.islandNoise, $$0.blockX() / 8, $$0.blockZ() / 8) - 8.0) / 128.0;
      }

      @Override
      public double minValue() {
         return -0.84375;
      }

      @Override
      public double maxValue() {
         return 0.5625;
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   record FindTopSurface(DensityFunction density, DensityFunction upperBound, int lowerBound, int cellHeight) implements DensityFunction {
      private static final MapCodec<DensityFunctions.FindTopSurface> DATA_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("density").forGetter(DensityFunctions.FindTopSurface::density),
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("upper_bound").forGetter(DensityFunctions.FindTopSurface::upperBound),
               Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("lower_bound").forGetter(DensityFunctions.FindTopSurface::lowerBound),
               ExtraCodecs.POSITIVE_INT.fieldOf("cell_height").forGetter(DensityFunctions.FindTopSurface::cellHeight)
            )
            .apply($$0, DensityFunctions.FindTopSurface::new)
      );
      public static final KeyDispatchDataCodec<DensityFunctions.FindTopSurface> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         int $$1 = Mth.floor(this.upperBound.compute($$0) / this.cellHeight) * this.cellHeight;
         if ($$1 <= this.lowerBound) {
            return this.lowerBound;
         } else {
            for (int $$2 = $$1; $$2 >= this.lowerBound; $$2 -= this.cellHeight) {
               if (this.density.compute(new DensityFunction.SinglePointContext($$0.blockX(), $$2, $$0.blockZ())) > 0.0) {
                  return $$2;
               }
            }

            return this.lowerBound;
         }
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         $$1.fillAllDirectly($$0, this);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.FindTopSurface(this.density.mapAll($$0), this.upperBound.mapAll($$0), this.lowerBound, this.cellHeight));
      }

      @Override
      public double minValue() {
         return this.lowerBound;
      }

      @Override
      public double maxValue() {
         return Math.max((double)this.lowerBound, this.upperBound.maxValue());
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   @VisibleForDebug
   public record HolderHolder(Holder<DensityFunction> function) implements DensityFunction {
      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return ((DensityFunction)this.function.value()).compute($$0);
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         ((DensityFunction)this.function.value()).fillArray($$0, $$1);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.HolderHolder(new Direct(((DensityFunction)this.function.value()).mapAll($$0))));
      }

      @Override
      public double minValue() {
         return this.function.isBound() ? ((DensityFunction)this.function.value()).minValue() : Double.NEGATIVE_INFINITY;
      }

      @Override
      public double maxValue() {
         return this.function.isBound() ? ((DensityFunction)this.function.value()).maxValue() : Double.POSITIVE_INFINITY;
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
      }
   }

   protected record Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double minValue, double maxValue)
      implements DensityFunctions.PureTransformer {
      public static DensityFunctions.Mapped create(DensityFunctions.Mapped.Type $$0, DensityFunction $$1) {
         double $$2 = $$1.minValue();
         double $$3 = $$1.maxValue();
         double $$4 = transform($$0, $$2);
         double $$5 = transform($$0, $$3);
         if ($$0 == DensityFunctions.Mapped.Type.INVERT) {
            return $$2 < 0.0 && $$3 > 0.0
               ? new DensityFunctions.Mapped($$0, $$1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
               : new DensityFunctions.Mapped($$0, $$1, $$5, $$4);
         } else {
            return $$0 != DensityFunctions.Mapped.Type.ABS && $$0 != DensityFunctions.Mapped.Type.SQUARE
               ? new DensityFunctions.Mapped($$0, $$1, $$4, $$5)
               : new DensityFunctions.Mapped($$0, $$1, Math.max(0.0, $$2), Math.max($$4, $$5));
         }
      }

      private static double transform(DensityFunctions.Mapped.Type $$0, double $$1) {
         return switch ($$0) {
            case ABS -> Math.abs($$1);
            case SQUARE -> $$1 * $$1;
            case CUBE -> $$1 * $$1 * $$1;
            case HALF_NEGATIVE -> $$1 > 0.0 ? $$1 : $$1 * 0.5;
            case QUARTER_NEGATIVE -> $$1 > 0.0 ? $$1 : $$1 * 0.25;
            case INVERT -> 1.0 / $$1;
            case SQUEEZE -> {
               double $$2 = Mth.clamp($$1, -1.0, 1.0);
               yield $$2 / 2.0 - $$2 * $$2 * $$2 / 24.0;
            }
         };
      }

      @Override
      public double transform(double $$0) {
         return transform(this.type, $$0);
      }

      public DensityFunctions.Mapped mapAll(DensityFunction.Visitor $$0) {
         return create(this.type, this.input.mapAll($$0));
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return this.type.codec;
      }

      static enum Type implements StringRepresentable {
         ABS("abs"),
         SQUARE("square"),
         CUBE("cube"),
         HALF_NEGATIVE("half_negative"),
         QUARTER_NEGATIVE("quarter_negative"),
         INVERT("invert"),
         SQUEEZE("squeeze");

         private final String name;
         final KeyDispatchDataCodec<DensityFunctions.Mapped> codec = DensityFunctions.singleFunctionArgumentCodec(
            $$0x -> DensityFunctions.Mapped.create(this, $$0x), DensityFunctions.Mapped::input
         );

         private Type(final String $$0) {
            this.name = $$0;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   protected record Marker(DensityFunctions.Marker.Type type, DensityFunction wrapped) implements DensityFunctions.MarkerOrMarked {
      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return this.wrapped.compute($$0);
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         this.wrapped.fillArray($$0, $$1);
      }

      @Override
      public double minValue() {
         return this.wrapped.minValue();
      }

      @Override
      public double maxValue() {
         return this.wrapped.maxValue();
      }

      static enum Type implements StringRepresentable {
         Interpolated("interpolated"),
         FlatCache("flat_cache"),
         Cache2D("cache_2d"),
         CacheOnce("cache_once"),
         CacheAllInCell("cache_all_in_cell");

         private final String name;
         final KeyDispatchDataCodec<DensityFunctions.MarkerOrMarked> codec = DensityFunctions.singleFunctionArgumentCodec(
            $$0x -> new DensityFunctions.Marker(this, $$0x), DensityFunctions.MarkerOrMarked::wrapped
         );

         private Type(final String $$0) {
            this.name = $$0;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   public interface MarkerOrMarked extends DensityFunction {
      DensityFunctions.Marker.Type type();

      DensityFunction wrapped();

      @Override
      default KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return this.type().codec;
      }

      @Override
      default DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.Marker(this.type(), this.wrapped().mapAll($$0)));
      }
   }

   record MulOrAdd(DensityFunctions.MulOrAdd.Type specificType, DensityFunction input, double minValue, double maxValue, double argument)
      implements DensityFunctions.PureTransformer,
      DensityFunctions.TwoArgumentSimpleFunction {
      @Override
      public DensityFunctions.TwoArgumentSimpleFunction.Type type() {
         return this.specificType == DensityFunctions.MulOrAdd.Type.MUL
            ? DensityFunctions.TwoArgumentSimpleFunction.Type.MUL
            : DensityFunctions.TwoArgumentSimpleFunction.Type.ADD;
      }

      @Override
      public DensityFunction argument1() {
         return DensityFunctions.constant(this.argument);
      }

      @Override
      public DensityFunction argument2() {
         return this.input;
      }

      @Override
      public double transform(double $$0) {
         return switch (this.specificType) {
            case MUL -> $$0 * this.argument;
            case ADD -> $$0 + this.argument;
         };
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         DensityFunction $$1 = this.input.mapAll($$0);
         double $$2 = $$1.minValue();
         double $$3 = $$1.maxValue();
         double $$4;
         double $$5;
         if (this.specificType == DensityFunctions.MulOrAdd.Type.ADD) {
            $$4 = $$2 + this.argument;
            $$5 = $$3 + this.argument;
         } else if (this.argument >= 0.0) {
            $$4 = $$2 * this.argument;
            $$5 = $$3 * this.argument;
         } else {
            $$4 = $$3 * this.argument;
            $$5 = $$2 * this.argument;
         }

         return new DensityFunctions.MulOrAdd(this.specificType, $$1, $$4, $$5, this.argument);
      }

      static enum Type {
         MUL,
         ADD;
      }
   }

   protected record Noise(DensityFunction.NoiseHolder noise, @Deprecated double xzScale, double yScale) implements DensityFunction {
      public static final MapCodec<DensityFunctions.Noise> DATA_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.Noise::noise),
               Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.Noise::xzScale),
               Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.Noise::yScale)
            )
            .apply($$0, DensityFunctions.Noise::new)
      );
      public static final KeyDispatchDataCodec<DensityFunctions.Noise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return this.noise.getValue($$0.blockX() * this.xzScale, $$0.blockY() * this.yScale, $$0.blockZ() * this.xzScale);
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         $$1.fillAllDirectly($$0, this);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.Noise($$0.visitNoise(this.noise), this.xzScale, this.yScale));
      }

      @Override
      public double minValue() {
         return -this.maxValue();
      }

      @Override
      public double maxValue() {
         return this.noise.maxValue();
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   interface PureTransformer extends DensityFunction {
      DensityFunction input();

      @Override
      default double compute(DensityFunction.FunctionContext $$0) {
         return this.transform(this.input().compute($$0));
      }

      @Override
      default void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         this.input().fillArray($$0, $$1);

         for (int $$2 = 0; $$2 < $$0.length; $$2++) {
            $$0[$$2] = this.transform($$0[$$2]);
         }
      }

      double transform(double var1);
   }

   record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange)
      implements DensityFunction {
      public static final MapCodec<DensityFunctions.RangeChoice> DATA_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.RangeChoice::input),
               DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(DensityFunctions.RangeChoice::minInclusive),
               DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(DensityFunctions.RangeChoice::maxExclusive),
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(DensityFunctions.RangeChoice::whenInRange),
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(DensityFunctions.RangeChoice::whenOutOfRange)
            )
            .apply($$0, DensityFunctions.RangeChoice::new)
      );
      public static final KeyDispatchDataCodec<DensityFunctions.RangeChoice> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         double $$1 = this.input.compute($$0);
         return $$1 >= this.minInclusive && $$1 < this.maxExclusive ? this.whenInRange.compute($$0) : this.whenOutOfRange.compute($$0);
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         this.input.fillArray($$0, $$1);

         for (int $$2 = 0; $$2 < $$0.length; $$2++) {
            double $$3 = $$0[$$2];
            if ($$3 >= this.minInclusive && $$3 < this.maxExclusive) {
               $$0[$$2] = this.whenInRange.compute($$1.forIndex($$2));
            } else {
               $$0[$$2] = this.whenOutOfRange.compute($$1.forIndex($$2));
            }
         }
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(
            new DensityFunctions.RangeChoice(
               this.input.mapAll($$0), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll($$0), this.whenOutOfRange.mapAll($$0)
            )
         );
      }

      @Override
      public double minValue() {
         return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
      }

      @Override
      public double maxValue() {
         return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected record Shift(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
      static final KeyDispatchDataCodec<DensityFunctions.Shift> CODEC = DensityFunctions.singleArgumentCodec(
         DensityFunction.NoiseHolder.CODEC, DensityFunctions.Shift::new, DensityFunctions.Shift::offsetNoise
      );

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return this.compute($$0.blockX(), $$0.blockY(), $$0.blockZ());
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.Shift($$0.visitNoise(this.offsetNoise)));
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected record ShiftA(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
      static final KeyDispatchDataCodec<DensityFunctions.ShiftA> CODEC = DensityFunctions.singleArgumentCodec(
         DensityFunction.NoiseHolder.CODEC, DensityFunctions.ShiftA::new, DensityFunctions.ShiftA::offsetNoise
      );

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return this.compute($$0.blockX(), 0.0, $$0.blockZ());
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.ShiftA($$0.visitNoise(this.offsetNoise)));
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected record ShiftB(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
      static final KeyDispatchDataCodec<DensityFunctions.ShiftB> CODEC = DensityFunctions.singleArgumentCodec(
         DensityFunction.NoiseHolder.CODEC, DensityFunctions.ShiftB::new, DensityFunctions.ShiftB::offsetNoise
      );

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return this.compute($$0.blockZ(), $$0.blockX(), 0.0);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.ShiftB($$0.visitNoise(this.offsetNoise)));
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   interface ShiftNoise extends DensityFunction {
      DensityFunction.NoiseHolder offsetNoise();

      @Override
      default double minValue() {
         return -this.maxValue();
      }

      @Override
      default double maxValue() {
         return this.offsetNoise().maxValue() * 4.0;
      }

      default double compute(double $$0, double $$1, double $$2) {
         return this.offsetNoise().getValue($$0 * 0.25, $$1 * 0.25, $$2 * 0.25) * 4.0;
      }

      @Override
      default void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         $$1.fillAllDirectly($$0, this);
      }
   }

   protected record ShiftedNoise(
      DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.NoiseHolder noise
   ) implements DensityFunction {
      private static final MapCodec<DensityFunctions.ShiftedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DensityFunctions.ShiftedNoise::shiftX),
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(DensityFunctions.ShiftedNoise::shiftY),
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DensityFunctions.ShiftedNoise::shiftZ),
               Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.ShiftedNoise::xzScale),
               Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.ShiftedNoise::yScale),
               DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.ShiftedNoise::noise)
            )
            .apply($$0, DensityFunctions.ShiftedNoise::new)
      );
      public static final KeyDispatchDataCodec<DensityFunctions.ShiftedNoise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         double $$1 = $$0.blockX() * this.xzScale + this.shiftX.compute($$0);
         double $$2 = $$0.blockY() * this.yScale + this.shiftY.compute($$0);
         double $$3 = $$0.blockZ() * this.xzScale + this.shiftZ.compute($$0);
         return this.noise.getValue($$1, $$2, $$3);
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         $$1.fillAllDirectly($$0, this);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(
            new DensityFunctions.ShiftedNoise(
               this.shiftX.mapAll($$0), this.shiftY.mapAll($$0), this.shiftZ.mapAll($$0), this.xzScale, this.yScale, $$0.visitNoise(this.noise)
            )
         );
      }

      @Override
      public double minValue() {
         return -this.maxValue();
      }

      @Override
      public double maxValue() {
         return this.noise.maxValue();
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   public record Spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline) implements DensityFunction {
      private static final Codec<CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>> SPLINE_CODEC = CubicSpline.codec(
         DensityFunctions.Spline.Coordinate.CODEC
      );
      private static final MapCodec<DensityFunctions.Spline> DATA_CODEC = SPLINE_CODEC.fieldOf("spline")
         .xmap(DensityFunctions.Spline::new, DensityFunctions.Spline::spline);
      public static final KeyDispatchDataCodec<DensityFunctions.Spline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return this.spline.apply(new DensityFunctions.Spline.Point($$0));
      }

      @Override
      public double minValue() {
         return this.spline.minValue();
      }

      @Override
      public double maxValue() {
         return this.spline.maxValue();
      }

      @Override
      public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         $$1.fillAllDirectly($$0, this);
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.Spline(this.spline.mapAll($$1 -> $$1.mapAll($$0))));
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public record Coordinate(Holder<DensityFunction> function) implements BoundedFloatFunction<DensityFunctions.Spline.Point> {
         public static final Codec<DensityFunctions.Spline.Coordinate> CODEC = DensityFunction.CODEC
            .xmap(DensityFunctions.Spline.Coordinate::new, DensityFunctions.Spline.Coordinate::function);

         @Override
         public String toString() {
            Optional<ResourceKey<DensityFunction>> $$0 = this.function.unwrapKey();
            if ($$0.isPresent()) {
               ResourceKey<DensityFunction> $$1 = $$0.get();
               if ($$1 == NoiseRouterData.CONTINENTS) {
                  return "continents";
               }

               if ($$1 == NoiseRouterData.EROSION) {
                  return "erosion";
               }

               if ($$1 == NoiseRouterData.RIDGES) {
                  return "weirdness";
               }

               if ($$1 == NoiseRouterData.RIDGES_FOLDED) {
                  return "ridges";
               }
            }

            return "Coordinate[" + this.function + "]";
         }

         public float apply(DensityFunctions.Spline.Point $$0) {
            return (float)((DensityFunction)this.function.value()).compute($$0.context());
         }

         public float minValue() {
            return this.function.isBound() ? (float)((DensityFunction)this.function.value()).minValue() : Float.NEGATIVE_INFINITY;
         }

         public float maxValue() {
            return this.function.isBound() ? (float)((DensityFunction)this.function.value()).maxValue() : Float.POSITIVE_INFINITY;
         }

         public DensityFunctions.Spline.Coordinate mapAll(DensityFunction.Visitor $$0) {
            return new DensityFunctions.Spline.Coordinate(new Direct(((DensityFunction)this.function.value()).mapAll($$0)));
         }
      }

      public record Point(DensityFunction.FunctionContext context) {
      }
   }

   interface TransformerWithContext extends DensityFunction {
      DensityFunction input();

      @Override
      default double compute(DensityFunction.FunctionContext $$0) {
         return this.transform($$0, this.input().compute($$0));
      }

      @Override
      default void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         this.input().fillArray($$0, $$1);

         for (int $$2 = 0; $$2 < $$0.length; $$2++) {
            $$0[$$2] = this.transform($$1.forIndex($$2), $$0[$$2]);
         }
      }

      double transform(DensityFunction.FunctionContext var1, double var2);
   }

   interface TwoArgumentSimpleFunction extends DensityFunction {
      Logger LOGGER = LogUtils.getLogger();

      static DensityFunctions.TwoArgumentSimpleFunction create(DensityFunctions.TwoArgumentSimpleFunction.Type $$0, DensityFunction $$1, DensityFunction $$2) {
         double $$3 = $$1.minValue();
         double $$4 = $$2.minValue();
         double $$5 = $$1.maxValue();
         double $$6 = $$2.maxValue();
         if ($$0 == DensityFunctions.TwoArgumentSimpleFunction.Type.MIN || $$0 == DensityFunctions.TwoArgumentSimpleFunction.Type.MAX) {
            boolean $$7 = $$3 >= $$6;
            boolean $$8 = $$4 >= $$5;
            if ($$7 || $$8) {
               LOGGER.warn("Creating a {} function between two non-overlapping inputs: {} and {}", new Object[]{$$0, $$1, $$2});
            }
         }
         double $$9 = switch ($$0) {
            case ADD -> $$3 + $$4;
            case MUL -> $$3 > 0.0 && $$4 > 0.0 ? $$3 * $$4 : ($$5 < 0.0 && $$6 < 0.0 ? $$5 * $$6 : Math.min($$3 * $$6, $$5 * $$4));
            case MIN -> Math.min($$3, $$4);
            case MAX -> Math.max($$3, $$4);
         };

         double $$10 = switch ($$0) {
            case ADD -> $$5 + $$6;
            case MUL -> $$3 > 0.0 && $$4 > 0.0 ? $$5 * $$6 : ($$5 < 0.0 && $$6 < 0.0 ? $$3 * $$4 : Math.max($$3 * $$4, $$5 * $$6));
            case MIN -> Math.min($$5, $$6);
            case MAX -> Math.max($$5, $$6);
         };
         if ($$0 == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL || $$0 == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
            if ($$1 instanceof DensityFunctions.Constant $$11) {
               return new DensityFunctions.MulOrAdd(
                  $$0 == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
                  $$2,
                  $$9,
                  $$10,
                  $$11.value
               );
            }

            if ($$2 instanceof DensityFunctions.Constant $$12) {
               return new DensityFunctions.MulOrAdd(
                  $$0 == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL,
                  $$1,
                  $$9,
                  $$10,
                  $$12.value
               );
            }
         }

         return new DensityFunctions.Ap2($$0, $$1, $$2, $$9, $$10);
      }

      DensityFunctions.TwoArgumentSimpleFunction.Type type();

      DensityFunction argument1();

      DensityFunction argument2();

      @Override
      default KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return this.type().codec;
      }

      public static enum Type implements StringRepresentable {
         ADD("add"),
         MUL("mul"),
         MIN("min"),
         MAX("max");

         final KeyDispatchDataCodec<DensityFunctions.TwoArgumentSimpleFunction> codec = DensityFunctions.doubleFunctionArgumentCodec(
            ($$0x, $$1) -> DensityFunctions.TwoArgumentSimpleFunction.create(this, $$0x, $$1),
            DensityFunctions.TwoArgumentSimpleFunction::argument1,
            DensityFunctions.TwoArgumentSimpleFunction::argument2
         );
         private final String name;

         private Type(final String $$0) {
            this.name = $$0;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   protected record WeirdScaledSampler(
      DensityFunction input, DensityFunction.NoiseHolder noise, DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper
   ) implements DensityFunctions.TransformerWithContext {
      private static final MapCodec<DensityFunctions.WeirdScaledSampler> DATA_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.WeirdScaledSampler::input),
               DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.WeirdScaledSampler::noise),
               DensityFunctions.WeirdScaledSampler.RarityValueMapper.CODEC
                  .fieldOf("rarity_value_mapper")
                  .forGetter(DensityFunctions.WeirdScaledSampler::rarityValueMapper)
            )
            .apply($$0, DensityFunctions.WeirdScaledSampler::new)
      );
      public static final KeyDispatchDataCodec<DensityFunctions.WeirdScaledSampler> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double transform(DensityFunction.FunctionContext $$0, double $$1) {
         double $$2 = this.rarityValueMapper.mapper.get($$1);
         return $$2 * Math.abs(this.noise.getValue($$0.blockX() / $$2, $$0.blockY() / $$2, $$0.blockZ() / $$2));
      }

      @Override
      public DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(new DensityFunctions.WeirdScaledSampler(this.input.mapAll($$0), $$0.visitNoise(this.noise), this.rarityValueMapper));
      }

      @Override
      public double minValue() {
         return 0.0;
      }

      @Override
      public double maxValue() {
         return this.rarityValueMapper.maxRarity * this.noise.maxValue();
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public static enum RarityValueMapper implements StringRepresentable {
         TYPE1("type_1", NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0),
         TYPE2("type_2", NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0);

         public static final Codec<DensityFunctions.WeirdScaledSampler.RarityValueMapper> CODEC = StringRepresentable.fromEnum(
            DensityFunctions.WeirdScaledSampler.RarityValueMapper::values
         );
         private final String name;
         final Double2DoubleFunction mapper;
         final double maxRarity;

         private RarityValueMapper(final String $$0, final Double2DoubleFunction $$1, final double $$2) {
            this.name = $$0;
            this.mapper = $$1;
            this.maxRarity = $$2;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction {
      private static final MapCodec<DensityFunctions.YClampedGradient> DATA_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("from_y").forGetter(DensityFunctions.YClampedGradient::fromY),
               Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("to_y").forGetter(DensityFunctions.YClampedGradient::toY),
               DensityFunctions.NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(DensityFunctions.YClampedGradient::fromValue),
               DensityFunctions.NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(DensityFunctions.YClampedGradient::toValue)
            )
            .apply($$0, DensityFunctions.YClampedGradient::new)
      );
      public static final KeyDispatchDataCodec<DensityFunctions.YClampedGradient> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      @Override
      public double compute(DensityFunction.FunctionContext $$0) {
         return Mth.clampedMap($$0.blockY(), this.fromY, this.toY, this.fromValue, this.toValue);
      }

      @Override
      public double minValue() {
         return Math.min(this.fromValue, this.toValue);
      }

      @Override
      public double maxValue() {
         return Math.max(this.fromValue, this.toValue);
      }

      @Override
      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }
}
