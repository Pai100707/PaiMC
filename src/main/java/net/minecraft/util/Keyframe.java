package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Keyframe<T>(int ticks, T value) {
   public static <T> Codec<net.minecraft.util.Keyframe<T>> codec(Codec<T> $$0) {
      return RecordCodecBuilder.create(
         $$1 -> $$1.group(
               net.minecraft.util.ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks").forGetter(net.minecraft.util.Keyframe::ticks),
               $$0.fieldOf("value").forGetter(net.minecraft.util.Keyframe::value)
            )
            .apply($$1, net.minecraft.util.Keyframe::new)
      );
   }
}
