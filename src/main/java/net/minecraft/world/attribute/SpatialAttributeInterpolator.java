package net.minecraft.world.attribute;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap.Entry;
import java.util.Objects;

public class SpatialAttributeInterpolator {
   private final Reference2DoubleArrayMap<net.minecraft.world.attribute.EnvironmentAttributeMap> weightsBySource = new Reference2DoubleArrayMap();

   public void clear() {
      this.weightsBySource.clear();
   }

   public net.minecraft.world.attribute.SpatialAttributeInterpolator accumulate(double $$0, net.minecraft.world.attribute.EnvironmentAttributeMap $$1) {
      this.weightsBySource.mergeDouble($$1, $$0, Double::sum);
      return this;
   }

   public <Value> Value applyAttributeLayer(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, Value $$1) {
      if (this.weightsBySource.isEmpty()) {
         return $$1;
      } else if (this.weightsBySource.size() == 1) {
         net.minecraft.world.attribute.EnvironmentAttributeMap $$2 = (net.minecraft.world.attribute.EnvironmentAttributeMap)this.weightsBySource
            .keySet()
            .iterator()
            .next();
         return $$2.applyModifier($$0, $$1);
      } else {
         net.minecraft.world.attribute.LerpFunction<Value> $$3 = $$0.type().spatialLerp();
         Value $$4 = null;
         double $$5 = 0.0;
         ObjectIterator var7 = Reference2DoubleMaps.fastIterable(this.weightsBySource).iterator();

         while (var7.hasNext()) {
            Entry<net.minecraft.world.attribute.EnvironmentAttributeMap> $$6 = (Entry<net.minecraft.world.attribute.EnvironmentAttributeMap>)var7.next();
            net.minecraft.world.attribute.EnvironmentAttributeMap $$7 = (net.minecraft.world.attribute.EnvironmentAttributeMap)$$6.getKey();
            double $$8 = $$6.getDoubleValue();
            Value $$9 = $$7.applyModifier($$0, $$1);
            $$5 += $$8;
            if ($$4 == null) {
               $$4 = $$9;
            } else {
               float $$10 = (float)($$8 / $$5);
               $$4 = $$3.apply($$10, $$4, $$9);
            }
         }

         return Objects.requireNonNull($$4);
      }
   }
}
