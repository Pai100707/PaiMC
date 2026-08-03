package net.minecraft.world.timeline;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.LongSupplier;
import net.minecraft.util.KeyframeTrack;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public record AttributeTrack<Value, Argument>(AttributeModifier<Value, Argument> modifier, KeyframeTrack<Argument> argumentTrack) {
   public static <Value> Codec<net.minecraft.world.timeline.AttributeTrack<Value, ?>> createCodec(EnvironmentAttribute<Value> $$0) {
      MapCodec<AttributeModifier<Value, ?>> $$1 = $$0.type().modifierCodec().optionalFieldOf("modifier", AttributeModifier.override());
      return $$1.dispatch(net.minecraft.world.timeline.AttributeTrack::modifier, Util.memoize($$1x -> createCodecWithModifier($$0, $$1x)));
   }

   private static <Value, Argument> MapCodec<net.minecraft.world.timeline.AttributeTrack<Value, Argument>> createCodecWithModifier(
      EnvironmentAttribute<Value> $$0, AttributeModifier<Value, Argument> $$1
   ) {
      return KeyframeTrack.mapCodec($$1.argumentCodec($$0))
         .xmap($$1x -> new net.minecraft.world.timeline.AttributeTrack($$1, $$1x), net.minecraft.world.timeline.AttributeTrack::argumentTrack);
   }

   public net.minecraft.world.timeline.AttributeTrackSampler<Value, Argument> bakeSampler(
      EnvironmentAttribute<Value> $$0, Optional<Integer> $$1, LongSupplier $$2
   ) {
      return new net.minecraft.world.timeline.AttributeTrackSampler<>($$1, this.modifier, this.argumentTrack, this.modifier.argumentKeyframeLerp($$0), $$2);
   }

   public static DataResult<net.minecraft.world.timeline.AttributeTrack<?, ?>> validatePeriod(net.minecraft.world.timeline.AttributeTrack<?, ?> $$0, int $$1) {
      return KeyframeTrack.validatePeriod($$0.argumentTrack(), $$1).map($$1x -> $$0);
   }
}
