/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.EasingType;
import net.minecraft.util.Keyframe;
import net.minecraft.util.KeyframeTrackSampler;
import net.minecraft.world.attribute.LerpFunction;

public record KeyframeTrack<T>(List<Keyframe<T>> keyframes, EasingType easingType) {
    public KeyframeTrack {
        if ($$0.isEmpty()) {
            throw new IllegalArgumentException("Track has no keyframes");
        }
    }

    public static <T> MapCodec<KeyframeTrack<T>> mapCodec(Codec<T> $$0) {
        Codec $$12 = Keyframe.codec($$0).listOf().validate(KeyframeTrack::validateKeyframes);
        return RecordCodecBuilder.mapCodec((T $$1) -> $$1.group((App)$$12.fieldOf("keyframes").forGetter(KeyframeTrack::keyframes), (App)EasingType.CODEC.optionalFieldOf("ease", (Object)EasingType.LINEAR).forGetter(KeyframeTrack::easingType)).apply((Applicative)$$1, KeyframeTrack::new));
    }

    static <T> DataResult<List<Keyframe<T>>> validateKeyframes(List<Keyframe<T>> $$0) {
        if ($$0.isEmpty()) {
            return DataResult.error(() -> "Keyframes must not be empty");
        }
        if (!Comparators.isInOrder($$0, Comparator.comparingInt(Keyframe::ticks))) {
            return DataResult.error(() -> "Keyframes must be ordered by ticks field");
        }
        if ($$0.size() > 1) {
            int $$1 = 0;
            int $$2 = $$0.getLast().ticks();
            for (Keyframe $$3 : $$0) {
                if ($$3.ticks() == $$2) {
                    if (++$$1 > 2) {
                        return DataResult.error(() -> "More than 2 keyframes on same tick: " + $$3.ticks());
                    }
                } else {
                    $$1 = 0;
                }
                $$2 = $$3.ticks();
            }
        }
        return DataResult.success($$0);
    }

    public static DataResult<KeyframeTrack<?>> validatePeriod(KeyframeTrack<?> $$0, int $$1) {
        for (Keyframe<?> $$2 : $$0.keyframes()) {
            int $$3 = $$2.ticks();
            if ($$3 >= 0 && $$3 <= $$1) continue;
            return DataResult.error(() -> "Keyframe at tick " + $$2.ticks() + " must be in range [0; " + $$1 + "]");
        }
        return DataResult.success($$0);
    }

    public KeyframeTrackSampler<T> bakeSampler(Optional<Integer> $$0, LerpFunction<T> $$1) {
        return new KeyframeTrackSampler<T>(this, $$0, $$1);
    }

    public static class Builder<T> {
        private final ImmutableList.Builder<Keyframe<T>> keyframes = ImmutableList.builder();
        private EasingType easing = EasingType.LINEAR;

        public Builder<T> addKeyframe(int $$0, T $$1) {
            this.keyframes.add(new Keyframe<T>($$0, $$1));
            return this;
        }

        public Builder<T> setEasing(EasingType $$0) {
            this.easing = $$0;
            return this;
        }

        public KeyframeTrack<T> build() {
            List $$0 = (List)KeyframeTrack.validateKeyframes(this.keyframes.build()).getOrThrow();
            return new KeyframeTrack($$0, this.easing);
        }
    }
}

