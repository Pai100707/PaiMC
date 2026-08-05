/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Sets
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public interface DataComponentMap
extends Iterable<TypedDataComponent<?>>,
DataComponentGetter {
    public static final DataComponentMap EMPTY = new DataComponentMap(){

        @Override
        public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
            return null;
        }

        @Override
        public Set<DataComponentType<?>> keySet() {
            return Set.of();
        }

        @Override
        public Iterator<TypedDataComponent<?>> iterator() {
            return Collections.emptyIterator();
        }
    };
    public static final Codec<DataComponentMap> CODEC = DataComponentMap.makeCodecFromMap(DataComponentType.VALUE_MAP_CODEC);

    public static Codec<DataComponentMap> makeCodec(Codec<DataComponentType<?>> $$0) {
        return DataComponentMap.makeCodecFromMap(Codec.dispatchedMap($$0, DataComponentType::codecOrThrow));
    }

    public static Codec<DataComponentMap> makeCodecFromMap(Codec<Map<DataComponentType<?>, Object>> $$02) {
        return $$02.flatComapMap(Builder::buildFromMapTrusted, $$0 -> {
            int $$1 = $$0.size();
            if ($$1 == 0) {
                return DataResult.success((Object)Reference2ObjectMaps.emptyMap());
            }
            Reference2ObjectArrayMap $$2 = new Reference2ObjectArrayMap($$1);
            for (TypedDataComponent<?> $$3 : $$0) {
                if ($$3.type().isTransient()) continue;
                $$2.put($$3.type(), $$3.value());
            }
            return DataResult.success((Object)$$2);
        });
    }

    public static DataComponentMap composite(final DataComponentMap $$0, final DataComponentMap $$1) {
        return new DataComponentMap(){

            @Override
            public <T> @Nullable T get(DataComponentType<? extends T> $$02) {
                T $$12 = $$1.get($$02);
                if ($$12 != null) {
                    return $$12;
                }
                return $$0.get($$02);
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return Sets.union($$0.keySet(), $$1.keySet());
            }
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public Set<DataComponentType<?>> keySet();

    default public boolean has(DataComponentType<?> $$0) {
        return this.get($$0) != null;
    }

    @Override
    default public Iterator<TypedDataComponent<?>> iterator() {
        return Iterators.transform(this.keySet().iterator(), $$0 -> Objects.requireNonNull(this.getTyped($$0)));
    }

    default public Stream<TypedDataComponent<?>> stream() {
        return StreamSupport.stream(Spliterators.spliterator(this.iterator(), (long)this.size(), 1345), false);
    }

    default public int size() {
        return this.keySet().size();
    }

    default public boolean isEmpty() {
        return this.size() == 0;
    }

    default public DataComponentMap filter(final Predicate<DataComponentType<?>> $$0) {
        return new DataComponentMap(){

            @Override
            public <T> @Nullable T get(DataComponentType<? extends T> $$02) {
                return $$0.test($$02) ? (T)DataComponentMap.this.get($$02) : null;
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return Sets.filter(DataComponentMap.this.keySet(), $$0::test);
            }
        };
    }

    public static class Builder {
        private final Reference2ObjectMap<DataComponentType<?>, Object> map = new Reference2ObjectArrayMap();

        Builder() {
        }

        public <T> Builder set(DataComponentType<T> $$0, @Nullable T $$1) {
            this.setUnchecked($$0, $$1);
            return this;
        }

        <T> void setUnchecked(DataComponentType<T> $$0, @Nullable Object $$1) {
            if ($$1 != null) {
                this.map.put($$0, $$1);
            } else {
                this.map.remove($$0);
            }
        }

        public Builder addAll(DataComponentMap $$0) {
            for (TypedDataComponent<?> $$1 : $$0) {
                this.map.put($$1.type(), $$1.value());
            }
            return this;
        }

        public DataComponentMap build() {
            return Builder.buildFromMapTrusted(this.map);
        }

        private static DataComponentMap buildFromMapTrusted(Map<DataComponentType<?>, Object> $$0) {
            if ($$0.isEmpty()) {
                return EMPTY;
            }
            if ($$0.size() < 8) {
                return new SimpleMap((Reference2ObjectMap<DataComponentType<?>, Object>)new Reference2ObjectArrayMap($$0));
            }
            return new SimpleMap((Reference2ObjectMap<DataComponentType<?>, Object>)new Reference2ObjectOpenHashMap($$0));
        }

        record SimpleMap(Reference2ObjectMap<DataComponentType<?>, Object> map) implements DataComponentMap
        {
            @Override
            public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
                return (T)this.map.get($$0);
            }

            @Override
            public boolean has(DataComponentType<?> $$0) {
                return this.map.containsKey($$0);
            }

            @Override
            public Set<DataComponentType<?>> keySet() {
                return this.map.keySet();
            }

            @Override
            public Iterator<TypedDataComponent<?>> iterator() {
                return Iterators.transform((Iterator)Reference2ObjectMaps.fastIterator(this.map), TypedDataComponent::fromEntryUnchecked);
            }

            @Override
            public int size() {
                return this.map.size();
            }

            @Override
            public String toString() {
                return this.map.toString();
            }
        }
    }
}

