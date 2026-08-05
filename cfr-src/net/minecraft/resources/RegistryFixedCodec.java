/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

public final class RegistryFixedCodec<E>
implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public static <E> RegistryFixedCodec<E> create(ResourceKey<? extends Registry<E>> $$0) {
        return new RegistryFixedCodec<E>($$0);
    }

    private RegistryFixedCodec(ResourceKey<? extends Registry<E>> $$0) {
        this.registryKey = $$0;
    }

    public <T> DataResult<T> encode(Holder<E> $$02, DynamicOps<T> $$1, T $$22) {
        RegistryOps $$3;
        Optional $$4;
        if ($$1 instanceof RegistryOps && ($$4 = ($$3 = (RegistryOps)$$1).owner(this.registryKey)).isPresent()) {
            if (!$$02.canSerializeIn($$4.get())) {
                return DataResult.error(() -> "Element " + String.valueOf($$02) + " is not valid in current registry set");
            }
            return (DataResult)$$02.unwrap().map($$2 -> Identifier.CODEC.encode((Object)$$2.identifier(), $$1, $$22), $$0 -> DataResult.error(() -> "Elements from registry " + String.valueOf(this.registryKey) + " can't be serialized to a value"));
        }
        return DataResult.error(() -> "Can't access registry " + String.valueOf(this.registryKey));
    }

    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> $$0, T $$1) {
        RegistryOps $$2;
        Optional $$3;
        if ($$0 instanceof RegistryOps && ($$3 = ($$2 = (RegistryOps)$$0).getter(this.registryKey)).isPresent()) {
            return Identifier.CODEC.decode($$0, $$1).flatMap($$12 -> {
                Identifier $$2 = (Identifier)$$12.getFirst();
                return ((HolderGetter)$$3.get()).get(ResourceKey.create(this.registryKey, $$2)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + String.valueOf($$2))).map($$1 -> Pair.of((Object)$$1, (Object)$$12.getSecond())).setLifecycle(Lifecycle.stable());
            });
        }
        return DataResult.error(() -> "Can't access registry " + String.valueOf(this.registryKey));
    }

    public String toString() {
        return "RegistryFixedCodec[" + String.valueOf(this.registryKey) + "]";
    }

    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((Holder)object, dynamicOps, object2);
    }
}

