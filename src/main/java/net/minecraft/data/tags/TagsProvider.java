package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;

public abstract class TagsProvider<T> implements net.minecraft.data.DataProvider {
   protected final net.minecraft.data.PackOutput.PathProvider pathProvider;
   private final CompletableFuture<Provider> lookupProvider;
   private final CompletableFuture<Void> contentsDone = new CompletableFuture<>();
   private final CompletableFuture<TagsProvider.TagLookup<T>> parentProvider;
   protected final ResourceKey<? extends Registry<T>> registryKey;
   private final Map<Identifier, TagBuilder> builders = Maps.newLinkedHashMap();

   protected TagsProvider(net.minecraft.data.PackOutput $$0, ResourceKey<? extends Registry<T>> $$1, CompletableFuture<Provider> $$2) {
      this($$0, $$1, $$2, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()));
   }

   protected TagsProvider(
      net.minecraft.data.PackOutput $$0,
      ResourceKey<? extends Registry<T>> $$1,
      CompletableFuture<Provider> $$2,
      CompletableFuture<TagsProvider.TagLookup<T>> $$3
   ) {
      this.pathProvider = $$0.createRegistryTagsPathProvider($$1);
      this.registryKey = $$1;
      this.parentProvider = $$3;
      this.lookupProvider = $$2;
   }

   @Override
   public final String getName() {
      return "Tags for " + this.registryKey.identifier();
   }

   protected abstract void addTags(Provider var1);

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      record CombinedData<T>(Provider contents, TagsProvider.TagLookup<T> parent) {
      }

      return this.createContentsProvider()
         .thenApply($$0x -> {
            this.contentsDone.complete(null);
            return $$0x;
         })
         .thenCombineAsync(this.parentProvider, ($$0x, $$1) -> new CombinedData<>($$0x, (TagsProvider.TagLookup<T>)$$1), Util.backgroundExecutor())
         .thenCompose(
            $$1 -> {
               RegistryLookup<T> $$2 = $$1.contents.lookupOrThrow(this.registryKey);
               Predicate<Identifier> $$3 = $$1x -> $$2.get(ResourceKey.create(this.registryKey, $$1x)).isPresent();
               Predicate<Identifier> $$4 = $$1x -> this.builders.containsKey($$1x) || $$1.parent.contains(TagKey.create(this.registryKey, $$1x));
               return CompletableFuture.allOf(
                  this.builders
                     .entrySet()
                     .stream()
                     .map(
                        $$4x -> {
                           Identifier $$5 = (Identifier)$$4x.getKey();
                           TagBuilder $$6 = (TagBuilder)$$4x.getValue();
                           List<TagEntry> $$7 = $$6.build();
                           List<TagEntry> $$8 = $$7.stream().filter($$2xx -> !$$2xx.verifyIfPresent($$3, $$4)).toList();
                           if (!$$8.isEmpty()) {
                              throw new IllegalArgumentException(
                                 String.format(
                                    Locale.ROOT,
                                    "Couldn't define tag %s as it is missing following references: %s",
                                    $$5,
                                    $$8.stream().map(Objects::toString).collect(Collectors.joining(","))
                                 )
                              );
                           } else {
                              Path $$9 = this.pathProvider.json($$5);
                              return net.minecraft.data.DataProvider.saveStable($$0, $$1.contents, TagFile.CODEC, new TagFile($$7, false), $$9);
                           }
                        }
                     )
                     .toArray(CompletableFuture[]::new)
               );
            }
         );
   }

   protected TagBuilder getOrCreateRawBuilder(TagKey<T> $$0) {
      return this.builders.computeIfAbsent($$0.location(), $$0x -> TagBuilder.create());
   }

   public CompletableFuture<TagsProvider.TagLookup<T>> contentsGetter() {
      return this.contentsDone.thenApply($$0 -> $$0x -> Optional.ofNullable(this.builders.get($$0x.location())));
   }

   protected CompletableFuture<Provider> createContentsProvider() {
      return this.lookupProvider.thenApply($$0 -> {
         this.builders.clear();
         this.addTags($$0);
         return (Provider)$$0;
      });
   }

   @FunctionalInterface
   public interface TagLookup<T> extends Function<TagKey<T>, Optional<TagBuilder>> {
      static <T> TagsProvider.TagLookup<T> empty() {
         return $$0 -> Optional.empty();
      }

      default boolean contains(TagKey<T> $$0) {
         return this.apply($$0).isPresent();
      }
   }
}
