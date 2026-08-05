/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;

public abstract class TagsProvider<T>
implements DataProvider {
    protected final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final CompletableFuture<Void> contentsDone = new CompletableFuture();
    private final CompletableFuture<TagLookup<T>> parentProvider;
    protected final ResourceKey<? extends Registry<T>> registryKey;
    private final Map<Identifier, TagBuilder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(PackOutput $$0, ResourceKey<? extends Registry<T>> $$1, CompletableFuture<HolderLookup.Provider> $$2) {
        this($$0, $$1, $$2, CompletableFuture.completedFuture(TagLookup.empty()));
    }

    protected TagsProvider(PackOutput $$0, ResourceKey<? extends Registry<T>> $$1, CompletableFuture<HolderLookup.Provider> $$2, CompletableFuture<TagLookup<T>> $$3) {
        this.pathProvider = $$0.createRegistryTagsPathProvider($$1);
        this.registryKey = $$1;
        this.parentProvider = $$3;
        this.lookupProvider = $$2;
    }

    @Override
    public final String getName() {
        return "Tags for " + String.valueOf(this.registryKey.identifier());
    }

    protected abstract void addTags(HolderLookup.Provider var1);

    @Override
    public CompletableFuture<?> run(CachedOutput $$02) {
        final class CombinedData<T>
        extends Record {
            final HolderLookup.Provider contents;
            final TagLookup<T> parent;

            CombinedData(HolderLookup.Provider $$0, TagLookup<T> $$1) {
                this.contents = $$0;
                this.parent = $$1;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this);
            }

            @Override
            public final boolean equals(Object $$0) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CombinedData.class, "contents;parent", "contents", "parent"}, this, $$0);
            }

            public HolderLookup.Provider contents() {
                return this.contents;
            }

            public TagLookup<T> parent() {
                return this.parent;
            }
        }
        return ((CompletableFuture)((CompletableFuture)this.createContentsProvider().thenApply($$0 -> {
            this.contentsDone.complete(null);
            return $$0;
        })).thenCombineAsync(this.parentProvider, ($$0, $$1) -> new CombinedData((HolderLookup.Provider)$$0, $$1), (Executor)Util.backgroundExecutor())).thenCompose($$12 -> {
            HolderGetter $$2 = $$12.contents.lookupOrThrow(this.registryKey);
            Predicate<Identifier> $$3 = arg_0 -> this.lambda$run$2((HolderLookup.RegistryLookup)$$2, arg_0);
            Predicate<Identifier> $$42 = $$1 -> this.builders.containsKey($$1) || $$0.parent.contains(TagKey.create(this.registryKey, $$1));
            return CompletableFuture.allOf((CompletableFuture[])this.builders.entrySet().stream().map($$4 -> {
                Identifier $$5 = (Identifier)$$4.getKey();
                TagBuilder $$6 = (TagBuilder)$$4.getValue();
                List<TagEntry> $$7 = $$6.build();
                List<TagEntry> $$8 = $$7.stream().filter($$2 -> !$$2.verifyIfPresent($$3, $$42)).toList();
                if (!$$8.isEmpty()) {
                    throw new IllegalArgumentException(String.format(Locale.ROOT, "Couldn't define tag %s as it is missing following references: %s", $$5, $$8.stream().map(Objects::toString).collect(Collectors.joining(","))));
                }
                Path $$9 = this.pathProvider.json($$5);
                return DataProvider.saveStable($$02, $$3.contents, TagFile.CODEC, new TagFile($$7, false), $$9);
            }).toArray(CompletableFuture[]::new));
        });
    }

    protected TagBuilder getOrCreateRawBuilder(TagKey<T> $$02) {
        return this.builders.computeIfAbsent($$02.location(), $$0 -> TagBuilder.create());
    }

    public CompletableFuture<TagLookup<T>> contentsGetter() {
        return this.contentsDone.thenApply($$02 -> $$0 -> Optional.ofNullable(this.builders.get($$0.location())));
    }

    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return this.lookupProvider.thenApply($$0 -> {
            this.builders.clear();
            this.addTags((HolderLookup.Provider)$$0);
            return $$0;
        });
    }

    private /* synthetic */ boolean lambda$run$2(HolderLookup.RegistryLookup $$0, Identifier $$1) {
        return $$0.get(ResourceKey.create(this.registryKey, $$1)).isPresent();
    }

    @FunctionalInterface
    public static interface TagLookup<T>
    extends Function<TagKey<T>, Optional<TagBuilder>> {
        public static <T> TagLookup<T> empty() {
            return $$0 -> Optional.empty();
        }

        default public boolean contains(TagKey<T> $$0) {
            return ((Optional)this.apply($$0)).isPresent();
        }
    }
}

