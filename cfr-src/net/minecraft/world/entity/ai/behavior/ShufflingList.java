/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.RandomSource;

public class ShufflingList<U>
implements Iterable<U> {
    protected final List<WeightedEntry<U>> entries;
    private final RandomSource random = RandomSource.create();

    public ShufflingList() {
        this.entries = Lists.newArrayList();
    }

    private ShufflingList(List<WeightedEntry<U>> $$0) {
        this.entries = Lists.newArrayList($$0);
    }

    public static <U> Codec<ShufflingList<U>> codec(Codec<U> $$02) {
        return WeightedEntry.codec($$02).listOf().xmap(ShufflingList::new, $$0 -> $$0.entries);
    }

    public ShufflingList<U> add(U $$0, int $$1) {
        this.entries.add(new WeightedEntry<U>($$0, $$1));
        return this;
    }

    public ShufflingList<U> shuffle() {
        this.entries.forEach($$0 -> $$0.setRandom(this.random.nextFloat()));
        this.entries.sort(Comparator.comparingDouble(WeightedEntry::getRandWeight));
        return this;
    }

    public Stream<U> stream() {
        return this.entries.stream().map(WeightedEntry::getData);
    }

    @Override
    public Iterator<U> iterator() {
        return Iterators.transform(this.entries.iterator(), WeightedEntry::getData);
    }

    public String toString() {
        return "ShufflingList[" + String.valueOf(this.entries) + "]";
    }

    public static class WeightedEntry<T> {
        final T data;
        final int weight;
        private double randWeight;

        WeightedEntry(T $$0, int $$1) {
            this.weight = $$1;
            this.data = $$0;
        }

        private double getRandWeight() {
            return this.randWeight;
        }

        void setRandom(float $$0) {
            this.randWeight = -Math.pow($$0, 1.0f / (float)this.weight);
        }

        public T getData() {
            return this.data;
        }

        public int getWeight() {
            return this.weight;
        }

        public String toString() {
            return this.weight + ":" + String.valueOf(this.data);
        }

        public static <E> Codec<WeightedEntry<E>> codec(final Codec<E> $$0) {
            return new Codec<WeightedEntry<E>>(){

                public <T> DataResult<Pair<WeightedEntry<E>, T>> decode(DynamicOps<T> $$02, T $$12) {
                    Dynamic $$2 = new Dynamic($$02, $$12);
                    return $$2.get("data").flatMap(arg_0 -> ((Codec)$$0).parse(arg_0)).map($$1 -> new WeightedEntry<Object>($$1, $$2.get("weight").asInt(1))).map($$1 -> Pair.of((Object)$$1, (Object)$$02.empty()));
                }

                public <T> DataResult<T> encode(WeightedEntry<E> $$02, DynamicOps<T> $$1, T $$2) {
                    return $$1.mapBuilder().add("weight", $$1.createInt($$02.weight)).add("data", $$0.encodeStart($$1, $$02.data)).build($$2);
                }

                public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                    return this.encode((WeightedEntry)object, dynamicOps, object2);
                }
            };
        }
    }
}

