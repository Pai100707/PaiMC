package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class TrapezoidFloat extends FloatProvider {
   public static final MapCodec<TrapezoidFloat> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.FLOAT.fieldOf("min").forGetter($$0x -> $$0x.min),
               Codec.FLOAT.fieldOf("max").forGetter($$0x -> $$0x.max),
               Codec.FLOAT.fieldOf("plateau").forGetter($$0x -> $$0x.plateau)
            )
            .apply($$0, TrapezoidFloat::new)
      )
      .validate(
         $$0 -> {
            if ($$0.max < $$0.min) {
               return DataResult.error(() -> "Max must be larger than min: [" + $$0.min + ", " + $$0.max + "]");
            } else {
               return $$0.plateau > $$0.max - $$0.min
                  ? DataResult.error(() -> "Plateau can at most be the full span: [" + $$0.min + ", " + $$0.max + "]")
                  : DataResult.success($$0);
            }
         }
      );
   private final float min;
   private final float max;
   private final float plateau;

   public static TrapezoidFloat of(float $$0, float $$1, float $$2) {
      return new TrapezoidFloat($$0, $$1, $$2);
   }

   private TrapezoidFloat(float $$0, float $$1, float $$2) {
      this.min = $$0;
      this.max = $$1;
      this.plateau = $$2;
   }

   @Override
   public float sample(net.minecraft.util.RandomSource $$0) {
      float $$1 = this.max - this.min;
      float $$2 = ($$1 - this.plateau) / 2.0F;
      float $$3 = $$1 - $$2;
      return this.min + $$0.nextFloat() * $$3 + $$0.nextFloat() * $$2;
   }

   @Override
   public float getMinValue() {
      return this.min;
   }

   @Override
   public float getMaxValue() {
      return this.max;
   }

   @Override
   public FloatProviderType<?> getType() {
      return FloatProviderType.TRAPEZOID;
   }

   @Override
   public String toString() {
      return "trapezoid(" + this.plateau + ") in [" + this.min + "-" + this.max + "]";
   }
}
