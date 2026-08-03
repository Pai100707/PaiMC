package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class FloatProvider implements SampledFloat {
   private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
      Codec.FLOAT, BuiltInRegistries.FLOAT_PROVIDER_TYPE.byNameCodec().dispatch(FloatProvider::getType, FloatProviderType::codec)
   );
   public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
      $$0 -> (FloatProvider)$$0.map(ConstantFloat::of, $$0x -> $$0x),
      $$0 -> $$0.getType() == FloatProviderType.CONSTANT ? Either.left(((ConstantFloat)$$0).getValue()) : Either.right($$0)
   );

   public static Codec<FloatProvider> codec(float $$0, float $$1) {
      return CODEC.validate(
         $$2 -> {
            if ($$2.getMinValue() < $$0) {
               return DataResult.error(() -> "Value provider too low: " + $$0 + " [" + $$2.getMinValue() + "-" + $$2.getMaxValue() + "]");
            } else {
               return $$2.getMaxValue() > $$1
                  ? DataResult.error(() -> "Value provider too high: " + $$1 + " [" + $$2.getMinValue() + "-" + $$2.getMaxValue() + "]")
                  : DataResult.success($$2);
            }
         }
      );
   }

   public abstract float getMinValue();

   public abstract float getMaxValue();

   public abstract FloatProviderType<?> getType();
}
