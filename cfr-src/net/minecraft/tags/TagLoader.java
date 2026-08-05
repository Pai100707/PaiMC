/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.tags;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.StrictJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final ElementLookup<T> elementLookup;
    private final String directory;

    public TagLoader(ElementLookup<T> $$0, String $$1) {
        this.elementLookup = $$0;
        this.directory = $$1;
    }

    public Map<Identifier, List<EntryWithSource>> load(ResourceManager $$02) {
        HashMap<Identifier, List<EntryWithSource>> $$1 = new HashMap<Identifier, List<EntryWithSource>>();
        FileToIdConverter $$22 = FileToIdConverter.json(this.directory);
        for (Map.Entry<Identifier, List<Resource>> $$3 : $$22.listMatchingResourceStacks($$02).entrySet()) {
            Identifier $$4 = $$3.getKey();
            Identifier $$5 = $$22.fileToId($$4);
            for (Resource $$6 : $$3.getValue()) {
                try {
                    BufferedReader $$7 = $$6.openAsReader();
                    try {
                        JsonElement $$8 = StrictJsonParser.parse($$7);
                        List $$9 = $$1.computeIfAbsent($$5, $$0 -> new ArrayList());
                        TagFile $$10 = (TagFile)TagFile.CODEC.parse(new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)$$8)).getOrThrow();
                        if ($$10.replace()) {
                            $$9.clear();
                        }
                        String $$11 = $$6.sourcePackId();
                        $$10.entries().forEach($$2 -> $$9.add(new EntryWithSource((TagEntry)$$2, $$11)));
                    }
                    finally {
                        if ($$7 == null) continue;
                        ((Reader)$$7).close();
                    }
                }
                catch (Exception $$12) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{$$5, $$4, $$6.sourcePackId(), $$12});
                }
            }
        }
        return $$1;
    }

    private Either<List<EntryWithSource>, List<T>> tryBuildTag(TagEntry.Lookup<T> $$0, List<EntryWithSource> $$1) {
        LinkedHashSet $$2 = new LinkedHashSet();
        ArrayList<EntryWithSource> $$3 = new ArrayList<EntryWithSource>();
        for (EntryWithSource $$4 : $$1) {
            if ($$4.entry().build($$0, $$2::add)) continue;
            $$3.add($$4);
        }
        return $$3.isEmpty() ? Either.right(List.copyOf($$2)) : Either.left($$3);
    }

    public Map<Identifier, List<T>> build(Map<Identifier, List<EntryWithSource>> $$0) {
        final HashMap $$12 = new HashMap();
        TagEntry.Lookup $$23 = new TagEntry.Lookup<T>(){

            @Override
            public @Nullable T element(Identifier $$0, boolean $$1) {
                return TagLoader.this.elementLookup.get($$0, $$1).orElse(null);
            }

            @Override
            public @Nullable Collection<T> tag(Identifier $$0) {
                return (Collection)$$12.get($$0);
            }
        };
        DependencySorter<Identifier, SortingEntry> $$32 = new DependencySorter<Identifier, SortingEntry>();
        $$0.forEach(($$1, $$2) -> $$32.addEntry((Identifier)$$1, new SortingEntry((List<EntryWithSource>)$$2)));
        $$32.orderByDependencies(($$22, $$3) -> this.tryBuildTag($$23, $$3.entries).ifLeft($$1 -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", $$22, (Object)$$1.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight($$2 -> $$12.put((Identifier)$$22, (List)$$2)));
        return $$12;
    }

    public static <T> void loadTagsFromNetwork(TagNetworkSerialization.NetworkPayload $$0, WritableRegistry<T> $$1) {
        $$0.resolve($$1).tags.forEach($$1::bindTag);
    }

    public static List<Registry.PendingTags<?>> loadTagsForExistingRegistries(ResourceManager $$0, RegistryAccess $$12) {
        return $$12.registries().map($$1 -> TagLoader.loadPendingTags($$0, $$1.value())).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
    }

    public static <T> void loadTagsForRegistry(ResourceManager $$0, WritableRegistry<T> $$1) {
        ResourceKey $$22 = $$1.key();
        TagLoader<Holder<T>> $$32 = new TagLoader<Holder<T>>(ElementLookup.fromWritableRegistry($$1), Registries.tagsDirPath($$22));
        $$32.build($$32.load($$0)).forEach(($$2, $$3) -> $$1.bindTag(TagKey.create($$22, $$2), (List)$$3));
    }

    private static <T> Map<TagKey<T>, List<Holder<T>>> wrapTags(ResourceKey<? extends Registry<T>> $$0, Map<Identifier, List<Holder<T>>> $$12) {
        return $$12.entrySet().stream().collect(Collectors.toUnmodifiableMap($$1 -> TagKey.create($$0, (Identifier)$$1.getKey()), Map.Entry::getValue));
    }

    private static <T> Optional<Registry.PendingTags<T>> loadPendingTags(ResourceManager $$0, Registry<T> $$1) {
        ResourceKey<Registry<T>> $$2 = $$1.key();
        TagLoader<Holder<T>> $$3 = new TagLoader<Holder<T>>(ElementLookup.fromFrozenRegistry($$1), Registries.tagsDirPath($$2));
        LoadResult<T> $$4 = new LoadResult<T>($$2, TagLoader.wrapTags($$1.key(), $$3.build($$3.load($$0))));
        return $$4.tags().isEmpty() ? Optional.empty() : Optional.of($$1.prepareTagReload($$4));
    }

    public static List<HolderLookup.RegistryLookup<?>> buildUpdatedLookups(RegistryAccess.Frozen $$0, List<Registry.PendingTags<?>> $$1) {
        ArrayList $$22 = new ArrayList();
        $$0.registries().forEach($$2 -> {
            Registry.PendingTags $$3 = TagLoader.findTagsForRegistry($$1, $$2.key());
            $$22.add($$3 != null ? $$3.lookup() : $$2.value());
        });
        return $$22;
    }

    private static @Nullable Registry.PendingTags<?> findTagsForRegistry(List<Registry.PendingTags<?>> $$0, ResourceKey<? extends Registry<?>> $$1) {
        for (Registry.PendingTags<?> $$2 : $$0) {
            if ($$2.key() != $$1) continue;
            return $$2;
        }
        return null;
    }

    public static interface ElementLookup<T> {
        public Optional<? extends T> get(Identifier var1, boolean var2);

        public static <T> ElementLookup<? extends Holder<T>> fromFrozenRegistry(Registry<T> $$0) {
            return ($$1, $$2) -> $$0.get($$1);
        }

        public static <T> ElementLookup<Holder<T>> fromWritableRegistry(WritableRegistry<T> $$0) {
            HolderGetter $$1 = $$0.createRegistrationLookup();
            return ($$2, $$3) -> ($$3 ? $$1 : $$0).get(ResourceKey.create($$0.key(), $$2));
        }
    }

    public static final class EntryWithSource
    extends Record {
        final TagEntry entry;
        private final String source;

        public EntryWithSource(TagEntry $$0, String $$1) {
            this.entry = $$0;
            this.source = $$1;
        }

        @Override
        public String toString() {
            return String.valueOf(this.entry) + " (from " + this.source + ")";
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryWithSource.class, "entry;source", "entry", "source"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryWithSource.class, "entry;source", "entry", "source"}, this, $$0);
        }

        public TagEntry entry() {
            return this.entry;
        }

        public String source() {
            return this.source;
        }
    }

    public static final class LoadResult<T>
    extends Record {
        private final ResourceKey<? extends Registry<T>> key;
        final Map<TagKey<T>, List<Holder<T>>> tags;

        public LoadResult(ResourceKey<? extends Registry<T>> $$0, Map<TagKey<T>, List<Holder<T>>> $$1) {
            this.key = $$0;
            this.tags = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoadResult.class, "key;tags", "key", "tags"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoadResult.class, "key;tags", "key", "tags"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoadResult.class, "key;tags", "key", "tags"}, this, $$0);
        }

        public ResourceKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Map<TagKey<T>, List<Holder<T>>> tags() {
            return this.tags;
        }
    }

    static final class SortingEntry
    extends Record
    implements DependencySorter.Entry<Identifier> {
        final List<EntryWithSource> entries;

        SortingEntry(List<EntryWithSource> $$0) {
            this.entries = $$0;
        }

        @Override
        public void visitRequiredDependencies(Consumer<Identifier> $$0) {
            this.entries.forEach($$1 -> $$1.entry.visitRequiredDependencies($$0));
        }

        @Override
        public void visitOptionalDependencies(Consumer<Identifier> $$0) {
            this.entries.forEach($$1 -> $$1.entry.visitOptionalDependencies($$0));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SortingEntry.class, "entries", "entries"}, this, $$0);
        }

        public List<EntryWithSource> entries() {
            return this.entries;
        }
    }
}

