/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Iterators
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class MappedRegistry<T>
implements WritableRegistry<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList(256);
    private final Reference2IntMap<T> toId = (Reference2IntMap)Util.make(new Reference2IntOpenHashMap(), $$0 -> $$0.defaultReturnValue(-1));
    private final Map<Identifier, Holder.Reference<T>> byLocation = new HashMap<Identifier, Holder.Reference<T>>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<ResourceKey<T>, Holder.Reference<T>>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<T, Holder.Reference<T>>();
    private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos = new IdentityHashMap<ResourceKey<T>, RegistrationInfo>();
    private Lifecycle registryLifecycle;
    private final Map<TagKey<T>, HolderSet.Named<T>> frozenTags = new IdentityHashMap<TagKey<T>, HolderSet.Named<T>>();
    TagSet<T> allTags = TagSet.unbound();
    private boolean frozen;
    private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return this.getTags();
    }

    public MappedRegistry(ResourceKey<? extends Registry<T>> $$0, Lifecycle $$1) {
        this($$0, $$1, false);
    }

    public MappedRegistry(ResourceKey<? extends Registry<T>> $$02, Lifecycle $$1, boolean $$2) {
        this.key = $$02;
        this.registryLifecycle = $$1;
        if ($$2) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap<T, Holder.Reference<T>>();
        }
    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    public String toString() {
        return "Registry[" + String.valueOf(this.key) + " (" + String.valueOf(this.registryLifecycle) + ")]";
    }

    private void validateWrite() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void validateWrite(ResourceKey<T> $$0) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + String.valueOf($$0) + ")");
        }
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> $$02, T $$1, RegistrationInfo $$2) {
        Holder.Reference $$4;
        this.validateWrite($$02);
        Objects.requireNonNull($$02);
        Objects.requireNonNull($$1);
        if (this.byLocation.containsKey($$02.identifier())) {
            throw Util.pauseInIde(new IllegalStateException("Adding duplicate key '" + String.valueOf($$02) + "' to registry"));
        }
        if (this.byValue.containsKey($$1)) {
            throw Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + String.valueOf($$1) + "' to registry"));
        }
        if (this.unregisteredIntrusiveHolders != null) {
            Holder.Reference<T> $$3 = this.unregisteredIntrusiveHolders.remove($$1);
            if ($$3 == null) {
                throw new AssertionError((Object)("Missing intrusive holder for " + String.valueOf($$02) + ":" + String.valueOf($$1)));
            }
            $$3.bindKey($$02);
        } else {
            $$4 = this.byKey.computeIfAbsent($$02, $$0 -> Holder.Reference.createStandAlone(this, $$0));
        }
        this.byKey.put($$02, $$4);
        this.byLocation.put($$02.identifier(), $$4);
        this.byValue.put($$1, $$4);
        int $$5 = this.byId.size();
        this.byId.add((Object)$$4);
        this.toId.put($$1, $$5);
        this.registrationInfos.put($$02, $$2);
        this.registryLifecycle = this.registryLifecycle.add($$2.lifecycle());
        return $$4;
    }

    @Override
    public @Nullable Identifier getKey(T $$0) {
        Holder.Reference<T> $$1 = this.byValue.get($$0);
        return $$1 != null ? $$1.key().identifier() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T $$0) {
        return Optional.ofNullable(this.byValue.get($$0)).map(Holder.Reference::key);
    }

    @Override
    public int getId(@Nullable T $$0) {
        return this.toId.getInt($$0);
    }

    @Override
    public @Nullable T getValue(@Nullable ResourceKey<T> $$0) {
        return MappedRegistry.getValueFromNullable(this.byKey.get($$0));
    }

    @Override
    public @Nullable T byId(int $$0) {
        if ($$0 < 0 || $$0 >= this.byId.size()) {
            return null;
        }
        return ((Holder.Reference)this.byId.get($$0)).value();
    }

    @Override
    public Optional<Holder.Reference<T>> get(int $$0) {
        if ($$0 < 0 || $$0 >= this.byId.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable((Holder.Reference)this.byId.get($$0));
    }

    @Override
    public Optional<Holder.Reference<T>> get(Identifier $$0) {
        return Optional.ofNullable(this.byLocation.get($$0));
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> $$0) {
        return Optional.ofNullable(this.byKey.get($$0));
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return this.byId.isEmpty() ? Optional.empty() : Optional.of((Holder.Reference)this.byId.getFirst());
    }

    @Override
    public Holder<T> wrapAsHolder(T $$0) {
        Holder.Reference<T> $$1 = this.byValue.get($$0);
        return $$1 != null ? $$1 : Holder.direct($$0);
    }

    Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> $$02) {
        return this.byKey.computeIfAbsent($$02, $$0 -> {
            if (this.unregisteredIntrusiveHolders != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            }
            this.validateWrite((ResourceKey<T>)$$0);
            return Holder.Reference.createStandAlone(this, $$0);
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> $$0) {
        return Optional.ofNullable(this.registrationInfos.get($$0));
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.registryLifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform((Iterator)this.byId.iterator(), Holder::value);
    }

    @Override
    public @Nullable T getValue(@Nullable Identifier $$0) {
        Holder.Reference<T> $$1 = this.byLocation.get($$0);
        return MappedRegistry.getValueFromNullable($$1);
    }

    private static <T> @Nullable T getValueFromNullable(@Nullable Holder.Reference<T> $$0) {
        return $$0 != null ? (T)$$0.value() : null;
    }

    @Override
    public Set<Identifier> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Collections.unmodifiableSet(this.byKey.keySet());
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Util.mapValuesLazy(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return this.byId.stream();
    }

    @Override
    public Stream<HolderSet.Named<T>> getTags() {
        return this.allTags.getTags();
    }

    HolderSet.Named<T> getOrCreateTagForRegistration(TagKey<T> $$0) {
        return this.frozenTags.computeIfAbsent($$0, this::createTag);
    }

    private HolderSet.Named<T> createTag(TagKey<T> $$0) {
        return new HolderSet.Named<T>(this, $$0);
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource $$0) {
        return Util.getRandomSafe(this.byId, $$0);
    }

    @Override
    public boolean containsKey(Identifier $$0) {
        return this.byLocation.containsKey($$0);
    }

    @Override
    public boolean containsKey(ResourceKey<T> $$0) {
        return this.byKey.containsKey($$0);
    }

    @Override
    public Registry<T> freeze() {
        if (this.frozen) {
            return this;
        }
        this.frozen = true;
        this.byValue.forEach((? super K $$0, ? super V $$1) -> $$1.bindValue($$0));
        List<Identifier> $$02 = this.byKey.entrySet().stream().filter($$0 -> !((Holder.Reference)$$0.getValue()).isBound()).map($$0 -> ((ResourceKey)$$0.getKey()).identifier()).sorted().toList();
        if (!$$02.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + String.valueOf(this.key()) + ": " + String.valueOf($$02));
        }
        if (this.unregisteredIntrusiveHolders != null) {
            if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                throw new IllegalStateException("Some intrusive holders were not registered: " + String.valueOf(this.unregisteredIntrusiveHolders.values()));
            }
            this.unregisteredIntrusiveHolders = null;
        }
        if (this.allTags.isBound()) {
            throw new IllegalStateException("Tags already present before freezing");
        }
        List<Identifier> $$12 = this.frozenTags.entrySet().stream().filter($$0 -> !((HolderSet.Named)$$0.getValue()).isBound()).map($$0 -> ((TagKey)$$0.getKey()).location()).sorted().toList();
        if (!$$12.isEmpty()) {
            throw new IllegalStateException("Unbound tags in registry " + String.valueOf(this.key()) + ": " + String.valueOf($$12));
        }
        this.allTags = TagSet.fromMap(this.frozenTags);
        this.refreshTagsInHolders();
        return this;
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T $$02) {
        if (this.unregisteredIntrusiveHolders == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        }
        this.validateWrite();
        return this.unregisteredIntrusiveHolders.computeIfAbsent($$02, $$0 -> Holder.Reference.createIntrusive(this, $$0));
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> $$0) {
        return this.allTags.get($$0);
    }

    private Holder.Reference<T> validateAndUnwrapTagElement(TagKey<T> $$0, Holder<T> $$1) {
        if (!$$1.canSerializeIn(this)) {
            throw new IllegalStateException("Can't create named set " + String.valueOf($$0) + " containing value " + String.valueOf($$1) + " from outside registry " + String.valueOf(this));
        }
        if ($$1 instanceof Holder.Reference) {
            Holder.Reference $$2 = (Holder.Reference)$$1;
            return $$2;
        }
        throw new IllegalStateException("Found direct holder " + String.valueOf($$1) + " value in tag " + String.valueOf($$0));
    }

    @Override
    public void bindTag(TagKey<T> $$0, List<Holder<T>> $$1) {
        this.validateWrite();
        this.getOrCreateTagForRegistration($$0).bind($$1);
    }

    void refreshTagsInHolders() {
        IdentityHashMap<Holder.Reference, List> $$0 = new IdentityHashMap<Holder.Reference, List>();
        this.byKey.values().forEach($$1 -> $$0.put((Holder.Reference)$$1, new ArrayList()));
        this.allTags.forEach((? super TagKey<T> $$1, ? super HolderSet.Named<T> $$2) -> {
            for (Holder $$3 : $$2) {
                Holder.Reference $$4 = this.validateAndUnwrapTagElement((TagKey<T>)$$1, $$3);
                ((List)$$0.get($$4)).add($$1);
            }
        });
        $$0.forEach(Holder.Reference::bindTags);
    }

    public void bindAllTagsToEmpty() {
        this.validateWrite();
        this.frozenTags.values().forEach($$0 -> $$0.bind(List.of()));
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        this.validateWrite();
        return new HolderGetter<T>(){

            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> $$0) {
                return Optional.of(this.getOrThrow($$0));
            }

            @Override
            public Holder.Reference<T> getOrThrow(ResourceKey<T> $$0) {
                return MappedRegistry.this.getOrCreateHolderOrThrow($$0);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> $$0) {
                return Optional.of(this.getOrThrow($$0));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> $$0) {
                return MappedRegistry.this.getOrCreateTagForRegistration($$0);
            }
        };
    }

    @Override
    public Registry.PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> $$0) {
        if (!this.frozen) {
            throw new IllegalStateException("Invalid method used for tag loading");
        }
        ImmutableMap.Builder $$1 = ImmutableMap.builder();
        final HashMap $$22 = new HashMap();
        $$0.tags().forEach((? super K $$2, ? super V $$3) -> {
            HolderSet.Named<T> $$4 = this.frozenTags.get($$2);
            if ($$4 == null) {
                $$4 = this.createTag((TagKey<T>)$$2);
            }
            $$1.put($$2, $$4);
            $$22.put($$2, List.copyOf($$3));
        });
        final ImmutableMap $$32 = $$1.build();
        final HolderLookup.RegistryLookup.Delegate $$4 = new HolderLookup.RegistryLookup.Delegate<T>(){

            @Override
            public HolderLookup.RegistryLookup<T> parent() {
                return MappedRegistry.this;
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> $$0) {
                return Optional.ofNullable((HolderSet.Named)$$32.get($$0));
            }

            @Override
            public Stream<HolderSet.Named<T>> listTags() {
                return $$32.values().stream();
            }
        };
        return new Registry.PendingTags<T>(){

            @Override
            public ResourceKey<? extends Registry<? extends T>> key() {
                return MappedRegistry.this.key();
            }

            @Override
            public int size() {
                return $$22.size();
            }

            @Override
            public HolderLookup.RegistryLookup<T> lookup() {
                return $$4;
            }

            @Override
            public void apply() {
                $$32.forEach(($$1, $$2) -> {
                    List $$3 = $$22.getOrDefault($$1, List.of());
                    $$2.bind($$3);
                });
                MappedRegistry.this.allTags = TagSet.fromMap($$32);
                MappedRegistry.this.refreshTagsInHolders();
            }
        };
    }

    static interface TagSet<T> {
        public static <T> TagSet<T> unbound() {
            return new TagSet<T>(){

                @Override
                public boolean isBound() {
                    return false;
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> $$0) {
                    throw new IllegalStateException("Tags not bound, trying to access " + String.valueOf($$0));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> $$0) {
                    throw new IllegalStateException("Tags not bound");
                }

                @Override
                public Stream<HolderSet.Named<T>> getTags() {
                    throw new IllegalStateException("Tags not bound");
                }
            };
        }

        public static <T> TagSet<T> fromMap(final Map<TagKey<T>, HolderSet.Named<T>> $$0) {
            return new TagSet<T>(){

                @Override
                public boolean isBound() {
                    return true;
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> $$02) {
                    return Optional.ofNullable((HolderSet.Named)$$0.get($$02));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> $$02) {
                    $$0.forEach($$02);
                }

                @Override
                public Stream<HolderSet.Named<T>> getTags() {
                    return $$0.values().stream();
                }
            };
        }

        public boolean isBound();

        public Optional<HolderSet.Named<T>> get(TagKey<T> var1);

        public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> var1);

        public Stream<HolderSet.Named<T>> getTags();
    }
}

