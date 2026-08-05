package net.minecraft.world.attribute;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.timeline.Timeline;

public class EnvironmentAttributeSystem implements net.minecraft.world.attribute.EnvironmentAttributeReader {
   private final Map<net.minecraft.world.attribute.EnvironmentAttribute<?>, net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<?>> attributeSamplers = new Reference2ObjectOpenHashMap();

   EnvironmentAttributeSystem(Map<net.minecraft.world.attribute.EnvironmentAttribute<?>, List<net.minecraft.world.attribute.EnvironmentAttributeLayer<?>>> $$0) {
      $$0.forEach(
         ($$0x, $$1) -> this.attributeSamplers
            .put($$0x, this.bakeLayerSampler($$0x, (List<? extends net.minecraft.world.attribute.EnvironmentAttributeLayer<?>>)$$1))
      );
   }

   private <Value> net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<Value> bakeLayerSampler(
      net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, List<? extends net.minecraft.world.attribute.EnvironmentAttributeLayer<?>> $$1
   ) {
      List<net.minecraft.world.attribute.EnvironmentAttributeLayer<Value>> $$2 = new ArrayList<>(
         (Collection<? extends net.minecraft.world.attribute.EnvironmentAttributeLayer<Value>>)$$1
      );
      Value $$3 = $$0.defaultValue();

      while (!$$2.isEmpty()) {
         if (!($$2.getFirst() instanceof net.minecraft.world.attribute.EnvironmentAttributeLayer.Constant<Value> $$4)) {
            break;
         }

         $$3 = $$4.applyConstant($$3);
         $$2.removeFirst();
      }

      boolean $$5 = $$2.stream().anyMatch($$0x -> $$0x instanceof net.minecraft.world.attribute.EnvironmentAttributeLayer.Positional);
      return new net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<>($$0, $$3, List.copyOf($$2), $$5);
   }

   public static net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder builder() {
      return new net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder();
   }

   static void addDefaultLayers(net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder $$0, Level $$1) {
      RegistryAccess $$2 = $$1.registryAccess();
      BiomeManager $$3 = $$1.getBiomeManager();
      LongSupplier $$4 = $$1::getDayTime;
      addDimensionLayer($$0, $$1.dimensionType());
      addBiomeLayer($$0, $$2.lookupOrThrow(Registries.BIOME), $$3);
      $$1.dimensionType().timelines().forEach($$2x -> $$0.addTimelineLayer($$2x, $$4));
      if ($$1.canHaveWeather()) {
         net.minecraft.world.attribute.WeatherAttributes.addBuiltinLayers($$0, net.minecraft.world.attribute.WeatherAttributes.WeatherAccess.from($$1));
      }
   }

   private static void addDimensionLayer(net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder $$0, DimensionType $$1) {
      $$0.addConstantLayer($$1.attributes());
   }

   private static void addBiomeLayer(net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder $$0, HolderLookup<Biome> $$1, BiomeManager $$2) {
      Stream<net.minecraft.world.attribute.EnvironmentAttribute<?>> $$3 = $$1.listElements()
         .flatMap($$0x -> ((Biome)$$0x.value()).getAttributes().keySet().stream())
         .distinct();
      $$3.forEach($$2x -> addBiomeLayerForAttribute($$0, $$2x, $$2));
   }

   private static <Value> void addBiomeLayerForAttribute(
      net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder $$0, net.minecraft.world.attribute.EnvironmentAttribute<Value> $$1, BiomeManager $$2
   ) {
      $$0.addPositionalLayer($$1, ($$2x, $$3, $$4) -> {
         if ($$4 != null && $$1.isSpatiallyInterpolated()) {
            return $$4.applyAttributeLayer($$1, (Value)$$2x);
         } else {
            Holder<Biome> $$5 = $$2.getNoiseBiomeAtPosition($$3.x, $$3.y, $$3.z);
            return ((Biome)$$5.value()).getAttributes().applyModifier($$1, (Value)$$2x);
         }
      });
   }

