package net.minecraft.util;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.attribute.LerpFunction;

public record KeyframeTrack<T>(List<net.minecraft.util.Keyframe<T>> keyframes, net.minecraft.util.EasingType easingType) {
   public KeyframeTrack(List<net.minecraft.util.Keyframe<T>> keyframes, net.minecraft.util.EasingType easingType) {
      if (keyframes.isEmpty()) {
         throw new IllegalArgumentException("Track has no keyframes");
      } else {
         this.keyframes = keyframes;
         this.easingType = easingType;
      }
   }

   public static <T> MapCodec<net.minecraft.util.KeyframeTrack<T>> mapCodec(Codec<T> $$0) {
      Codec<List<net.minecraft.util.Keyframe<T>>> $$1 = net.minecraft.util.Keyframe.codec($$0)
         .listOf()
         .validate(net.minecraft.util.KeyframeTrack::validateKeyframes);
      return RecordCodecBuilder.mapCodec(
         $$1x -> $$1x.group(
               $$1.fieldOf("keyframes").forGetter(net.minecraft.util.KeyframeTrack::keyframes),
               net.minecraft.util.EasingType.CODEC
                  .optionalFieldOf("ease", net.minecraft.util.EasingType.LINEAR)
                  .forGetter(net.minecraft.util.KeyframeTrack::easingType)
            )
            .apply($$1x, net.minecraft.util.KeyframeTrack::new)
      );
   }

   static <T> DataResult<List<net.minecraft.util.Keyframe<T>>> validateKeyframes(List<net.minecraft.util.Keyframe<T>> $$0) {
      if ($$0.isEmpty()) {
         return DataResult.error(() -> "Keyframes must not be empty");
      } else if (!Comparators.isInOrder($$0, Comparator.comparingInt(net.minecraft.util.Keyframe::ticks))) {
         return DataResult.error(() -> "Keyframes must be ordered by ticks field");
      } else {
         if ($$0.size() > 1) {
            int $$1 = 0;
            int $$2 = $$0.getLast().ticks();

            for (net.minecraft.util.Keyframe<T> $$3 : $$0) {
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
   }

   public static DataResult<net.minecraft.util.KeyframeTrack<?>> validatePeriod(net.minecraft.util.KeyframeTrack<?> $$0, int $$1) {
      for (net.minecraft.util.Keyframe<?> $$2 : $$0.keyframes()) {
         int $$3 = $$2.ticks();
         if ($$3 < 0 || $$3 > $$1) {
            return DataResult.error(() -> "Keyframe at tick " + $$2.ticks() + " must be in range [0; " + $$1 + "]");
         }
      }

      return DataResult.success($$0);
   }

   public net.minecraft.util.KeyframeTrackSampler<T> bakeSampler(Optional<Integer> $$0, LerpFunction<T> $$1) {
      return new net.minecraft.util.KeyframeTrackSampler<>(this, $$0, $$1);
   }

   public static class Builder<T> {
      private final com.google.common.collect.ImmutableList.Builder<net.minecraft.util.Keyframe<T>> keyframes = ImmutableList.builder();
      private net.minecraft.util.EasingType easing = net.minecraft.util.EasingType.LINEAR;

      public net.minecraft.util.KeyframeTrack.Builder<T> addKeyframe(int $$0, T $$1) {
         this.keyframes.add(new net.minecraft.util.Keyframe($$0, $$1));
         return this;
      }

      public net.minecraft.util.KeyframeTrack.Builder<T> setEasing(net.minecraft.util.EasingType $$0) {
         this.easing = $$0;
         return this;
      }

      public net.minecraft.util.KeyframeTrack<T> build() {
         List<net.minecraft.util.Keyframe<T>> $$0 = (List<net.minecraft.util.Keyframe<T>>)net.minecraft.util.KeyframeTrack.validateKeyframes(
               this.keyframes.build()
            )
            .getOrThrow();
         return new net.minecraft.util.KeyframeTrack<>($$0, this.easing);
      }
   }
}
