/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.SampledFloat;

public abstract class FloatProvider
implements SampledFloat {
    private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either((Codec)Codec.FLOAT, (Codec)BuiltInRegistries.FLOAT_PROVIDER_TYPE.byNameCodec().dispatch(FloatProvider::getType, FloatProviderType::codec));
    public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap($$02 -> (FloatProvider)$$02.map(ConstantFloat::of, $$0 -> $$0), $$0 -> $$0.getType() == FloatProviderType.CONSTANT ? Either.left((Object)Float.valueOf(((ConstantFloat)$$0).getValue())) : Either.right((Object)$$0));

    public static Codec<FloatProvider> codec(float $$0, float $$1) {
        return CODEC.validate($$2 -> {
            if ($$2.getMinValue() < $$0) {
                return DataResult.error(() -> "Value provider too low: " + $$0 + " [" + $$2.getMinValue() + "-" + $$2.getMaxValue() + "]");
            }
            if ($$2.getMaxValue() > $$1) {
                return DataResult.error(() -> "Value provider too high: " + $$1 + " [" + $$2.getMinValue() + "-" + $$2.getMaxValue() + "]");
            }
            return DataResult.success((Object)$$2);
        });
    }

    public abstract float getMinValue();

    public abstract float getMaxValue();

    public abstract FloatProviderType<?> getType();
}

