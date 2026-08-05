/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.GaussianSampler;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EnvironmentAttributeProbe {
    private final Map<EnvironmentAttribute<?>, ValueProbe<?>> valueProbes = new Reference2ObjectOpenHashMap();
    private final Function<EnvironmentAttribute<?>, ValueProbe<?>> valueProbeFactory = $$0 -> new ValueProbe($$0);
    @Nullable Level level;
    @Nullable Vec3 position;
    final SpatialAttributeInterpolator biomeInterpolator = new SpatialAttributeInterpolator();

    public void reset() {
        this.level = null;
        this.position = null;
        this.biomeInterpolator.clear();
        this.valueProbes.clear();
    }

    public void tick(Level $$02, Vec3 $$12) {
        this.level = $$02;
        this.position = $$12;
        this.valueProbes.values().removeIf(ValueProbe::tick);
        this.biomeInterpolator.clear();
        GaussianSampler.sample($$12.scale(0.25), $$02.getBiomeManager()::getNoiseBiomeAtQuart, ($$0, $$1) -> this.biomeInterpolator.accumulate($$0, ((Biome)$$1.value()).getAttributes()));
    }

    public <Value> Value getValue(EnvironmentAttribute<Value> $$0, float $$1) {
        ValueProbe<?> $$2 = this.valueProbes.computeIfAbsent($$0, this.valueProbeFactory);
        return (Value)$$2.get($$0, $$1);
    }

    class ValueProbe<Value> {
        private Value lastValue;
        private @Nullable Value newValue;

        public ValueProbe(EnvironmentAttribute<Value> $$0) {
            Value $$1 = this.getValueFromLevel($$0);
            this.lastValue = $$1;
            this.newValue = $$1;
        }

        private Value getValueFromLevel(EnvironmentAttribute<Value> $$0) {
            if (EnvironmentAttributeProbe.this.level == null || EnvironmentAttributeProbe.this.position == null) {
                return $$0.defaultValue();
            }
            return EnvironmentAttributeProbe.this.level.environmentAttributes().getValue($$0, EnvironmentAttributeProbe.this.position, EnvironmentAttributeProbe.this.biomeInterpolator);
        }

        public boolean tick() {
            if (this.newValue == null) {
                return true;
            }
            this.lastValue = this.newValue;
            this.newValue = null;
            return false;
        }

        public Value get(EnvironmentAttribute<Value> $$0, float $$1) {
            if (this.newValue == null) {
                this.newValue = this.getValueFromLevel($$0);
            }
            return $$0.type().partialTickLerp().apply($$1, this.lastValue, this.newValue);
        }
    }
}

