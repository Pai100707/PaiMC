/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;

public class EncoderCache {
    final LoadingCache<Key<?, ?>, DataResult<?>> cache;

    public EncoderCache(int $$0) {
        this.cache = CacheBuilder.newBuilder().maximumSize((long)$$0).concurrencyLevel(1).softValues().build(new CacheLoader<Key<?, ?>, DataResult<?>>(this){

            public DataResult<?> load(Key<?, ?> $$0) {
                return $$0.resolve();
            }

            public /* synthetic */ Object load(Object object) throws Exception {
                return this.load((Key)object);
            }
        });
    }

    public <A> Codec<A> wrap(final Codec<A> $$0) {
        return new Codec<A>(){

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> $$02, T $$1) {
                return $$0.decode($$02, $$1);
            }

            public <T> DataResult<T> encode(A $$02, DynamicOps<T> $$1, T $$2) {
                return ((DataResult)EncoderCache.this.cache.getUnchecked(new Key($$0, $$02, $$1))).map($$0 -> {
                    if ($$0 instanceof Tag) {
                        Tag $$1 = (Tag)$$0;
                        return $$1.copy();
                    }
                    return $$0;
                });
            }
        };
    }

    record Key<A, T>(Codec<A> codec, A value, DynamicOps<T> ops) {
        public DataResult<T> resolve() {
            return this.codec.encodeStart(this.ops, this.value);
        }

        @Override
        public boolean equals(Object $$0) {
            if (this == $$0) {
                return true;
            }
            if ($$0 instanceof Key) {
                Key $$1 = (Key)$$0;
                return this.codec == $$1.codec && this.value.equals($$1.value) && this.ops.equals($$1.ops);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int $$0 = System.identityHashCode(this.codec);
            $$0 = 31 * $$0 + this.value.hashCode();
            $$0 = 31 * $$0 + this.ops.hashCode();
            return $$0;
        }
    }
}