   public void invalidateTickCache() {
      this.attributeSamplers.values().forEach(net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler::invalidateTickCache);
   }

   
   private <Value> net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<Value> getValueSampler(
      net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0
   ) {
      return (net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<Value>)this.attributeSamplers.get($$0);
   }

   @Override
   public <Value> Value getDimensionValue(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0) {
      if (SharedConstants.IS_RUNNING_IN_IDE && $$0.isPositional()) {
         throw new IllegalStateException("Position must always be provided for positional attribute " + $$0);
      } else {
         net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<Value> $$1 = this.getValueSampler($$0);
         return $$1 == null ? $$0.defaultValue() : $$1.getDimensionValue();
      }
   }

   @Override
   public <Value> Value getValue(
      net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, Vec3 $$1, net.minecraft.world.attribute.SpatialAttributeInterpolator $$2
   ) {
      net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<Value> $$3 = this.getValueSampler($$0);
      return $$3 == null ? $$0.defaultValue() : $$3.getValue($$1, $$2);
   }

   @VisibleForTesting
   <Value> Value getConstantBaseValue(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0) {
      net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<Value> $$1 = this.getValueSampler($$0);
      return $$1 != null ? $$1.baseValue : $$0.defaultValue();
   }

   @VisibleForTesting
   boolean isAffectedByPosition(net.minecraft.world.attribute.EnvironmentAttribute<?> $$0) {
      net.minecraft.world.attribute.EnvironmentAttributeSystem.ValueSampler<?> $$1 = this.getValueSampler($$0);
      return $$1 != null && $$1.isAffectedByPosition;
   }

   public static class Builder {
      private final Map<net.minecraft.world.attribute.EnvironmentAttribute<?>, List<net.minecraft.world.attribute.EnvironmentAttributeLayer<?>>> layersByAttribute = new HashMap<>();

      Builder() {
      }

      public net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addDefaultLayers(Level $$0) {
         net.minecraft.world.attribute.EnvironmentAttributeSystem.addDefaultLayers(this, $$0);
         return this;
      }

      public net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addConstantLayer(net.minecraft.world.attribute.EnvironmentAttributeMap $$0) {
         for (net.minecraft.world.attribute.EnvironmentAttribute<?> $$1 : $$0.keySet()) {
            this.addConstantEntry($$1, $$0);
         }

         return this;
      }

      private <Value> net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addConstantEntry(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, net.minecraft.world.attribute.EnvironmentAttributeMap $$1
      ) {
         net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?> $$2 = $$1.get($$0);
         if ($$2 == null) {
            throw new IllegalArgumentException("Missing attribute " + $$0);
         } else {
            return this.addConstantLayer($$0, $$2::applyModifier);
         }
      }

      public <Value> net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addConstantLayer(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, net.minecraft.world.attribute.EnvironmentAttributeLayer.Constant<Value> $$1
      ) {
         return this.addLayer($$0, $$1);
      }

      public <Value> net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addTimeBasedLayer(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, net.minecraft.world.attribute.EnvironmentAttributeLayer.TimeBased<Value> $$1
      ) {
         return this.addLayer($$0, $$1);
      }

      public <Value> net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addPositionalLayer(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, net.minecraft.world.attribute.EnvironmentAttributeLayer.Positional<Value> $$1
      ) {
         return this.addLayer($$0, $$1);
      }

      private <Value> net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addLayer(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, net.minecraft.world.attribute.EnvironmentAttributeLayer<Value> $$1
      ) {
         this.layersByAttribute.computeIfAbsent($$0, $$0x -> new ArrayList<>()).add($$1);
         return this;
      }

