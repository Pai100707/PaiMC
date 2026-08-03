package net.minecraft.tags;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.StrictJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TagLoader<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   final net.minecraft.tags.TagLoader.ElementLookup<T> elementLookup;
   private final String directory;

   public TagLoader(net.minecraft.tags.TagLoader.ElementLookup<T> $$0, String $$1) {
      this.elementLookup = $$0;
      this.directory = $$1;
   }

   public Map<Identifier, List<net.minecraft.tags.TagLoader.EntryWithSource>> load(ResourceManager $$0) {
      Map<Identifier, List<net.minecraft.tags.TagLoader.EntryWithSource>> $$1 = new HashMap<>();
      FileToIdConverter $$2 = FileToIdConverter.json(this.directory);

      for (Entry<Identifier, List<Resource>> $$3 : $$2.listMatchingResourceStacks($$0).entrySet()) {
         Identifier $$4 = $$3.getKey();
         Identifier $$5 = $$2.fileToId($$4);

         for (Resource $$6 : $$3.getValue()) {
            try (Reader $$7 = $$6.openAsReader()) {
               JsonElement $$8 = StrictJsonParser.parse($$7);
               List<net.minecraft.tags.TagLoader.EntryWithSource> $$9 = $$1.computeIfAbsent($$5, $$0x -> new ArrayList<>());
               net.minecraft.tags.TagFile $$10 = (net.minecraft.tags.TagFile)net.minecraft.tags.TagFile.CODEC
                  .parse(new Dynamic(JsonOps.INSTANCE, $$8))
                  .getOrThrow();
               if ($$10.replace()) {
                  $$9.clear();
               }

               String $$11 = $$6.sourcePackId();
               $$10.entries().forEach($$2x -> $$9.add(new net.minecraft.tags.TagLoader.EntryWithSource($$2x, $$11)));
            } catch (Exception var17) {
               LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{$$5, $$4, $$6.sourcePackId(), var17});
            }
         }
      }

      return $$1;
   }

   private Either<List<net.minecraft.tags.TagLoader.EntryWithSource>, List<T>> tryBuildTag(
      net.minecraft.tags.TagEntry.Lookup<T> $$0, List<net.minecraft.tags.TagLoader.EntryWithSource> $$1
   ) {
      SequencedSet<T> $$2 = new LinkedHashSet<>();
      List<net.minecraft.tags.TagLoader.EntryWithSource> $$3 = new ArrayList<>();

      for (net.minecraft.tags.TagLoader.EntryWithSource $$4 : $$1) {
         if (!$$4.entry().build($$0, $$2::add)) {
            $$3.add($$4);
         }
      }

      return $$3.isEmpty() ? Either.right(List.copyOf($$2)) : Either.left($$3);
   }

   public Map<Identifier, List<T>> build(Map<Identifier, List<net.minecraft.tags.TagLoader.EntryWithSource>> $$0) {
      final Map<Identifier, List<T>> $$1 = new HashMap<>();
      net.minecraft.tags.TagEntry.Lookup<T> $$2 = new net.minecraft.tags.TagEntry.Lookup<T>() {
         @Nullable
         @Override
         public T element(Identifier $$0, boolean $$1x) {
            return (T)TagLoader.this.elementLookup.get($$0, $$1).orElse(null);
         }

         @Nullable
         @Override
         public Collection<T> tag(Identifier $$0) {
            return $$1.get($$0);
         }
      };
      DependencySorter<Identifier, net.minecraft.tags.TagLoader.SortingEntry> $$3 = new DependencySorter();
      $$0.forEach(($$1x, $$2x) -> $$3.addEntry($$1x, new net.minecraft.tags.TagLoader.SortingEntry($$2x)));
      $$3.orderByDependencies(
         ($$2x, $$3x) -> this.tryBuildTag($$2, $$3x.entries)
            .ifLeft(
               $$1xx -> LOGGER.error(
                  "Couldn't load tag {} as it is missing following references: {}",
                  $$2x,
                  $$1xx.stream().map(Objects::toString).collect(Collectors.joining(", "))
               )
            )
            .ifRight($$2xx -> $$1.put($$2x, $$2xx))
      );
      return $$1;
   }

   public static <T> void loadTagsFromNetwork(net.minecraft.tags.TagNetworkSerialization.NetworkPayload $$0, WritableRegistry<T> $$1) {
      $$0.resolve($$1).tags.forEach($$1::bindTag);
   }

   public static List<PendingTags<?>> loadTagsForExistingRegistries(ResourceManager $$0, RegistryAccess $$1) {
      return $$1.registries().map($$1x -> loadPendingTags($$0, $$1x.value())).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
   }

   public static <T> void loadTagsForRegistry(ResourceManager $$0, WritableRegistry<T> $$1) {
      ResourceKey<? extends Registry<T>> $$2 = $$1.key();
      net.minecraft.tags.TagLoader<Holder<T>> $$3 = new net.minecraft.tags.TagLoader<>(
         net.minecraft.tags.TagLoader.ElementLookup.fromWritableRegistry($$1), Registries.tagsDirPath($$2)
      );
      $$3.build($$3.load($$0)).forEach(($$2x, $$3x) -> $$1.bindTag(net.minecraft.tags.TagKey.create($$2, $$2x), $$3x));
   }

   private static <T> Map<net.minecraft.tags.TagKey<T>, List<Holder<T>>> wrapTags(ResourceKey<? extends Registry<T>> $$0, Map<Identifier, List<Holder<T>>> $$1) {
      return $$1.entrySet()
         .stream()
         .collect(Collectors.toUnmodifiableMap($$1x -> net.minecraft.tags.TagKey.create($$0, (Identifier)$$1x.getKey()), Entry::getValue));
   }

   private static <T> Optional<PendingTags<T>> loadPendingTags(ResourceManager $$0, Registry<T> $$1) {
      ResourceKey<? extends Registry<T>> $$2 = $$1.key();
      net.minecraft.tags.TagLoader<Holder<T>> $$3 = new net.minecraft.tags.TagLoader<>(
         (net.minecraft.tags.TagLoader.ElementLookup<Holder<T>>)net.minecraft.tags.TagLoader.ElementLookup.fromFrozenRegistry($$1), Registries.tagsDirPath($$2)
      );
      net.minecraft.tags.TagLoader.LoadResult<T> $$4 = new net.minecraft.tags.TagLoader.LoadResult<>($$2, wrapTags($$1.key(), $$3.build($$3.load($$0))));
      return $$4.tags().isEmpty() ? Optional.empty() : Optional.of($$1.prepareTagReload($$4));
   }

   public static List<RegistryLookup<?>> buildUpdatedLookups(Frozen $$0, List<PendingTags<?>> $$1) {
      List<RegistryLookup<?>> $$2 = new ArrayList<>();
      $$0.registries().forEach($$2x -> {
         PendingTags<?> $$3 = findTagsForRegistry($$1, $$2x.key());
         $$2.add((RegistryLookup<?>)($$3 != null ? $$3.lookup() : $$2x.value()));
      });
      return $$2;
   }

   @Nullable
   private static PendingTags<?> findTagsForRegistry(List<PendingTags<?>> $$0, ResourceKey<? extends Registry<?>> $$1) {
      for (PendingTags<?> $$2 : $$0) {
         if ($$2.key() == $$1) {
            return $$2;
         }
      }

      return null;
   }

   public interface ElementLookup<T> {
      Optional<? extends T> get(Identifier var1, boolean var2);

      static <T> net.minecraft.tags.TagLoader.ElementLookup<? extends Holder<T>> fromFrozenRegistry(Registry<T> $$0) {
         return ($$1, $$2) -> $$0.get($$1);
      }

      static <T> net.minecraft.tags.TagLoader.ElementLookup<Holder<T>> fromWritableRegistry(WritableRegistry<T> $$0) {
         HolderGetter<T> $$1 = $$0.createRegistrationLookup();
         return ($$2, $$3) -> ($$3 ? $$1 : $$0).get(ResourceKey.create($$0.key(), $$2));
      }
   }

   public record EntryWithSource(net.minecraft.tags.TagEntry entry, String source) {

      @Override
      public String toString() {
         return this.entry + " (from " + this.source + ")";
      }
   }

   public record LoadResult<T>(ResourceKey<? extends Registry<T>> key, Map<net.minecraft.tags.TagKey<T>, List<Holder<T>>> tags) {
   }

   record SortingEntry(List<net.minecraft.tags.TagLoader.EntryWithSource> entries) implements net.minecraft.util.DependencySorter.Entry<Identifier> {

      public void visitRequiredDependencies(Consumer<Identifier> $$0) {
         this.entries.forEach($$1 -> $$1.entry.visitRequiredDependencies($$0));
      }

      public void visitOptionalDependencies(Consumer<Identifier> $$0) {
         this.entries.forEach($$1 -> $$1.entry.visitOptionalDependencies($$0));
      }
   }
}
