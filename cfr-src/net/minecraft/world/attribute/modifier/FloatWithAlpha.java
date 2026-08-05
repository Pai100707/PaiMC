/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FloatWithAlpha(float value, float alpha) {
    private static final Codec<FloatWithAlpha> FULL_CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.FLOAT.fieldOf("value").forGetter(FloatWithAlpha::value), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("alpha", (Object)Float.valueOf(1.0f)).forGetter(FloatWithAlpha::alpha)).apply((Applicative)$$0, FloatWithAlpha::new));
    public static final Codec<FloatWithAlpha> CODEC = Codec.either((Codec)Codec.FLOAT, FULL_CODEC).xmap($$02 -> (FloatWithAlpha)$$02.map(FloatWithAlpha::new, $$0 -> $$0), $$0 -> $$0.alpha() == 1.0f ? Either.left((Object)Float.valueOf($$0.value())) : Either.right((Object)$$0));

    public FloatWithAlpha(float $$0) {
        this($$0, 1.0f);
    }
}

