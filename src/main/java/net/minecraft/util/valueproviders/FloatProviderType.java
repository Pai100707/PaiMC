package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface FloatProviderType<P extends FloatProvider> {
   FloatProviderType<ConstantFloat> CONSTANT = register("constant", ConstantFloat.CODEC);
   FloatProviderType<UniformFloat> UNIFORM = register("uniform", UniformFloat.CODEC);
   FloatProviderType<ClampedNormalFloat> CLAMPED_NORMAL = register("clamped_normal", ClampedNormalFloat.CODEC);
   FloatProviderType<TrapezoidFloat> TRAPEZOID = register("trapezoid", TrapezoidFloat.CODEC);

   MapCodec<P> codec();

   static <P extends FloatProvider> FloatProviderType<P> register(String $$0, MapCodec<P> $$1) {
      return (FloatProviderType<P>)Registry.register(BuiltInRegistries.FLOAT_PROVIDER_TYPE, $$0, (FloatProviderType<P>)() -> $$1);
   }
}
