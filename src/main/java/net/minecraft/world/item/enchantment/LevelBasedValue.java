package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;

public interface LevelBasedValue {
   Codec<LevelBasedValue> DISPATCH_CODEC = BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.byNameCodec().dispatch(LevelBasedValue::codec, $$0 -> $$0);
   Codec<LevelBasedValue> CODEC = Codec.either(LevelBasedValue.Constant.CODEC, DISPATCH_CODEC)
      .xmap(
         $$0 -> (LevelBasedValue)$$0.map($$0x -> $$0x, $$0x -> $$0x), $$0 -> $$0 instanceof LevelBasedValue.Constant $$1 ? Either.left($$1) : Either.right($$0)
      );

   static MapCodec<? extends LevelBasedValue> bootstrap(Registry<MapCodec<? extends LevelBasedValue>> $$0) {
      Registry.register($$0, "clamped", LevelBasedValue.Clamped.CODEC);
      Registry.register($$0, "fraction", LevelBasedValue.Fraction.CODEC);
      Registry.register($$0, "levels_squared", LevelBasedValue.LevelsSquared.CODEC);
      Registry.register($$0, "linear", LevelBasedValue.Linear.CODEC);
      Registry.register($$0, "exponent", LevelBasedValue.Exponent.CODEC);
      return (MapCodec<? extends LevelBasedValue>)Registry.register($$0, "lookup", LevelBasedValue.Lookup.CODEC);
   }

   static LevelBasedValue.Constant constant(float $$0) {
      return new LevelBasedValue.Constant($$0);
   }

   static LevelBasedValue.Linear perLevel(float $$0, float $$1) {
      return new LevelBasedValue.Linear($$0, $$1);
   }

   static LevelBasedValue.Linear perLevel(float $$0) {
      return perLevel($$0, $$0);
   }

   static LevelBasedValue.Lookup lookup(List<Float> $$0, LevelBasedValue $$1) {
      return new LevelBasedValue.Lookup($$0, $$1);
   }

   float calculate(int var1);

   MapCodec<? extends LevelBasedValue> codec();

   public record Clamped(LevelBasedValue value, float min, float max) implements LevelBasedValue {
      public static final MapCodec<LevelBasedValue.Clamped> CODEC = RecordCodecBuilder.mapCodec(
            $$0 -> $$0.group(
                  LevelBasedValue.CODEC.fieldOf("value").forGetter(LevelBasedValue.Clamped::value),
                  Codec.FLOAT.fieldOf("min").forGetter(LevelBasedValue.Clamped::min),
                  Codec.FLOAT.fieldOf("max").forGetter(LevelBasedValue.Clamped::max)
               )
               .apply($$0, LevelBasedValue.Clamped::new)
         )
         .validate(
            $$0 -> $$0.max <= $$0.min ? DataResult.error(() -> "Max must be larger than min, min: " + $$0.min + ", max: " + $$0.max) : DataResult.success($$0)
         );

      @Override
      public float calculate(int $$0) {
         return Mth.clamp(this.value.calculate($$0), this.min, this.max);
      }

      @Override
      public MapCodec<LevelBasedValue.Clamped> codec() {
         return CODEC;
      }
   }

   public record Constant(float value) implements LevelBasedValue {
      public static final Codec<LevelBasedValue.Constant> CODEC = Codec.FLOAT.xmap(LevelBasedValue.Constant::new, LevelBasedValue.Constant::value);
      public static final MapCodec<LevelBasedValue.Constant> TYPED_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(Codec.FLOAT.fieldOf("value").forGetter(LevelBasedValue.Constant::value)).apply($$0, LevelBasedValue.Constant::new)
      );

      @Override
      public float calculate(int $$0) {
         return this.value;
      }

      @Override
      public MapCodec<LevelBasedValue.Constant> codec() {
         return TYPED_CODEC;
      }
   }

   public record Exponent(LevelBasedValue base, LevelBasedValue power) implements LevelBasedValue {
      public static final MapCodec<LevelBasedValue.Exponent> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               LevelBasedValue.CODEC.fieldOf("base").forGetter(LevelBasedValue.Exponent::base),
               LevelBasedValue.CODEC.fieldOf("power").forGetter(LevelBasedValue.Exponent::power)
            )
            .apply($$0, LevelBasedValue.Exponent::new)
      );

      @Override
      public float calculate(int $$0) {
         return (float)Math.pow(this.base.calculate($$0), this.power.calculate($$0));
      }

      @Override
      public MapCodec<LevelBasedValue.Exponent> codec() {
         return CODEC;
      }
   }

   public record Fraction(LevelBasedValue numerator, LevelBasedValue denominator) implements LevelBasedValue {
      public static final MapCodec<LevelBasedValue.Fraction> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               LevelBasedValue.CODEC.fieldOf("numerator").forGetter(LevelBasedValue.Fraction::numerator),
               LevelBasedValue.CODEC.fieldOf("denominator").forGetter(LevelBasedValue.Fraction::denominator)
            )
            .apply($$0, LevelBasedValue.Fraction::new)
      );

      @Override
      public float calculate(int $$0) {
         float $$1 = this.denominator.calculate($$0);
         return $$1 == 0.0F ? 0.0F : this.numerator.calculate($$0) / $$1;
      }

      @Override
      public MapCodec<LevelBasedValue.Fraction> codec() {
         return CODEC;
      }
   }

   public record LevelsSquared(float added) implements LevelBasedValue {
      public static final MapCodec<LevelBasedValue.LevelsSquared> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(Codec.FLOAT.fieldOf("added").forGetter(LevelBasedValue.LevelsSquared::added)).apply($$0, LevelBasedValue.LevelsSquared::new)
      );

      @Override
      public float calculate(int $$0) {
         return Mth.square($$0) + this.added;
      }

      @Override
      public MapCodec<LevelBasedValue.LevelsSquared> codec() {
         return CODEC;
      }
   }

   public record Linear(float base, float perLevelAboveFirst) implements LevelBasedValue {
      public static final MapCodec<LevelBasedValue.Linear> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.FLOAT.fieldOf("base").forGetter(LevelBasedValue.Linear::base),
               Codec.FLOAT.fieldOf("per_level_above_first").forGetter(LevelBasedValue.Linear::perLevelAboveFirst)
            )
            .apply($$0, LevelBasedValue.Linear::new)
      );

      @Override
      public float calculate(int $$0) {
         return this.base + this.perLevelAboveFirst * ($$0 - 1);
      }

      @Override
      public MapCodec<LevelBasedValue.Linear> codec() {
         return CODEC;
      }
   }

   public record Lookup(List<Float> values, LevelBasedValue fallback) implements LevelBasedValue {
      public static final MapCodec<LevelBasedValue.Lookup> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.FLOAT.listOf().fieldOf("values").forGetter(LevelBasedValue.Lookup::values),
               LevelBasedValue.CODEC.fieldOf("fallback").forGetter(LevelBasedValue.Lookup::fallback)
            )
            .apply($$0, LevelBasedValue.Lookup::new)
      );

      @Override
      public float calculate(int $$0) {
         return $$0 <= this.values.size() ? this.values.get($$0 - 1) : this.fallback.calculate($$0);
      }

      @Override
      public MapCodec<LevelBasedValue.Lookup> codec() {
         return CODEC;
      }
   }
}
