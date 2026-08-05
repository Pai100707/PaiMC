/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FallbackResourceManager
implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<PackEntry> fallbacks = Lists.newArrayList();
    private final PackType type;
    private final String namespace;

    public FallbackResourceManager(PackType $$0, String $$1) {
        this.type = $$0;
        this.namespace = $$1;
    }

    public void push(PackResources $$0) {
        this.pushInternal($$0.packId(), $$0, null);
    }

    public void push(PackResources $$0, Predicate<Identifier> $$1) {
        this.pushInternal($$0.packId(), $$0, $$1);
    }

    public void pushFilterOnly(String $$0, Predicate<Identifier> $$1) {
        this.pushInternal($$0, null, $$1);
    }

    private void pushInternal(String $$0, @Nullable PackResources $$1, @Nullable Predicate<Identifier> $$2) {
        this.fallbacks.add(new PackEntry($$0, $$1, $$2));
    }

    @Override
    public Set<String> getNamespaces() {
        return ImmutableSet.of((Object)this.namespace);
    }

    @Override
    public Optional<Resource> getResource(Identifier $$0) {
        for (int $$1 = this.fallbacks.size() - 1; $$1 >= 0; --$$1) {
            IoSupplier<InputStream> $$4;
            PackEntry $$2 = this.fallbacks.get($$1);
            PackResources $$3 = $$2.resources;
            if ($$3 != null && ($$4 = $$3.getResource(this.type, $$0)) != null) {
                IoSupplier<ResourceMetadata> $$5 = this.createStackMetadataFinder($$0, $$1);
                return Optional.of(FallbackResourceManager.createResource($$3, $$0, $$4, $$5));
            }
            if (!$$2.isFiltered($$0)) continue;
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)$$0, (Object)$$2.name);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Resource createResource(PackResources $$0, Identifier $$1, IoSupplier<InputStream> $$2, IoSupplier<ResourceMetadata> $$3) {
        return new Resource($$0, FallbackResourceManager.wrapForDebug($$1, $$0, $$2), $$3);
    }

    private static IoSupplier<InputStream> wrapForDebug(Identifier $$0, PackResources $$1, IoSupplier<InputStream> $$2) {
        if (LOGGER.isDebugEnabled()) {
            return () -> new LeakedResourceWarningInputStream((InputStream)$$2.get(), $$0, $$1.packId());
        }
        return $$2;
    }

    @Override
    public List<Resource> getResourceStack(Identifier $$0) {
        Identifier $$1 = FallbackResourceManager.getMetadataLocation($$0);
        ArrayList<Resource> $$2 = new ArrayList<Resource>();
        boolean $$3 = false;
        String $$4 = null;
        for (int $$5 = this.fallbacks.size() - 1; $$5 >= 0; --$$5) {
            IoSupplier<InputStream> $$8;
            PackEntry $$6 = this.fallbacks.get($$5);
            PackResources $$7 = $$6.resources;
            if ($$7 != null && ($$8 = $$7.getResource(this.type, $$0)) != null) {
                IoSupplier<ResourceMetadata> $$10;
                if ($$3) {
                    IoSupplier<ResourceMetadata> $$9 = ResourceMetadata.EMPTY_SUPPLIER;
                } else {
                    $$10 = () -> {
                        IoSupplier<InputStream> $$2 = $$7.getResource(this.type, $$1);
                        return $$2 != null ? FallbackResourceManager.parseMetadata($$2) : ResourceMetadata.EMPTY;
                    };
                }
                $$2.add(new Resource($$7, $$8, $$10));
            }
            if ($$6.isFiltered($$0)) {
                $$4 = $$6.name;
                break;
            }
            if (!$$6.isFiltered($$1)) continue;
            $$3 = true;
        }
        if ($$2.isEmpty() && $$4 != null) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)$$0, $$4);
        }
        return Lists.reverse($$2);
    }

    private static boolean isMetadata(Identifier $$0) {
        return $$0.getPath().endsWith(".mcmeta");
    }

    private static Identifier getIdentifierFromMetadata(Identifier $$0) {
        String $$1 = $$0.getPath().substring(0, $$0.getPath().length() - ".mcmeta".length());
        return $$0.withPath($$1);
    }

    static Identifier getMetadataLocation(Identifier $$0) {
        return $$0.withPath($$0.getPath() + ".mcmeta");
    }

    @Override
    public Map<Identifier, Resource> listResources(String $$0, Predicate<Identifier> $$1) {
        final class ResourceWithSourceAndIndex
        extends Record {
            final PackResources packResources;
            final IoSupplier<InputStream> resource;
            final int packIndex;

            ResourceWithSourceAndIndex(PackResources $$0, IoSupplier<InputStream> $$1, int $$2) {
                this.packResources = $$0;
                this.resource = $$1;
                this.packIndex = $$2;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this);
            }

            @Override
            public final boolean equals(Object $$0) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResourceWithSourceAndIndex.class, "packResources;resource;packIndex", "packResources", "resource", "packIndex"}, this, $$0);
            }

            public PackResources packResources() {
                return this.packResources;
            }

            public IoSupplier<InputStream> resource() {
                return this.resource;
            }

            public int packIndex() {
                return this.packIndex;
            }
        }
        HashMap<Identifier, ResourceWithSourceAndIndex> $$22 = new HashMap<Identifier, ResourceWithSourceAndIndex>();
        HashMap $$32 = new HashMap();
        int $$4 = this.fallbacks.size();
        for (int $$52 = 0; $$52 < $$4; ++$$52) {
            PackEntry $$62 = this.fallbacks.get($$52);
            $$62.filterAll($$22.keySet());
            $$62.filterAll($$32.keySet());
            PackResources $$7 = $$62.resources;
            if ($$7 == null) continue;
            int $$8 = $$52;
            $$7.listResources(this.type, this.namespace, $$0, ($$5, $$6) -> {
                if (FallbackResourceManager.isMetadata($$5)) {
                    if ($$1.test(FallbackResourceManager.getIdentifierFromMetadata($$5))) {
                        $$32.put($$5, new ResourceWithSourceAndIndex($$7, (IoSupplier<InputStream>)$$6, $$8));
                    }
                } else if ($$1.test((Identifier)$$5)) {
                    $$22.put((Identifier)$$5, new ResourceWithSourceAndIndex($$7, (IoSupplier<InputStream>)$$6, $$8));
                }
            });
        }
        TreeMap $$9 = Maps.newTreeMap();
        $$22.forEach(($$2, $$3) -> {
            IoSupplier<ResourceMetadata> $$7;
            Identifier $$4 = FallbackResourceManager.getMetadataLocation($$2);
            ResourceWithSourceAndIndex $$5 = (ResourceWithSourceAndIndex)$$32.get($$4);
            if ($$5 != null && $$5.packIndex >= $$3.packIndex) {
                IoSupplier<ResourceMetadata> $$6 = FallbackResourceManager.convertToMetadata($$5.resource);
            } else {
                $$7 = ResourceMetadata.EMPTY_SUPPLIER;
            }
            $$9.put($$2, FallbackResourceManager.createResource($$3.packResources, $$2, $$3.resource, $$7));
        });
        return $$9;
    }

    private IoSupplier<ResourceMetadata> createStackMetadataFinder(Identifier $$0, int $$1) {
        return () -> {
            Identifier $$2 = FallbackResourceManager.getMetadataLocation($$0);
            for (int $$3 = this.fallbacks.size() - 1; $$3 >= $$1; --$$3) {
                IoSupplier<InputStream> $$6;
                PackEntry $$4 = this.fallbacks.get($$3);
                PackResources $$5 = $$4.resources;
                if ($$5 != null && ($$6 = $$5.getResource(this.type, $$2)) != null) {
                    return FallbackResourceManager.parseMetadata($$6);
                }
                if ($$4.isFiltered($$2)) break;
            }
            return ResourceMetadata.EMPTY;
        };
    }

    private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> $$0) {
        return () -> FallbackResourceManager.parseMetadata($$0);
    }

    private static ResourceMetadata parseMetadata(IoSupplier<InputStream> $$0) throws IOException {
        try (InputStream $$1 = $$0.get();){
            ResourceMetadata resourceMetadata = ResourceMetadata.fromJsonStream($$1);
            return resourceMetadata;
        }
    }

    private static void applyPackFiltersToExistingResources(PackEntry $$0, Map<Identifier, EntryStack> $$1) {
        for (EntryStack $$2 : $$1.values()) {
            if ($$0.isFiltered($$2.fileLocation)) {
                $$2.fileSources.clear();
                continue;
            }
            if (!$$0.isFiltered($$2.metadataLocation())) continue;
            $$2.metaSources.clear();
        }
    }

    private void listPackResources(PackEntry $$0, String $$1, Predicate<Identifier> $$2, Map<Identifier, EntryStack> $$32) {
        PackResources $$42 = $$0.resources;
        if ($$42 == null) {
            return;
        }
        $$42.listResources(this.type, this.namespace, $$1, ($$3, $$4) -> {
            if (FallbackResourceManager.isMetadata($$3)) {
                Identifier $$5 = FallbackResourceManager.getIdentifierFromMetadata($$3);
                if (!$$2.test($$5)) {
                    return;
                }
                $$1.computeIfAbsent($$5, (Function<Identifier, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.Identifier ), (Lnet/minecraft/resources/Identifier;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).metaSources.put($$42, (IoSupplier<InputStream>)$$4);
            } else {
                if (!$$2.test((Identifier)$$3)) {
                    return;
                }
                $$1.computeIfAbsent($$3, (Function<Identifier, EntryStack>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.resources.Identifier ), (Lnet/minecraft/resources/Identifier;)Lnet/minecraft/server/packs/resources/FallbackResourceManager$EntryStack;)()).fileSources.add(new ResourceWithSource($$42, (IoSupplier<InputStream>)$$4));
            }
        });
    }

    @Override
    public Map<Identifier, List<Resource>> listResourceStacks(String $$0, Predicate<Identifier> $$1) {
        HashMap $$2 = Maps.newHashMap();
        for (PackEntry $$3 : this.fallbacks) {
            FallbackResourceManager.applyPackFiltersToExistingResources($$3, $$2);
            this.listPackResources($$3, $$0, $$1, $$2);
        }
        TreeMap $$4 = Maps.newTreeMap();
        for (EntryStack $$5 : $$2.values()) {
            if ($$5.fileSources.isEmpty()) continue;
            ArrayList<Resource> $$6 = new ArrayList<Resource>();
            for (ResourceWithSource $$7 : $$5.fileSources) {
                PackResources $$8 = $$7.source;
                IoSupplier<InputStream> $$9 = $$5.metaSources.get($$8);
                IoSupplier<ResourceMetadata> $$10 = $$9 != null ? FallbackResourceManager.convertToMetadata($$9) : ResourceMetadata.EMPTY_SUPPLIER;
                $$6.add(FallbackResourceManager.createResource($$8, $$5.fileLocation, $$7.resource, $$10));
            }
            $$4.put($$5.fileLocation, $$6);
        }
        return $$4;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.fallbacks.stream().map($$0 -> $$0.resources).filter(Objects::nonNull);
    }

    static final class PackEntry
    extends Record {
        final String name;
        final @Nullable PackResources resources;
        private final @Nullable Predicate<Identifier> filter;

        PackEntry(String $$0, @Nullable PackResources $$1, @Nullable Predicate<Identifier> $$2) {
            this.name = $$0;
            this.resources = $$1;
            this.filter = $$2;
        }

        public void filterAll(Collection<Identifier> $$0) {
            if (this.filter != null) {
                $$0.removeIf(this.filter);
            }
        }

        public boolean isFiltered(Identifier $$0) {
            return this.filter != null && this.filter.test($$0);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PackEntry.class, "name;resources;filter", "name", "resources", "filter"}, this, $$0);
        }

        public String name() {
            return this.name;
        }

        public @Nullable PackResources resources() {
            return this.resources;
        }

        public @Nullable Predicate<Identifier> filter() {
            return this.filter;
        }
    }

    static final class EntryStack
    extends Record {
        final Identifier fileLocation;
        private final Identifier metadataLocation;
        final List<ResourceWithSource> fileSources;
        final Map<PackResources, IoSupplier<InputStream>> metaSources;

        EntryStack(Identifier $$0) {
            this($$0, FallbackResourceManager.getMetadataLocation($$0), new ArrayList<ResourceWithSource>(), (Map<PackResources, IoSupplier<InputStream>>)new Object2ObjectArrayMap());
        }

        private EntryStack(Identifier $$0, Identifier $$1, List<ResourceWithSource> $$2, Map<PackResources, IoSupplier<InputStream>> $$3) {
            this.fileLocation = $$0;
            this.metadataLocation = $$1;
            this.fileSources = $$2;
            this.metaSources = $$3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryStack.class, "fileLocation;metadataLocation;fileSources;metaSources", "fileLocation", "metadataLocation", "fileSources", "metaSources"}, this, $$0);
        }

        public Identifier fileLocation() {
            return this.fileLocation;
        }

        public Identifier metadataLocation() {
            return this.metadataLocation;
        }

        public List<ResourceWithSource> fileSources() {
            return this.fileSources;
        }

        public Map<PackResources, IoSupplier<InputStream>> metaSources() {
            return this.metaSources;
        }
    }

    static final class ResourceWithSource
    extends Record {
        final PackResources source;
        final IoSupplier<InputStream> resource;

        ResourceWithSource(PackResources $$0, IoSupplier<InputStream> $$1) {
            this.source = $$0;
            this.resource = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResourceWithSource.class, "source;resource", "source", "resource"}, this, $$0);
        }

        public PackResources source() {
            return this.source;
        }

        public IoSupplier<InputStream> resource() {
            return this.resource;
        }
    }

    static class LeakedResourceWarningInputStream
    extends FilterInputStream {
        private final Supplier<String> message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream $$0, Identifier $$1, String $$2) {
            super($$0);
            Exception $$3 = new Exception("Stacktrace");
            this.message = () -> {
                StringWriter $$3 = new StringWriter();
                $$3.printStackTrace(new PrintWriter($$3));
                return "Leaked resource: '" + String.valueOf($$1) + "' loaded from pack: '" + $$2 + "'\n" + String.valueOf($$3);
            };
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn("{}", (Object)this.message.get());
            }
            super.finalize();
        }
    }
}