      public net.minecraft.world.attribute.EnvironmentAttributeSystem.Builder addTimelineLayer(Holder<Timeline> $$0, LongSupplier $$1) {
         for (net.minecraft.world.attribute.EnvironmentAttribute<?> $$2 : ((Timeline)$$0.value()).attributes()) {
            this.addTimelineLayerForAttribute($$0, $$2, $$1);
         }

         return this;
      }

      private <Value> void addTimelineLayerForAttribute(Holder<Timeline> $$0, net.minecraft.world.attribute.EnvironmentAttribute<Value> $$1, LongSupplier $$2) {
         this.addTimeBasedLayer($$1, ((Timeline)$$0.value()).createTrackSampler($$1, $$2));
      }

      public net.minecraft.world.attribute.EnvironmentAttributeSystem build() {
         return new net.minecraft.world.attribute.EnvironmentAttributeSystem(this.layersByAttribute);
      }
   }

   static class ValueSampler<Value> {
      private final net.minecraft.world.attribute.EnvironmentAttribute<Value> attribute;
      final Value baseValue;
      private final List<net.minecraft.world.attribute.EnvironmentAttributeLayer<Value>> layers;
      final boolean isAffectedByPosition;
      
      private Value cachedTickValue;
      private int cacheTickId;

      ValueSampler(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0,
         Value $$1,
         List<net.minecraft.world.attribute.EnvironmentAttributeLayer<Value>> $$2,
         boolean $$3
      ) {
         this.attribute = $$0;
         this.baseValue = $$1;
         this.layers = $$2;
         this.isAffectedByPosition = $$3;
      }

      public void invalidateTickCache() {
         this.cachedTickValue = null;
         this.cacheTickId++;
      }

      public Value getDimensionValue() {
         if (this.cachedTickValue != null) {
            return this.cachedTickValue;
         } else {
            Value $$0 = this.computeValueNotPositional();
            this.cachedTickValue = $$0;
            return $$0;
         }
      }

      public Value getValue(Vec3 $$0, net.minecraft.world.attribute.SpatialAttributeInterpolator $$1) {
         return !this.isAffectedByPosition ? this.getDimensionValue() : this.computeValuePositional($$0, $$1);
      }

      private Value computeValuePositional(Vec3 $$0, net.minecraft.world.attribute.SpatialAttributeInterpolator $$1) {
         Value $$2 = this.baseValue;

         for (net.minecraft.world.attribute.EnvironmentAttributeLayer<Value> $$3 : this.layers) {
            $$2 = (Value)(switch ($$3) {
               case net.minecraft.world.attribute.EnvironmentAttributeLayer.Constant<Value> $$4 -> (Object)$$4.applyConstant($$2);
               case net.minecraft.world.attribute.EnvironmentAttributeLayer.TimeBased<Value> $$5 -> (Object)$$5.applyTimeBased($$2, this.cacheTickId);
               case net.minecraft.world.attribute.EnvironmentAttributeLayer.Positional<Value> $$6 -> (Object)$$6.applyPositional(
                  $$2, Objects.requireNonNull($$0), $$1
               );
               default -> throw new MatchException(null, null);
            });
         }

         return this.attribute.sanitizeValue($$2);
      }

      private Value computeValueNotPositional() {
         Value $$0 = this.baseValue;

         for (net.minecraft.world.attribute.EnvironmentAttributeLayer<Value> $$1 : this.layers) {
            $$0 = (Value)(switch ($$1) {
               case net.minecraft.world.attribute.EnvironmentAttributeLayer.Constant<Value> $$2 -> (Object)$$2.applyConstant($$0);
               case net.minecraft.world.attribute.EnvironmentAttributeLayer.TimeBased<Value> $$3 -> (Object)$$3.applyTimeBased($$0, this.cacheTickId);
               case net.minecraft.world.attribute.EnvironmentAttributeLayer.Positional<Value> $$4 -> (Object)$$0;
               default -> throw new MatchException(null, null);
            });
         }

         return this.attribute.sanitizeValue($$0);
      }
   }
}
