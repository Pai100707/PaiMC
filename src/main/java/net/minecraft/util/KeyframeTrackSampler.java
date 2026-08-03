package net.minecraft.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.attribute.LerpFunction;

public class KeyframeTrackSampler<T> {
   private final Optional<Integer> periodTicks;
   private final LerpFunction<T> lerp;
   private final List<net.minecraft.util.KeyframeTrackSampler.Segment<T>> segments;

   KeyframeTrackSampler(net.minecraft.util.KeyframeTrack<T> $$0, Optional<Integer> $$1, LerpFunction<T> $$2) {
      this.periodTicks = $$1;
      this.lerp = $$2;
      this.segments = bakeSegments($$0, $$1);
   }

   private static <T> List<net.minecraft.util.KeyframeTrackSampler.Segment<T>> bakeSegments(net.minecraft.util.KeyframeTrack<T> $$0, Optional<Integer> $$1) {
      List<net.minecraft.util.Keyframe<T>> $$2 = $$0.keyframes();
      if ($$2.size() == 1) {
         T $$3 = $$2.getFirst().value();
         return List.of(new net.minecraft.util.KeyframeTrackSampler.Segment<>(net.minecraft.util.EasingType.CONSTANT, $$3, 0, $$3, 0));
      } else {
         List<net.minecraft.util.KeyframeTrackSampler.Segment<T>> $$4 = new ArrayList<>();
         if ($$1.isPresent()) {
            net.minecraft.util.Keyframe<T> $$5 = $$2.getFirst();
            net.minecraft.util.Keyframe<T> $$6 = $$2.getLast();
            $$4.add(new net.minecraft.util.KeyframeTrackSampler.Segment<>($$0, $$6, $$6.ticks() - $$1.get(), $$5, $$5.ticks()));
            addSegmentsFromKeyframes($$0, $$2, $$4);
            $$4.add(new net.minecraft.util.KeyframeTrackSampler.Segment<>($$0, $$6, $$6.ticks(), $$5, $$5.ticks() + $$1.get()));
         } else {
            addSegmentsFromKeyframes($$0, $$2, $$4);
         }

         return List.copyOf($$4);
      }
   }

   private static <T> void addSegmentsFromKeyframes(
      net.minecraft.util.KeyframeTrack<T> $$0, List<net.minecraft.util.Keyframe<T>> $$1, List<net.minecraft.util.KeyframeTrackSampler.Segment<T>> $$2
   ) {
      for (int $$3 = 0; $$3 < $$1.size() - 1; $$3++) {
         net.minecraft.util.Keyframe<T> $$4 = $$1.get($$3);
         net.minecraft.util.Keyframe<T> $$5 = $$1.get($$3 + 1);
         $$2.add(new net.minecraft.util.KeyframeTrackSampler.Segment<>($$0, $$4, $$4.ticks(), $$5, $$5.ticks()));
      }
   }

   public T sample(long $$0) {
      long $$1 = this.loopTicks($$0);
      net.minecraft.util.KeyframeTrackSampler.Segment<T> $$2 = this.getSegmentAt($$1);
      if ($$1 <= $$2.fromTicks) {
         return $$2.fromValue;
      } else if ($$1 >= $$2.toTicks) {
         return $$2.toValue;
      } else {
         float $$3 = (float)($$1 - $$2.fromTicks) / ($$2.toTicks - $$2.fromTicks);
         float $$4 = $$2.easing.apply($$3);
         return (T)this.lerp.apply($$4, $$2.fromValue, $$2.toValue);
      }
   }

   private net.minecraft.util.KeyframeTrackSampler.Segment<T> getSegmentAt(long $$0) {
      for (net.minecraft.util.KeyframeTrackSampler.Segment<T> $$1 : this.segments) {
         if ($$0 < $$1.toTicks) {
            return $$1;
         }
      }

      return this.segments.getLast();
   }

   private long loopTicks(long $$0) {
      return this.periodTicks.isPresent() ? Math.floorMod($$0, this.periodTicks.get()) : $$0;
   }

   record Segment<T>(net.minecraft.util.EasingType easing, T fromValue, int fromTicks, T toValue, int toTicks) {

      public Segment(net.minecraft.util.KeyframeTrack<T> $$0, net.minecraft.util.Keyframe<T> $$1, int $$2, net.minecraft.util.Keyframe<T> $$3, int $$4) {
         this($$0.easingType(), $$1.value(), $$2, $$3.value(), $$4);
      }
   }
}
