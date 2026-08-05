/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess
extends HolderLookup.Provider {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Frozen EMPTY = new ImmutableRegistryAccess(Map.of()).freeze();

    public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> var1);

    default public <E> Registry<E> lookupOrThrow(ResourceKey<? extends Registry<? extends E>> $$0) {
        return this.lookup($$0).orElseThrow(() -> new IllegalStateException("Missing registry: " + String.valueOf($$0)));
    }

    public Stream<RegistryEntry<?>> registries();

    @Override
    default public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
        return this.registries().map($$0 -> $$0.key);
    }

    public static Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> $$0) {
        return new Frozen(){

            public <T> Optional<Registry<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$02) {
                Registry $$1 = $$0;
                return $$1.getOptional($$02);
            }

            @Override
            public Stream<RegistryEntry<?>> registries() {
                return $$0.entrySet().stream().map(RegistryEntry::fromMapEntry);
            }

            @Override
            public Frozen freeze() {
                return this;
            }
        };
    }

    default public Frozen freeze() {
        class FrozenAccess
        extends ImmutableRegistryAccess
        implements Frozen {
            protected FrozenAccess(RegistryAccess $$0, Stream<RegistryEntry<?>> $$1) {
                super($$1);
            }
        }
        return new FrozenAccess(this, this.registries().map(RegistryEntry::freeze));
    }

    @Override
    default public /* synthetic */ HolderLookup.RegistryLookup lookupOrThrow(ResourceKey resourceKey) {
        return this.lookupOrThrow(resourceKey);
    }

    @Override
    default public /* synthetic */ HolderGetter lookupOrThrow(ResourceKey resourceKey) {
        return this.lookupOrThrow(resourceKey);
    }

    public static final class RegistryEntry<T>
    extends Record {
        final ResourceKey<? extends Registry<T>> key;
        private final Registry<T> value;

        public RegistryEntry(ResourceKey<? extends Registry<T>> $$0, Registry<T> $$1) {
            this.key = $$0;
            this.value = $$1;
        }

        private static <T, R extends Registry<? extends T>> RegistryEntry<T> fromMapEntry(Map.Entry<? extends ResourceKey<? extends Registry<?>>, R> $$0) {
            return RegistryEntry.fromUntyped($$0.getKey(), (Registry)$$0.getValue());
        }

        private static <T> RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> $$0, Registry<?> $$1) {
            return new RegistryEntry($$0, $$1);
        }

        private RegistryEntry<T> freeze() {
            return new RegistryEntry<T>(this.key, this.value.freeze());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryEntry.class, "key;value", "key", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryEntry.class, "key;value", "key", "value"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryEntry.class, "key;value", "key", "value"}, this, $$0);
        }

        public ResourceKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Registry<T> value() {
            return this.value;
        }
    }

    public static class ImmutableRegistryAccess
    implements RegistryAccess {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableRegistryAccess(List<? extends Registry<?>> $$02) {
            this.registries = $$02.stream().collect(Collectors.toUnmodifiableMap(Registry::key, $$0 -> $$0));
        }

        public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> $$0) {
            this.registries = Map.copyOf($$0);
        }

        public ImmutableRegistryAccess(Stream<RegistryEntry<?>> $$0) {
            this.registries = (Map)$$0.collect(ImmutableMap.toImmutableMap(RegistryEntry::key, RegistryEntry::value));
        }

        @Override
        public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> $$02) {
            return Optional.ofNullable(this.registries.get($$02)).map($$0 -> $$0);
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            return this.registries.entrySet().stream().map(RegistryEntry::fromMapEntry);
        }
    }

    public static interface Frozen
    extends RegistryAccess {
    }
}

