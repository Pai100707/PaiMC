/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T>
extends DelegatingOps<T> {
    private final RegistryInfoLookup lookupProvider;

    public static <T> RegistryOps<T> create(DynamicOps<T> $$0, HolderLookup.Provider $$1) {
        return RegistryOps.create($$0, new HolderLookupAdapter($$1));
    }

    public static <T> RegistryOps<T> create(DynamicOps<T> $$0, RegistryInfoLookup $$1) {
        return new RegistryOps<T>($$0, $$1);
    }

    public static <T> Dynamic<T> injectRegistryContext(Dynamic<T> $$0, HolderLookup.Provider $$1) {
        return new Dynamic($$1.createSerializationContext($$0.getOps()), $$0.getValue());
    }

    private RegistryOps(DynamicOps<T> $$0, RegistryInfoLookup $$1) {
        super($$0);
        this.lookupProvider = $$1;
    }

    public <U> RegistryOps<U> withParent(DynamicOps<U> $$0) {
        if ($$0 == this.delegate) {
            return this;
        }
        return new RegistryOps<U>($$0, this.lookupProvider);
    }

    public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> $$0) {
        return this.lookupProvider.lookup($$0).map(RegistryInfo::owner);
    }

    public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> $$0) {
        return this.lookupProvider.lookup($$0).map(RegistryInfo::getter);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        RegistryOps $$1 = (RegistryOps)$$0;
        return this.delegate.equals((Object)$$1.delegate) && this.lookupProvider.equals($$1.lookupProvider);
    }

    public int hashCode() {
        return this.delegate.hashCode() * 31 + this.lookupProvider.hashCode();
    }

    public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends Registry<? extends E>> $$02) {
        return ExtraCodecs.retrieveContext($$1 -> {
            if ($$1 instanceof RegistryOps) {
                RegistryOps $$2 = (RegistryOps)$$1;
                return $$2.lookupProvider.lookup($$02).map($$0 -> DataResult.success($$0.getter(), (Lifecycle)$$0.elementsLifecycle())).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + String.valueOf($$02)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter($$0 -> null);
    }

    public static <E, O> RecordCodecBuilder<O, Holder.Reference<E>> retrieveElement(ResourceKey<E> $$02) {
        ResourceKey $$1 = ResourceKey.createRegistryKey($$02.registry());
        return ExtraCodecs.retrieveContext($$2 -> {
            if ($$2 instanceof RegistryOps) {
                RegistryOps $$3 = (RegistryOps)$$2;
                return $$3.lookupProvider.lookup($$1).flatMap($$1 -> $$1.getter().get($$02)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Can't find value: " + String.valueOf($$02)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter($$0 -> null);
    }

    static final class HolderLookupAdapter
    implements RegistryInfoLookup {
        private final HolderLookup.Provider lookupProvider;
        private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryInfo<?>>> lookups = new ConcurrentHashMap();

        public HolderLookupAdapter(HolderLookup.Provider $$0) {
            this.lookupProvider = $$0;
        }

        public <E> Optional<RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> $$0) {
            return this.lookups.computeIfAbsent($$0, this::createLookup);
        }

        private Optional<RegistryInfo<Object>> createLookup(ResourceKey<? extends Registry<?>> $$0) {
            return this.lookupProvider.lookup($$0).map(RegistryInfo::fromRegistryLookup);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object $$0) {
            if (this == $$0) {
                return true;
            }
            if (!($$0 instanceof HolderLookupAdapter)) return false;
            HolderLookupAdapter $$1 = (HolderLookupAdapter)$$0;
            if (!this.lookupProvider.equals($$1.lookupProvider)) return false;
            return true;
        }

        public int hashCode() {
            return this.lookupProvider.hashCode();
        }
    }

    public static interface RegistryInfoLookup {
        public <T> Optional<RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);
    }

    public record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
        public static <T> RegistryInfo<T> fromRegistryLookup(HolderLookup.RegistryLookup<T> $$0) {
            return new RegistryInfo<T>($$0, $$0, $$0.registryLifecycle());
        }
    }
}

