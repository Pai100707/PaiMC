/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.slf4j.Logger
 */
package net.minecraft.util.random;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public record Weighted<T>(T value, int weight) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Weighted {
        if ($$1 < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
        }
        if ($$1 == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.warn("Found 0 weight, make sure this is intentional!");
        }
    }

    public static <E> Codec<Weighted<E>> codec(Codec<E> $$0) {
        return Weighted.codec($$0.fieldOf("data"));
    }

    public static <E> Codec<Weighted<E>> codec(MapCodec<E> $$0) {
        return RecordCodecBuilder.create($$1 -> $$1.group((App)$$0.forGetter(Weighted::value), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weight").forGetter(Weighted::weight)).apply((Applicative)$$1, Weighted::new));
    }

    public static <B extends ByteBuf, T> StreamCodec<B, Weighted<T>> streamCodec(StreamCodec<B, T> $$0) {
        return StreamCodec.composite($$0, Weighted::value, ByteBufCodecs.VAR_INT, Weighted::weight, Weighted::new);
    }

    public <U> Weighted<U> map(Function<T, U> $$0) {
        return new Weighted<U>($$0.apply(this.value()), this.weight);
    }
}

