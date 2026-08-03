package net.minecraft.world.timeline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.level.Level;

public class Timeline {
   public static final Codec<Holder<net.minecraft.world.timeline.Timeline>> CODEC = RegistryFixedCodec.create(Registries.TIMELINE);
   private static final Codec<Map<EnvironmentAttribute<?>, net.minecraft.world.timeline.AttributeTrack<?, ?>>> TRACKS_CODEC = Codec.dispatchedMap(
      EnvironmentAttributes.CODEC, Util.memoize(net.minecraft.world.timeline.AttributeTrack::createCodec)
   );
   public static final Codec<net.minecraft.world.timeline.Timeline> DIRECT_CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               ExtraCodecs.POSITIVE_INT.optionalFieldOf("period_ticks").forGetter($$0x -> $$0x.periodTicks),
               TRACKS_CODEC.optionalFieldOf("tracks", Map.of()).forGetter($$0x -> $$0x.tracks)
            )
            .apply($$0, net.minecraft.world.timeline.Timeline::new)
      )
      .validate(net.minecraft.world.timeline.Timeline::validateInternal);
   public static final Codec<net.minecraft.world.timeline.Timeline> NETWORK_CODEC = DIRECT_CODEC.xmap(
      net.minecraft.world.timeline.Timeline::filterSyncableTracks, net.minecraft.world.timeline.Timeline::filterSyncableTracks
   );
   private final Optional<Integer> periodTicks;
   private final Map<EnvironmentAttribute<?>, net.minecraft.world.timeline.AttributeTrack<?, ?>> tracks;

   private static net.minecraft.world.timeline.Timeline filterSyncableTracks(net.minecraft.world.timeline.Timeline $$0) {
      Map<EnvironmentAttribute<?>, net.minecraft.world.timeline.AttributeTrack<?, ?>> $$1 = Map.copyOf(
         Maps.filterKeys($$0.tracks, EnvironmentAttribute::isSyncable)
      );
      return new net.minecraft.world.timeline.Timeline($$0.periodTicks, $$1);
   }

   Timeline(Optional<Integer> $$0, Map<EnvironmentAttribute<?>, net.minecraft.world.timeline.AttributeTrack<?, ?>> $$1) {
      this.periodTicks = $$0;
      this.tracks = $$1;
   }

   private static DataResult<net.minecraft.world.timeline.Timeline> validateInternal(net.minecraft.world.timeline.Timeline $$0) {
      if ($$0.periodTicks.isEmpty()) {
         return DataResult.success($$0);
      } else {
         int $$1 = $$0.periodTicks.get();
         DataResult<net.minecraft.world.timeline.Timeline> $$2 = DataResult.success($$0);

         for (net.minecraft.world.timeline.AttributeTrack<?, ?> $$3 : $$0.tracks.values()) {
            $$2 = $$2.apply2stable(($$0x, $$1x) -> $$0x, net.minecraft.world.timeline.AttributeTrack.validatePeriod($$3, $$1));
         }

         return $$2;
      }
   }

   public static net.minecraft.world.timeline.Timeline.Builder builder() {
      return new net.minecraft.world.timeline.Timeline.Builder();
   }

   public long getCurrentTicks(Level $$0) {
      long $$1 = this.getTotalTicks($$0);
      return this.periodTicks.isEmpty() ? $$1 : $$1 % this.periodTicks.get().intValue();
   }

   public long getTotalTicks(Level $$0) {
      return $$0.getDayTime();
   }

   public Optional<Integer> periodTicks() {
      return this.periodTicks;
   }

   public Set<EnvironmentAttribute<?>> attributes() {
      return this.tracks.keySet();
   }

   public <Value> net.minecraft.world.timeline.AttributeTrackSampler<Value, ?> createTrackSampler(EnvironmentAttribute<Value> $$0, LongSupplier $$1) {
      net.minecraft.world.timeline.AttributeTrack<Value, ?> $$2 = (net.minecraft.world.timeline.AttributeTrack<Value, ?>)this.tracks.get($$0);
      if ($$2 == null) {
         throw new IllegalStateException("Timeline has no track for " + $$0);
      } else {
         return $$2.bakeSampler($$0, this.periodTicks, $$1);
      }
   }

   public static class Builder {
      private Optional<Integer> periodTicks = Optional.empty();
      private final com.google.common.collect.ImmutableMap.Builder<EnvironmentAttribute<?>, net.minecraft.world.timeline.AttributeTrack<?, ?>> tracks = ImmutableMap.builder();

      Builder() {
      }

      public net.minecraft.world.timeline.Timeline.Builder setPeriodTicks(int $$0) {
         this.periodTicks = Optional.of($$0);
         return this;
      }

      public <Value, Argument> net.minecraft.world.timeline.Timeline.Builder addModifierTrack(
         EnvironmentAttribute<Value> $$0, AttributeModifier<Value, Argument> $$1, Consumer<net.minecraft.util.KeyframeTrack.Builder<Argument>> $$2
      ) {
         $$0.type().checkAllowedModifier($$1);
         net.minecraft.util.KeyframeTrack.Builder<Argument> $$3 = new net.minecraft.util.KeyframeTrack.Builder();
         $$2.accept($$3);
         this.tracks.put($$0, new net.minecraft.world.timeline.AttributeTrack($$1, $$3.build()));
         return this;
      }

      public <Value> net.minecraft.world.timeline.Timeline.Builder addTrack(
         EnvironmentAttribute<Value> $$0, Consumer<net.minecraft.util.KeyframeTrack.Builder<Value>> $$1
      ) {
         return this.addModifierTrack($$0, AttributeModifier.override(), $$1);
      }

      public net.minecraft.world.timeline.Timeline build() {
         return new net.minecraft.world.timeline.Timeline(this.periodTicks, this.tracks.build());
      }
   }
}
