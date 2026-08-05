/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
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
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeLayer;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.attribute.WeatherAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.timeline.Timeline;
import org.jspecify.annotations.Nullable;

public class EnvironmentAttributeSystem
implements EnvironmentAttributeReader {
    private final Map<EnvironmentAttribute<?>, ValueSampler<?>> attributeSamplers = new Reference2ObjectOpenHashMap();

    EnvironmentAttributeSystem(Map<EnvironmentAttribute<?>, List<EnvironmentAttributeLayer<?>>> $$02) {
        $$02.forEach(($$0, $$1) -> this.attributeSamplers.put((EnvironmentAttribute<?>)$$0, this.bakeLayerSampler((EnvironmentAttribute)$$0, (List<? extends EnvironmentAttributeLayer<?>>)$$1)));
    }

    private <Value> ValueSampler<Value> bakeLayerSampler(EnvironmentAttribute<Value> $$02, List<? extends EnvironmentAttributeLayer<?>> $$1) {
        Object e;
        ArrayList $$2 = new ArrayList($$1);
        Value $$3 = $$02.defaultValue();
        while (!$$2.isEmpty() && (e = $$2.getFirst()) instanceof EnvironmentAttributeLayer.Constant) {
            EnvironmentAttributeLayer.Constant $$4 = (EnvironmentAttributeLayer.Constant)e;
            $$3 = $$4.applyConstant($$3);
            $$2.removeFirst();
        }
        boolean $$5 = $$2.stream().anyMatch($$0 -> $$0 instanceof EnvironmentAttributeLayer.Positional);
        return new ValueSampler<Value>($$02, $$3, List.copyOf($$2), $$5);
    }

    public static Builder builder() {
        return new Builder();
    }

    static void addDefaultLayers(Builder $$0, Level $$1) {
        RegistryAccess $$22 = $$1.registryAccess();
        BiomeManager $$3 = $$1.getBiomeManager();
        LongSupplier $$4 = $$1::getDayTime;
        EnvironmentAttributeSystem.addDimensionLayer($$0, $$1.dimensionType());
        EnvironmentAttributeSystem.addBiomeLayer($$0, $$22.lookupOrThrow(Registries.BIOME), $$3);
        $$1.dimensionType().timelines().forEach($$2 -> $$0.addTimelineLayer((Holder<Timeline>)$$2, $$4));
        if ($$1.canHaveWeather()) {
            WeatherAttributes.addBuiltinLayers($$0, WeatherAttributes.WeatherAccess.from($$1));
        }
    }

    private static void addDimensionLayer(Builder $$0, DimensionType $$1) {
        $$0.addConstantLayer($$1.attributes());
    }

    private static void addBiomeLayer(Builder $$02, HolderLookup<Biome> $$1, BiomeManager $$22) {
        Stream $$3 = $$1.listElements().flatMap($$0 -> ((Biome)$$0.value()).getAttributes().keySet().stream()).distinct();
        $$3.forEach($$2 -> EnvironmentAttributeSystem.addBiomeLayerForAttribute($$02, $$2, $$22));
    }

    private static <Value> void addBiomeLayerForAttribute(Builder $$0, EnvironmentAttribute<Value> $$1, BiomeManager $$22) {
        $$0.addPositionalLayer($$1, ($$2, $$3, $$4) -> {
            if ($$4 != null && $$1.isSpatiallyInterpolated()) {
                return $$4.applyAttributeLayer($$1, $$2);
            }
            Holder<Biome> $$5 = $$22.getNoiseBiomeAtPosition($$3.x, $$3.y, $$3.z);
            return $$5.value().getAttributes().applyModifier($$1, $$2);
        });
    }

    public void invalidateTickCache() {
        this.attributeSamplers.values().forEach(ValueSampler::invalidateTickCache);
    }

    private <Value> @Nullable ValueSampler<Value> getValueSampler(EnvironmentAttribute<Value> $$0) {
        return this.attributeSamplers.get($$0);
    }

    @Override
    public <Value> Value getDimensionValue(EnvironmentAttribute<Value> $$0) {
        if (SharedConstants.IS_RUNNING_IN_IDE && $$0.isPositional()) {
            throw new IllegalStateException("Position must always be provided for positional attribute " + String.valueOf($$0));
        }
        ValueSampler<Value> $$1 = this.getValueSampler($$0);
        if ($$1 == null) {
            return $$0.defaultValue();
        }
        return $$1.getDimensionValue();
    }

    @Override
    public <Value> Value getValue(EnvironmentAttribute<Value> $$0, Vec3 $$1, @Nullable SpatialAttributeInterpolator $$2) {
        ValueSampler<Value> $$3 = this.getValueSampler($$0);
        if ($$3 == null) {
            return $$0.defaultValue();
        }
        return $$3.getValue($$1, $$2);
    }

    @VisibleForTesting
    <Value> Value getConstantBaseValue(EnvironmentAttribute<Value> $$0) {
        ValueSampler<Value> $$1 = this.getValueSampler($$0);
        return $$1 != null ? $$1.baseValue : $$0.defaultValue();
    }

    @VisibleForTesting
    boolean isAffectedByPosition(EnvironmentAttribute<?> $$0) {
        ValueSampler<?> $$1 = this.getValueSampler($$0);
        return $$1 != null && $$1.isAffectedByPosition;
    }

    static class ValueSampler<Value> {
        private final EnvironmentAttribute<Value> attribute;
        final Value baseValue;
        private final List<EnvironmentAttributeLayer<Value>> layers;
        final boolean isAffectedByPosition;
        private @Nullable Value cachedTickValue;
        private int cacheTickId;

        ValueSampler(EnvironmentAttribute<Value> $$0, Value $$1, List<EnvironmentAttributeLayer<Value>> $$2, boolean $$3) {
            this.attribute = $$0;
            this.baseValue = $$1;
            this.layers = $$2;
            this.isAffectedByPosition = $$3;
        }

        public void invalidateTickCache() {
            this.cachedTickValue = null;
            ++this.cacheTickId;
        }

        public Value getDimensionValue() {
            if (this.cachedTickValue != null) {
                return this.cachedTickValue;
            }
            Value $$0 = this.computeValueNotPositional();
            this.cachedTickValue = $$0;
            return $$0;
        }

        public Value getValue(Vec3 $$0, @Nullable SpatialAttributeInterpolator $$1) {
            if (!this.isAffectedByPosition) {
                return this.getDimensionValue();
            }
            return this.computeValuePositional($$0, $$1);
        }

        private Value computeValuePositional(Vec3 $$0, @Nullable SpatialAttributeInterpolator $$1) {
            Value $$2 = this.baseValue;
            for (EnvironmentAttributeLayer<Value> $$3 : this.layers) {
                EnvironmentAttributeLayer<Value> environmentAttributeLayer;
                Objects.requireNonNull($$3);
                int n = 0;
                $$2 = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EnvironmentAttributeLayer.Constant.class, EnvironmentAttributeLayer.TimeBased.class, EnvironmentAttributeLayer.Positional.class}, environmentAttributeLayer, n)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        EnvironmentAttributeLayer.Constant $$4 = (EnvironmentAttributeLayer.Constant)environmentAttributeLayer;
                        yield $$4.applyConstant($$2);
                    }
                    case 1 -> {
                        EnvironmentAttributeLayer.TimeBased $$5 = (EnvironmentAttributeLayer.TimeBased)environmentAttributeLayer;
                        yield $$5.applyTimeBased($$2, this.cacheTickId);
                    }
                    case 2 -> {
                        EnvironmentAttributeLayer.Positional $$6 = (EnvironmentAttributeLayer.Positional)environmentAttributeLayer;
                        yield $$6.applyPositional($$2, Objects.requireNonNull($$0), $$1);
                    }
                };
            }
            return this.attribute.sanitizeValue($$2);
        }

        private Value computeValueNotPositional() {
            Value $$0 = this.baseValue;
            for (EnvironmentAttributeLayer<Value> $$1 : this.layers) {
                EnvironmentAttributeLayer<Value> environmentAttributeLayer;
                Objects.requireNonNull($$1);
                int n = 0;
                $$0 = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EnvironmentAttributeLayer.Constant.class, EnvironmentAttributeLayer.TimeBased.class, EnvironmentAttributeLayer.Positional.class}, environmentAttributeLayer, n)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        EnvironmentAttributeLayer.Constant $$2 = (EnvironmentAttributeLayer.Constant)environmentAttributeLayer;
                        yield $$2.applyConstant($$0);
                    }
                    case 1 -> {
                        EnvironmentAttributeLayer.TimeBased $$3 = (EnvironmentAttributeLayer.TimeBased)environmentAttributeLayer;
                        yield $$3.applyTimeBased($$0, this.cacheTickId);
                    }
                    case 2 -> {
                        EnvironmentAttributeLayer.Positional $$4 = (EnvironmentAttributeLayer.Positional)environmentAttributeLayer;
                        yield $$0;
                    }
                };
            }
            return this.attribute.sanitizeValue($$0);
        }
    }

    public static class Builder {
        private final Map<EnvironmentAttribute<?>, List<EnvironmentAttributeLayer<?>>> layersByAttribute = new HashMap();

        Builder() {
        }

        public Builder addDefaultLayers(Level $$0) {
            EnvironmentAttributeSystem.addDefaultLayers(this, $$0);
            return this;
        }

        public Builder addConstantLayer(EnvironmentAttributeMap $$0) {
            for (EnvironmentAttribute<?> $$1 : $$0.keySet()) {
                this.addConstantEntry($$1, $$0);
            }
            return this;
        }

        private <Value> Builder addConstantEntry(EnvironmentAttribute<Value> $$0, EnvironmentAttributeMap $$1) {
            EnvironmentAttributeMap.Entry<Value, ?> $$2 = $$1.get($$0);
            if ($$2 == null) {
                throw new IllegalArgumentException("Missing attribute " + String.valueOf($$0));
            }
            return this.addConstantLayer($$0, $$2::applyModifier);
        }

        public <Value> Builder addConstantLayer(EnvironmentAttribute<Value> $$0, EnvironmentAttributeLayer.Constant<Value> $$1) {
            return this.addLayer($$0, $$1);
        }

        public <Value> Builder addTimeBasedLayer(EnvironmentAttribute<Value> $$0, EnvironmentAttributeLayer.TimeBased<Value> $$1) {
            return this.addLayer($$0, $$1);
        }

        public <Value> Builder addPositionalLayer(EnvironmentAttribute<Value> $$0, EnvironmentAttributeLayer.Positional<Value> $$1) {
            return this.addLayer($$0, $$1);
        }

        private <Value> Builder addLayer(EnvironmentAttribute<Value> $$02, EnvironmentAttributeLayer<Value> $$1) {
            this.layersByAttribute.computeIfAbsent($$02, $$0 -> new ArrayList()).add($$1);
            return this;
        }

        public Builder addTimelineLayer(Holder<Timeline> $$0, LongSupplier $$1) {
            for (EnvironmentAttribute<?> $$2 : $$0.value().attributes()) {
                this.addTimelineLayerForAttribute($$0, $$2, $$1);
            }
            return this;
        }

        private <Value> void addTimelineLayerForAttribute(Holder<Timeline> $$0, EnvironmentAttribute<Value> $$1, LongSupplier $$2) {
            this.addTimeBasedLayer($$1, $$0.value().createTrackSampler($$1, $$2));
        }

        public EnvironmentAttributeSystem build() {
            return new EnvironmentAttributeSystem(this.layersByAttribute);
        }
    }
}

