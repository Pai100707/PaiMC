package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class IntProvider {
   private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
      Codec.INT, BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec)
   );
   public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
      $$0 -> (IntProvider)$$0.map(ConstantInt::of, $$0x -> $$0x),
      $$0 -> $$0.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)$$0).getValue()) : Either.right($$0)
   );
   public static final Codec<IntProvider> NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
   public static final Codec<IntProvider> POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);

   public static Codec<IntProvider> codec(int $$0, int $$1) {
      return validateCodec($$0, $$1, CODEC);
   }

   public static <T extends IntProvider> Codec<T> validateCodec(int $$0, int $$1, Codec<T> $$2) {
      return $$2.validate($$2x -> validate($$0, $$1, $$2x));
   }

   private static <T extends IntProvider> DataResult<T> validate(int $$0, int $$1, T $$2) {
      if ($$2.getMinValue() < $$0) {
         return DataResult.error(() -> "Value provider too low: " + $$0 + " [" + $$2.getMinValue() + "-" + $$2.getMaxValue() + "]");
      } else {
         return $$2.getMaxValue() > $$1
            ? DataResult.error(() -> "Value provider too high: " + $$1 + " [" + $$2.getMinValue() + "-" + $$2.getMaxValue() + "]")
            : DataResult.success($$2);
      }
   }

   public abstract int sample(net.minecraft.util.RandomSource var1);

   public abstract int getMinValue();

   public abstract int getMaxValue();

   public abstract IntProviderType<?> getType();
}
