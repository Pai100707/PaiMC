/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Cloner;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class RegistrySetBuilder {
    private final List<RegistryStub<?>> entries = new ArrayList();

    static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.RegistryLookup<T> $$0) {
        return new EmptyTagLookup<T>($$0){

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> $$02) {
                return $$0.get($$02);
            }
        };
    }

    static <T> HolderLookup.RegistryLookup<T> lookupFromMap(final ResourceKey<? extends Registry<? extends T>> $$0, final Lifecycle $$1, HolderOwner<T> $$2, final Map<ResourceKey<T>, Holder.Reference<T>> $$3) {
        return new EmptyTagRegistryLookup<T>($$2){

            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return $$0;
            }

            @Override
            public Lifecycle registryLifecycle() {
                return $$1;
            }

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> $$02) {
                return Optional.ofNullable((Holder.Reference)$$3.get($$02));
            }

            @Override
            public Stream<Holder.Reference<T>> listElements() {
                return $$3.values().stream();
            }
        };
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> $$0, Lifecycle $$1, RegistryBootstrap<T> $$2) {
        this.entries.add(new RegistryStub<T>($$0, $$1, $$2));
        return this;
    }

    public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> $$0, RegistryBootstrap<T> $$1) {
        return this.add($$0, Lifecycle.stable(), $$1);
    }

    private BuildState createState(RegistryAccess $$0) {
        BuildState $$12 = BuildState.create($$0, this.entries.stream().map(RegistryStub::key));
        this.entries.forEach($$1 -> $$1.apply($$12));
        return $$12;
    }

    private static HolderLookup.Provider buildProviderWithContext(UniversalOwner $$0, RegistryAccess $$12, Stream<HolderLookup.RegistryLookup<?>> $$22) {
        record Entry<T>(HolderLookup.RegistryLookup<T> lookup, RegistryOps.RegistryInfo<T> opsInfo) {
            public static <T> Entry<T> createForContextRegistry(HolderLookup.RegistryLookup<T> $$0) {
                return new Entry<T>(new EmptyTagLookupWrapper<T>($$0, $$0), RegistryOps.RegistryInfo.fromRegistryLookup($$0));
            }

            public static <T> Entry<T> createForNewRegistry(UniversalOwner $$0, HolderLookup.RegistryLookup<T> $$1) {
                return new Entry(new EmptyTagLookupWrapper($$0.cast(), $$1), new RegistryOps.RegistryInfo($$0.cast(), $$1, $$1.registryLifecycle()));
            }
        }
        final HashMap $$3 = new HashMap();
        $$12.registries().forEach($$1 -> $$3.put($$1.key(), Entry.createForContextRegistry($$1.value())));
        $$22.forEach($$2 -> $$3.put($$2.key(), Entry.createForNewRegistry($$0, $$2)));
        return new HolderLookup.Provider(){

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return $$3.keySet().stream();
            }

            <T> Optional<Entry<T>> getEntry(ResourceKey<? extends Registry<? extends T>> $$0) {
                return Optional.ofNullable((Entry)$$3.get($$0));
            }

            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
                return this.getEntry($$0).map(Entry::lookup);
            }

            @Override
            public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> $$0) {
                return RegistryOps.create($$0, new RegistryOps.RegistryInfoLookup(){

                    @Override
                    public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
                        return this.getEntry($$0).map(Entry::opsInfo);
                    }
                });
            }
        };
    }

    public HolderLookup.Provider build(RegistryAccess $$0) {
        BuildState $$12 = this.createState($$0);
        Stream<HolderLookup.RegistryLookup<?>> $$2 = this.entries.stream().map($$1 -> $$1.collectRegisteredValues($$12).buildAsLookup($$0.owner));
        HolderLookup.Provider $$3 = RegistrySetBuilder.buildProviderWithContext($$12.owner, $$0, $$2);
        $$12.reportNotCollectedHolders();
        $$12.reportUnclaimedRegisteredValues();
        $$12.throwOnError();
        return $$3;
    }

    private HolderLookup.Provider createLazyFullPatchedRegistries(RegistryAccess $$0, HolderLookup.Provider $$1, Cloner.Factory $$2, Map<ResourceKey<? extends Registry<?>>, RegistryContents<?>> $$3, HolderLookup.Provider $$4) {
        UniversalOwner $$52 = new UniversalOwner();
        MutableObject $$6 = new MutableObject();
        List $$7 = $$3.keySet().stream().map($$5 -> this.createLazyFullPatchedRegistries($$52, $$2, (ResourceKey)$$5, $$4, $$1, (MutableObject<HolderLookup.Provider>)$$6)).collect(Collectors.toUnmodifiableList());
        HolderLookup.Provider $$8 = RegistrySetBuilder.buildProviderWithContext($$52, $$0, $$7.stream());
        $$6.setValue((Object)$$8);
        return $$8;
    }

    private <T> HolderLookup.RegistryLookup<T> createLazyFullPatchedRegistries(HolderOwner<T> $$0, Cloner.Factory $$1, ResourceKey<? extends Registry<? extends T>> $$2, HolderLookup.Provider $$3, HolderLookup.Provider $$4, MutableObject<HolderLookup.Provider> $$52) {
        Cloner $$6 = $$1.cloner($$2);
        if ($$6 == null) {
            throw new NullPointerException("No cloner for " + String.valueOf($$2.identifier()));
        }
        HashMap $$7 = new HashMap();
        HolderGetter $$8 = $$3.lookupOrThrow($$2);
        $$8.listElements().forEach($$5 -> {
            ResourceKey $$6 = $$5.key();
            LazyHolder $$7 = new LazyHolder($$0, $$6);
            $$7.supplier = () -> $$6.clone($$5.value(), $$3, (HolderLookup.Provider)$$52.get());
            $$7.put($$6, $$7);
        });
        HolderGetter $$9 = $$4.lookupOrThrow($$2);
        $$9.listElements().forEach($$5 -> {
            ResourceKey $$62 = $$5.key();
            $$7.computeIfAbsent($$62, $$6 -> {
                LazyHolder $$7 = new LazyHolder($$0, $$62);
                $$7.supplier = () -> $$6.clone($$5.value(), $$4, (HolderLookup.Provider)$$52.get());
                return $$7;
            });
        });
        Lifecycle $$10 = $$8.registryLifecycle().add($$9.registryLifecycle());
        return RegistrySetBuilder.lookupFromMap($$2, $$10, $$0, $$7);
    }

    public PatchedRegistries buildPatch(RegistryAccess $$0, HolderLookup.Provider $$12, Cloner.Factory $$2) {
        BuildState $$3 = this.createState($$0);
        HashMap $$4 = new HashMap();
        this.entries.stream().map($$1 -> $$1.collectRegisteredValues($$3)).forEach($$1 -> $$4.put((ResourceKey<Registry<?>>)$$1.key, (RegistryContents<?>)$$1));
        Set $$5 = $$0.listRegistryKeys().collect(Collectors.toUnmodifiableSet());
        $$12.listRegistryKeys().filter($$1 -> !$$5.contains($$1)).forEach($$1 -> $$4.putIfAbsent((ResourceKey<Registry<?>>)$$1, new RegistryContents($$1, Lifecycle.stable(), Map.of())));
        Stream<HolderLookup.RegistryLookup<?>> $$6 = $$4.values().stream().map($$1 -> $$1.buildAsLookup($$0.owner));
        HolderLookup.Provider $$7 = RegistrySetBuilder.buildProviderWithContext($$3.owner, $$0, $$6);
        $$3.reportUnclaimedRegisteredValues();
        $$3.throwOnError();
        HolderLookup.Provider $$8 = this.createLazyFullPatchedRegistries($$0, $$12, $$2, $$4, $$7);
        return new PatchedRegistries($$8, $$7);
    }

    record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistryBootstrap<T> bootstrap) {
        void apply(BuildState $$0) {
            this.bootstrap.run($$0.bootstrapContext());
        }

        public RegistryContents<T> collectRegisteredValues(BuildState $$0) {
            HashMap $$1 = new HashMap();
            Iterator<Map.Entry<ResourceKey<?>, RegisteredValue<?>>> $$2 = $$0.registeredValues.entrySet().iterator();
            while ($$2.hasNext()) {
                Map.Entry<ResourceKey<?>, RegisteredValue<?>> $$3 = $$2.next();
                ResourceKey<?> $$4 = $$3.getKey();
                if (!$$4.isFor(this.key)) continue;
                ResourceKey<?> $$5 = $$4;
                RegisteredValue<?> $$6 = $$3.getValue();
                Holder.Reference<Object> $$7 = $$0.lookup.holders.remove($$4);
                $$1.put($$5, new ValueAndHolder($$6, Optional.ofNullable($$7)));
                $$2.remove();
            }
            return new RegistryContents(this.key, this.lifecycle, $$1);
        }
    }

    @FunctionalInterface
    public static interface RegistryBootstrap<T> {
        public void run(BootstrapContext<T> var1);
    }

    static final class BuildState
    extends Record {
        final UniversalOwner owner;
        final UniversalLookup lookup;
        final Map<Identifier, HolderGetter<?>> registries;
        final Map<ResourceKey<?>, RegisteredValue<?>> registeredValues;
        final List<RuntimeException> errors;

        private BuildState(UniversalOwner $$0, UniversalLookup $$1, Map<Identifier, HolderGetter<?>> $$2, Map<ResourceKey<?>, RegisteredValue<?>> $$3, List<RuntimeException> $$4) {
            this.owner = $$0;
            this.lookup = $$1;
            this.registries = $$2;
            this.registeredValues = $$3;
            this.errors = $$4;
        }

        public static BuildState create(RegistryAccess $$0, Stream<ResourceKey<? extends Registry<?>>> $$12) {
            UniversalOwner $$22 = new UniversalOwner();
            ArrayList<RuntimeException> $$3 = new ArrayList<RuntimeException>();
            UniversalLookup $$4 = new UniversalLookup($$22);
            ImmutableMap.Builder $$5 = ImmutableMap.builder();
            $$0.registries().forEach($$1 -> $$5.put((Object)$$1.key().identifier(), RegistrySetBuilder.wrapContextLookup($$1.value())));
            $$12.forEach($$2 -> $$5.put((Object)$$2.identifier(), (Object)$$4));
            return new BuildState($$22, $$4, (Map<Identifier, HolderGetter<?>>)$$5.build(), new HashMap(), (List<RuntimeException>)$$3);
        }

        public <T> BootstrapContext<T> bootstrapContext() {
            return new BootstrapContext<T>(){

                @Override
                public Holder.Reference<T> register(ResourceKey<T> $$0, T $$1, Lifecycle $$2) {
                    RegisteredValue $$3 = registeredValues.put($$0, new RegisteredValue($$1, $$2));
                    if ($$3 != null) {
                        errors.add(new IllegalStateException("Duplicate registration for " + String.valueOf($$0) + ", new=" + String.valueOf($$1) + ", old=" + String.valueOf($$3.value)));
                    }
                    return lookup.getOrCreate($$0);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> $$0) {
                    return registries.getOrDefault($$0.identifier(), lookup);
                }
            };
        }

        public void reportUnclaimedRegisteredValues() {
            this.registeredValues.forEach(($$0, $$1) -> this.errors.add(new IllegalStateException("Orpaned value " + String.valueOf($$1.value) + " for key " + String.valueOf($$0))));
        }

        public void reportNotCollectedHolders() {
            for (ResourceKey<Object> $$0 : this.lookup.holders.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + String.valueOf($$0)));
            }
        }

        public void throwOnError() {
            if (!this.errors.isEmpty()) {
                IllegalStateException $$0 = new IllegalStateException("Errors during registry creation");
                for (RuntimeException $$1 : this.errors) {
                    $$0.addSuppressed($$1);
                }
                throw $$0;
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BuildState.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BuildState.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BuildState.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this, $$0);
        }

        public UniversalOwner owner() {
            return this.owner;
        }

        public UniversalLookup lookup() {
            return this.lookup;
        }

        public Map<Identifier, HolderGetter<?>> registries() {
            return this.registries;
        }

        public Map<ResourceKey<?>, RegisteredValue<?>> registeredValues() {
            return this.registeredValues;
        }

        public List<RuntimeException> errors() {
            return this.errors;
        }
    }

    static class UniversalOwner
    implements HolderOwner<Object> {
        UniversalOwner() {
        }

        public <T> HolderOwner<T> cast() {
            return this;
        }
    }

    public record PatchedRegistries(HolderLookup.Provider full, HolderLookup.Provider patches) {
    }

    static final class RegistryContents<T>
    extends Record {
        final ResourceKey<? extends Registry<? extends T>> key;
        private final Lifecycle lifecycle;
        private final Map<ResourceKey<T>, ValueAndHolder<T>> values;

        RegistryContents(ResourceKey<? extends Registry<? extends T>> $$0, Lifecycle $$1, Map<ResourceKey<T>, ValueAndHolder<T>> $$2) {
            this.key = $$0;
            this.lifecycle = $$1;
            this.values = $$2;
        }

        public HolderLookup.RegistryLookup<T> buildAsLookup(UniversalOwner $$0) {
            Map $$12 = this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, $$1 -> {
                ValueAndHolder $$2 = (ValueAndHolder)$$1.getValue();
                Holder.Reference $$3 = $$2.holder().orElseGet(() -> Holder.Reference.createStandAlone($$0.cast(), (ResourceKey)$$1.getKey()));
                $$3.bindValue($$2.value().value());
                return $$3;
            }));
            return RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, $$0.cast(), $$12);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryContents.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryContents.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryContents.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this, $$0);
        }

        public ResourceKey<? extends Registry<? extends T>> key() {
            return this.key;
        }

        public Lifecycle lifecycle() {
            return this.lifecycle;
        }

        public Map<ResourceKey<T>, ValueAndHolder<T>> values() {
            return this.values;
        }
    }

    static class LazyHolder<T>
    extends Holder.Reference<T> {
        @Nullable Supplier<T> supplier;

        protected LazyHolder(HolderOwner<T> $$0, @Nullable ResourceKey<T> $$1) {
            super(Holder.Reference.Type.STAND_ALONE, $$0, $$1, null);
        }

        @Override
        protected void bindValue(T $$0) {
            super.bindValue($$0);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.bindValue(this.supplier.get());
            }
            return super.value();
        }
    }

    record ValueAndHolder<T>(RegisteredValue<T> value, Optional<Holder.Reference<T>> holder) {
    }

    static final class RegisteredValue<T>
    extends Record {
        final T value;
        private final Lifecycle lifecycle;

        RegisteredValue(T $$0, Lifecycle $$1) {
            this.value = $$0;
            this.lifecycle = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this, $$0);
        }

        public T value() {
            return this.value;
        }

        public Lifecycle lifecycle() {
            return this.lifecycle;
        }
    }

    static class UniversalLookup
    extends EmptyTagLookup<Object> {
        final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap<ResourceKey<Object>, Holder.Reference<Object>>();

        public UniversalLookup(HolderOwner<Object> $$0) {
            super($$0);
        }

        @Override
        public Optional<Holder.Reference<Object>> get(ResourceKey<Object> $$0) {
            return Optional.of(this.getOrCreate($$0));
        }

        <T> Holder.Reference<T> getOrCreate(ResourceKey<T> $$02) {
            return this.holders.computeIfAbsent($$02, $$0 -> Holder.Reference.createStandAlone(this.owner, $$0));
        }
    }

    static class EmptyTagLookupWrapper<T>
    extends EmptyTagRegistryLookup<T>
    implements HolderLookup.RegistryLookup.Delegate<T> {
        private final HolderLookup.RegistryLookup<T> parent;

        EmptyTagLookupWrapper(HolderOwner<T> $$0, HolderLookup.RegistryLookup<T> $$1) {
            super($$0);
            this.parent = $$1;
        }

        @Override
        public HolderLookup.RegistryLookup<T> parent() {
            return this.parent;
        }
    }

    static abstract class EmptyTagRegistryLookup<T>
    extends EmptyTagLookup<T>
    implements HolderLookup.RegistryLookup<T> {
        protected EmptyTagRegistryLookup(HolderOwner<T> $$0) {
            super($$0);
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            throw new UnsupportedOperationException("Tags are not available in datagen");
        }
    }

    static abstract class EmptyTagLookup<T>
    implements HolderGetter<T> {
        protected final HolderOwner<T> owner;

        protected EmptyTagLookup(HolderOwner<T> $$0) {
            this.owner = $$0;
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> $$0) {
            return Optional.of(HolderSet.emptyNamed(this.owner, $$0));
        }
    }
}

