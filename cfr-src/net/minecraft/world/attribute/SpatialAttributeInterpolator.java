/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2DoubleMaps
 */
package net.minecraft.world.attribute;

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMaps;
import java.util.Objects;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.LerpFunction;

public class SpatialAttributeInterpolator {
    private final Reference2DoubleArrayMap<EnvironmentAttributeMap> weightsBySource = new Reference2DoubleArrayMap();

    public void clear() {
        this.weightsBySource.clear();
    }

    public SpatialAttributeInterpolator accumulate(double $$0, EnvironmentAttributeMap $$1) {
        this.weightsBySource.mergeDouble((Object)$$1, $$0, Double::sum);
        return this;
    }

    public <Value> Value applyAttributeLayer(EnvironmentAttribute<Value> $$0, Value $$1) {
        if (this.weightsBySource.isEmpty()) {
            return $$1;
        }
        if (this.weightsBySource.size() == 1) {
            EnvironmentAttributeMap $$2 = (EnvironmentAttributeMap)this.weightsBySource.keySet().iterator().next();
            return $$2.applyModifier($$0, $$1);
        }
        LerpFunction<Value> $$3 = $$0.type().spatialLerp();
        Object $$4 = null;
        double $$5 = 0.0;
        for (Reference2DoubleMap.Entry $$6 : Reference2DoubleMaps.fastIterable(this.weightsBySource)) {
            EnvironmentAttributeMap $$7 = (EnvironmentAttributeMap)$$6.getKey();
            double $$8 = $$6.getDoubleValue();
            Value $$9 = $$7.applyModifier($$0, $$1);
            $$5 += $$8;
            if ($$4 == null) {
                $$4 = $$9;
                continue;
            }
            float $$10 = (float)($$8 / $$5);
            $$4 = $$3.apply($$10, $$4, $$9);
        }
        return Objects.requireNonNull($$4);
    }
}

