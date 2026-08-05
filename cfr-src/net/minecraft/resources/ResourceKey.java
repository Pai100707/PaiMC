/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.MapMaker
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class ResourceKey<T> {
    private static final ConcurrentMap<InternKey, ResourceKey<?>> VALUES = new MapMaker().weakValues().makeMap();
    private final Identifier registryName;
    private final Identifier identifier;

    public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> $$0) {
        return Identifier.CODEC.xmap($$1 -> ResourceKey.create($$0, $$1), ResourceKey::identifier);
    }

    public static <T> StreamCodec<ByteBuf, ResourceKey<T>> streamCodec(ResourceKey<? extends Registry<T>> $$0) {
        return Identifier.STREAM_CODEC.map($$1 -> ResourceKey.create($$0, $$1), ResourceKey::identifier);
    }

    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> $$0, Identifier $$1) {
        return ResourceKey.create($$0.identifier, $$1);
    }

    public static <T> ResourceKey<Registry<T>> createRegistryKey(Identifier $$0) {
        return ResourceKey.create(Registries.ROOT_REGISTRY_NAME, $$0);
    }

    private static <T> ResourceKey<T> create(Identifier $$02, Identifier $$1) {
        return VALUES.computeIfAbsent(new InternKey($$02, $$1), $$0 -> new ResourceKey($$0.registry, $$0.identifier));
    }

    private ResourceKey(Identifier $$0, Identifier $$1) {
        this.registryName = $$0;
        this.identifier = $$1;
    }

    public String toString() {
        return "ResourceKey[" + String.valueOf(this.registryName) + " / " + String.valueOf(this.identifier) + "]";
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> $$0) {
        return this.registryName.equals($$0.identifier());
    }

    public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> $$0) {
        return this.isFor($$0) ? Optional.of(this) : Optional.empty();
    }

    public Identifier identifier() {
        return this.identifier;
    }

    public Identifier registry() {
        return this.registryName;
    }

    public ResourceKey<Registry<T>> registryKey() {
        return ResourceKey.createRegistryKey(this.registryName);
    }

    static final class InternKey
    extends Record {
        final Identifier registry;
        final Identifier identifier;

        InternKey(Identifier $$0, Identifier $$1) {
            this.registry = $$0;
            this.identifier = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InternKey.class, "registry;identifier", "registry", "identifier"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InternKey.class, "registry;identifier", "registry", "identifier"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InternKey.class, "registry;identifier", "registry", "identifier"}, this, $$0);
        }

        public Identifier registry() {
            return this.registry;
        }

        public Identifier identifier() {
            return this.identifier;
        }
    }
}

