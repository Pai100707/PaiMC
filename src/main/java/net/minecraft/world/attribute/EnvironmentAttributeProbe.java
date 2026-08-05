package net.minecraft.world.attribute;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

public class EnvironmentAttributeProbe {
   private final Map<net.minecraft.world.attribute.EnvironmentAttribute<?>, net.minecraft.world.attribute.EnvironmentAttributeProbe.ValueProbe<?>> valueProbes = new Reference2ObjectOpenHashMap();
   private final Function<net.minecraft.world.attribute.EnvironmentAttribute<?>, net.minecraft.world.attribute.EnvironmentAttributeProbe.ValueProbe<?>> valueProbeFactory = $$0 -> new net.minecraft.world.attribute.EnvironmentAttributeProbe.ValueProbe<>(
      $$0
   );
   
   Level level;
   
   Vec3 position;
   final net.minecraft.world.attribute.SpatialAttributeInterpolator biomeInterpolator = new net.minecraft.world.attribute.SpatialAttributeInterpolator();

   public void reset() {
      this.level = null;
      this.position = null;
      this.biomeInterpolator.clear();
      this.valueProbes.clear();
   }

   public void tick(Level $$0, Vec3 $$1) {
      this.level = $$0;
      this.position = $$1;
      this.valueProbes.values().removeIf(net.minecraft.world.attribute.EnvironmentAttributeProbe.ValueProbe::tick);
      this.biomeInterpolator.clear();
      net.minecraft.world.attribute.GaussianSampler.sample(
         $$1.scale(0.25),
         $$0.getBiomeManager()::getNoiseBiomeAtQuart,
         ($$0x, $$1x) -> this.biomeInterpolator.accumulate($$0x, ((Biome)$$1x.value()).getAttributes())
      );
   }

   public <Value> Value getValue(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, float $$1) {
      net.minecraft.world.attribute.EnvironmentAttributeProbe.ValueProbe<Value> $$2 = (net.minecraft.world.attribute.EnvironmentAttributeProbe.ValueProbe<Value>)this.valueProbes
         .computeIfAbsent($$0, this.valueProbeFactory);
      return $$2.get($$0, $$1);
   }

   class ValueProbe<Value> {
      private Value lastValue;
      
      private Value newValue;

      public ValueProbe(final net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0) {
         Value $$1 = this.getValueFromLevel($$0);
         this.lastValue = $$1;
         this.newValue = $$1;
      }

      private Value getValueFromLevel(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0) {
         return EnvironmentAttributeProbe.this.level != null && EnvironmentAttributeProbe.this.position != null
            ? EnvironmentAttributeProbe.this.level
               .environmentAttributes()
               .getValue($$0, EnvironmentAttributeProbe.this.position, EnvironmentAttributeProbe.this.biomeInterpolator)
            : $$0.defaultValue();
      }

      public boolean tick() {
         if (this.newValue == null) {
            return true;
         } else {
            this.lastValue = this.newValue;
            this.newValue = null;
            return false;
         }
      }

      public Value get(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, float $$1) {
         if (this.newValue == null) {
            this.newValue = this.getValueFromLevel($$0);
         }

         return $$0.type().partialTickLerp().apply($$1, this.lastValue, this.newValue);
      }
   }
}
