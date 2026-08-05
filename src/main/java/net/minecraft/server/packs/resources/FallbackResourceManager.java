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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class FallbackResourceManager implements ResourceManager {
   static final Logger LOGGER = LogUtils.getLogger();
   protected final List<FallbackResourceManager.PackEntry> fallbacks = Lists.newArrayList();
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

   private void pushInternal(String $$0, PackResources $$1, Predicate<Identifier> $$2) {
      this.fallbacks.add(new FallbackResourceManager.PackEntry($$0, $$1, $$2));
   }

   @Override
   public Set<String> getNamespaces() {
      return ImmutableSet.of(this.namespace);
   }

   @Override
   public Optional<Resource> getResource(Identifier $$0) {
      for (int $$1 = this.fallbacks.size() - 1; $$1 >= 0; $$1--) {
         FallbackResourceManager.PackEntry $$2 = this.fallbacks.get($$1);
         PackResources $$3 = $$2.resources;
         if ($$3 != null) {
            IoSupplier<InputStream> $$4 = $$3.getResource(this.type, $$0);
            if ($$4 != null) {
               IoSupplier<ResourceMetadata> $$5 = this.createStackMetadataFinder($$0, $$1);
               return Optional.of(createResource($$3, $$0, $$4, $$5));
            }
         }

         if ($$2.isFiltered($$0)) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", $$0, $$2.name);
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   private static Resource createResource(PackResources $$0, Identifier $$1, IoSupplier<InputStream> $$2, IoSupplier<ResourceMetadata> $$3) {
      return new Resource($$0, wrapForDebug($$1, $$0, $$2), $$3);
   }

   private static IoSupplier<InputStream> wrapForDebug(Identifier $$0, PackResources $$1, IoSupplier<InputStream> $$2) {
      return LOGGER.isDebugEnabled() ? () -> new FallbackResourceManager.LeakedResourceWarningInputStream($$2.get(), $$0, $$1.packId()) : $$2;
   }

   @Override
   public List<Resource> getResourceStack(Identifier $$0) {
      Identifier $$1 = getMetadataLocation($$0);
      List<Resource> $$2 = new ArrayList<>();
      boolean $$3 = false;
      String $$4 = null;

      for (int $$5 = this.fallbacks.size() - 1; $$5 >= 0; $$5--) {
         FallbackResourceManager.PackEntry $$6 = this.fallbacks.get($$5);
         PackResources $$7 = $$6.resources;
         if ($$7 != null) {
            IoSupplier<InputStream> $$8 = $$7.getResource(this.type, $$0);
            if ($$8 != null) {
               IoSupplier<ResourceMetadata> $$9;
               if ($$3) {
                  $$9 = ResourceMetadata.EMPTY_SUPPLIER;
               } else {
                  $$9 = () -> {
                     IoSupplier<InputStream> $$2x = $$7.getResource(this.type, $$1);
                     return $$2x != null ? parseMetadata($$2x) : ResourceMetadata.EMPTY;
                  };
               }

               $$2.add(new Resource($$7, $$8, $$9));
            }
         }

         if ($$6.isFiltered($$0)) {
            $$4 = $$6.name;
            break;
         }

         if ($$6.isFiltered($$1)) {
            $$3 = true;
         }
      }

      if ($$2.isEmpty() && $$4 != null) {
         LOGGER.warn("Resource {} not found, but was filtered by pack {}", $$0, $$4);
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
      record ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> resource, int packIndex) {
      }

      Map<Identifier, ResourceWithSourceAndIndex> $$2 = new HashMap<>();
      Map<Identifier, ResourceWithSourceAndIndex> $$3 = new HashMap<>();
      int $$4 = this.fallbacks.size();

      for (int $$5 = 0; $$5 < $$4; $$5++) {
         FallbackResourceManager.PackEntry $$6 = this.fallbacks.get($$5);
         $$6.filterAll($$2.keySet());
         $$6.filterAll($$3.keySet());
         PackResources $$7 = $$6.resources;
         if ($$7 != null) {
            int $$8 = $$5;
            $$7.listResources(this.type, this.namespace, $$0, ($$5x, $$6x) -> {
               if (isMetadata($$5x)) {
                  if ($$1.test(getIdentifierFromMetadata($$5x))) {
                     $$3.put($$5x, new ResourceWithSourceAndIndex($$7, $$6x, $$8));
                  }
               } else if ($$1.test($$5x)) {
                  $$2.put($$5x, new ResourceWithSourceAndIndex($$7, $$6x, $$8));
               }
            });
         }
      }

      Map<Identifier, Resource> $$9 = Maps.newTreeMap();
      $$2.forEach(($$2x, $$3x) -> {
         Identifier $$4x = getMetadataLocation($$2x);
         ResourceWithSourceAndIndex $$5x = $$3.get($$4x);
         IoSupplier<ResourceMetadata> $$6x;
         if ($$5x != null && $$5x.packIndex >= $$3x.packIndex) {
            $$6x = convertToMetadata($$5x.resource);
         } else {
            $$6x = ResourceMetadata.EMPTY_SUPPLIER;
         }

         $$9.put($$2x, createResource($$3x.packResources, $$2x, $$3x.resource, $$6x));
      });
      return $$9;
   }

   private IoSupplier<ResourceMetadata> createStackMetadataFinder(Identifier $$0, int $$1) {
      return () -> {
         Identifier $$2 = getMetadataLocation($$0);

         for (int $$3 = this.fallbacks.size() - 1; $$3 >= $$1; $$3--) {
            FallbackResourceManager.PackEntry $$4 = this.fallbacks.get($$3);
            PackResources $$5 = $$4.resources;
            if ($$5 != null) {
               IoSupplier<InputStream> $$6 = $$5.getResource(this.type, $$2);
               if ($$6 != null) {
                  return parseMetadata($$6);
               }
            }

            if ($$4.isFiltered($$2)) {
               break;
            }
         }

         return ResourceMetadata.EMPTY;
      };
   }

   private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> $$0) {
      return () -> parseMetadata($$0);
   }

   private static ResourceMetadata parseMetadata(IoSupplier<InputStream> $$0) throws IOException {
      ResourceMetadata var2;
      try (InputStream $$1 = $$0.get()) {
         var2 = ResourceMetadata.fromJsonStream($$1);
      }

      return var2;
   }

   private static void applyPackFiltersToExistingResources(FallbackResourceManager.PackEntry $$0, Map<Identifier, FallbackResourceManager.EntryStack> $$1) {
      for (FallbackResourceManager.EntryStack $$2 : $$1.values()) {
         if ($$0.isFiltered($$2.fileLocation)) {
            $$2.fileSources.clear();
         } else if ($$0.isFiltered($$2.metadataLocation())) {
            $$2.metaSources.clear();
         }
      }
   }

   private void listPackResources(
      FallbackResourceManager.PackEntry $$0, String $$1, Predicate<Identifier> $$2, Map<Identifier, FallbackResourceManager.EntryStack> $$3
   ) {
      PackResources $$4 = $$0.resources;
      if ($$4 != null) {
         $$4.listResources(this.type, this.namespace, $$1, ($$3x, $$4x) -> {
            if (isMetadata($$3x)) {
               Identifier $$5 = getIdentifierFromMetadata($$3x);
               if (!$$2.test($$5)) {
                  return;
               }

               $$3.computeIfAbsent($$5, FallbackResourceManager.EntryStack::new).metaSources.put($$4, $$4x);
            } else {
               if (!$$2.test($$3x)) {
                  return;
               }

               $$3.computeIfAbsent($$3x, FallbackResourceManager.EntryStack::new).fileSources.add(new FallbackResourceManager.ResourceWithSource($$4, $$4x));
            }
         });
      }
   }

   @Override
   public Map<Identifier, List<Resource>> listResourceStacks(String $$0, Predicate<Identifier> $$1) {
      Map<Identifier, FallbackResourceManager.EntryStack> $$2 = Maps.newHashMap();

      for (FallbackResourceManager.PackEntry $$3 : this.fallbacks) {
         applyPackFiltersToExistingResources($$3, $$2);
         this.listPackResources($$3, $$0, $$1, $$2);
      }

      TreeMap<Identifier, List<Resource>> $$4 = Maps.newTreeMap();

      for (FallbackResourceManager.EntryStack $$5 : $$2.values()) {
         if (!$$5.fileSources.isEmpty()) {
            List<Resource> $$6 = new ArrayList<>();

            for (FallbackResourceManager.ResourceWithSource $$7 : $$5.fileSources) {
               PackResources $$8 = $$7.source;
               IoSupplier<InputStream> $$9 = $$5.metaSources.get($$8);
               IoSupplier<ResourceMetadata> $$10 = $$9 != null ? convertToMetadata($$9) : ResourceMetadata.EMPTY_SUPPLIER;
               $$6.add(createResource($$8, $$5.fileLocation, $$7.resource, $$10));
            }

            $$4.put($$5.fileLocation, $$6);
         }
      }

      return $$4;
   }

   @Override
   public Stream<PackResources> listPacks() {
      return this.fallbacks.stream().map($$0 -> $$0.resources).filter(Objects::nonNull);
   }

   record EntryStack(
      Identifier fileLocation,
      Identifier metadataLocation,
      List<FallbackResourceManager.ResourceWithSource> fileSources,
      Map<PackResources, IoSupplier<InputStream>> metaSources
   ) {

      EntryStack(Identifier $$0) {
         this($$0, FallbackResourceManager.getMetadataLocation($$0), new ArrayList<>(), new Object2ObjectArrayMap());
      }
   }

   static class LeakedResourceWarningInputStream extends FilterInputStream {
      private final Supplier<String> message;
      private boolean closed;

      public LeakedResourceWarningInputStream(InputStream $$0, Identifier $$1, String $$2) {
         super($$0);
         Exception $$3 = new Exception("Stacktrace");
         this.message = () -> {
            StringWriter $$3x = new StringWriter();
            $$3.printStackTrace(new PrintWriter($$3x));
            return "Leaked resource: '" + $$1 + "' loaded from pack: '" + $$2 + "'\n" + $$3x;
         };
      }

      @Override
      public void close() throws IOException {
         super.close();
         this.closed = true;
      }

      @Override
      protected void finalize() throws Throwable {
         if (!this.closed) {
            FallbackResourceManager.LOGGER.warn("{}", this.message.get());
         }

         super.finalize();
      }
   }

   record PackEntry(String name, PackResources resources, Predicate<Identifier> filter) {

      public void filterAll(Collection<Identifier> $$0) {
         if (this.filter != null) {
            $$0.removeIf(this.filter);
         }
      }

      public boolean isFiltered(Identifier $$0) {
         return this.filter != null && this.filter.test($$0);
      }
   }

   record ResourceWithSource(PackResources source, IoSupplier<InputStream> resource) {
   }
}
