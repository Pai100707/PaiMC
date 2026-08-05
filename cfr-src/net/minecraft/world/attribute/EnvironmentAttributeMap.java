/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import org.jspecify.annotations.Nullable;

public final class EnvironmentAttributeMap {
    public static final EnvironmentAttributeMap EMPTY = new EnvironmentAttributeMap(Map.of());
    public static final Codec<EnvironmentAttributeMap> CODEC = Codec.lazyInitialized(() -> Codec.dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(Entry::createCodec)).xmap(EnvironmentAttributeMap::new, $$0 -> $$0.entries));
    public static final Codec<EnvironmentAttributeMap> NETWORK_CODEC = CODEC.xmap(EnvironmentAttributeMap::filterSyncable, EnvironmentAttributeMap::filterSyncable);
    public static final Codec<EnvironmentAttributeMap> CODEC_ONLY_POSITIONAL = CODEC.validate($$02 -> {
        List<EnvironmentAttribute> $$1 = $$02.keySet().stream().filter($$0 -> !$$0.isPositional()).toList();
        if (!$$1.isEmpty()) {
            return DataResult.error(() -> "The following attributes cannot be positional: " + String.valueOf($$1));
        }
        return DataResult.success((Object)$$02);
    });
    final Map<EnvironmentAttribute<?>, Entry<?, ?>> entries;

    private static EnvironmentAttributeMap filterSyncable(EnvironmentAttributeMap $$0) {
        return new EnvironmentAttributeMap(Map.copyOf(Maps.filterKeys($$0.entries, EnvironmentAttribute::isSyncable)));
    }

    EnvironmentAttributeMap(Map<EnvironmentAttribute<?>, Entry<?, ?>> $$0) {
        this.entries = $$0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <Value> @Nullable Entry<Value, ?> get(EnvironmentAttribute<Value> $$0) {
        return this.entries.get($$0);
    }

    public <Value> Value applyModifier(EnvironmentAttribute<Value> $$0, Value $$1) {
        Entry<Value, ?> $$2 = this.get($$0);
        return $$2 != null ? $$2.applyModifier($$1) : $$1;
    }

    public boolean contains(EnvironmentAttribute<?> $$0) {
        return this.entries.containsKey($$0);
    }

    public Set<EnvironmentAttribute<?>> keySet() {
        return this.entries.keySet();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if ($$0 == this) {
            return true;
        }
        if (!($$0 instanceof EnvironmentAttributeMap)) return false;
        EnvironmentAttributeMap $$1 = (EnvironmentAttributeMap)$$0;
        if (!this.entries.equals($$1.entries)) return false;
        return true;
    }

    public int hashCode() {
        return this.entries.hashCode();
    }

    public String toString() {
        return this.entries.toString();
    }

    public static class Builder {
        private final Map<EnvironmentAttribute<?>, Entry<?, ?>> entries = new HashMap();

        Builder() {
        }

        public Builder putAll(EnvironmentAttributeMap $$0) {
            this.entries.putAll($$0.entries);
            return this;
        }

        public <Value, Parameter> Builder modify(EnvironmentAttribute<Value> $$0, AttributeModifier<Value, Parameter> $$1, Parameter $$2) {
            $$0.type().checkAllowedModifier($$1);
            this.entries.put($$0, new Entry<Value, Parameter>($$2, $$1));
            return this;
        }

        public <Value> Builder set(EnvironmentAttribute<Value> $$0, Value $$1) {
            return this.modify($$0, AttributeModifier.override(), $$1);
        }

        public EnvironmentAttributeMap build() {
            if (this.entries.isEmpty()) {
                return EMPTY;
            }
            return new EnvironmentAttributeMap(Map.copyOf(this.entries));
        }
    }

    public record Entry<Value, Argument>(Argument argument, AttributeModifier<Value, Argument> modifier) {
        private static <Value> Codec<Entry<Value, ?>> createCodec(EnvironmentAttribute<Value> $$03) {
            Codec $$12 = $$03.type().modifierCodec().dispatch("modifier", Entry::modifier, Util.memoize($$1 -> Entry.createFullCodec($$03, $$1)));
            return Codec.either($$03.valueCodec(), (Codec)$$12).xmap($$02 -> (Entry)$$02.map($$0 -> new Entry($$0, AttributeModifier.override()), $$0 -> $$0), $$0 -> {
                if ($$0.modifier == AttributeModifier.override()) {
                    return Either.left($$0.argument());
                }
                return Either.right((Object)$$0);
            });
        }

        private static <Value, Argument> MapCodec<Entry<Value, Argument>> createFullCodec(EnvironmentAttribute<Value> $$0, AttributeModifier<Value, Argument> $$1) {
            return RecordCodecBuilder.mapCodec($$2 -> $$2.group((App)$$1.argumentCodec($$0).fieldOf("argument").forGetter(Entry::argument)).apply((Applicative)$$2, $$1 -> new Entry($$1, $$1)));
        }

        public Value applyModifier(Value $$0) {
            return this.modifier.apply($$0, this.argument);
        }
    }
}

