package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
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
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps.RegistryInfo;
import net.minecraft.resources.RegistryOps.RegistryInfoLookup;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class RegistrySetBuilder {
   private final List<net.minecraft.core.RegistrySetBuilder.RegistryStub<?>> entries = new ArrayList<>();

   static <T> net.minecraft.core.HolderGetter<T> wrapContextLookup(final net.minecraft.core.HolderLookup.RegistryLookup<T> $$0) {
      return new net.minecraft.core.RegistrySetBuilder.EmptyTagLookup<T>($$0) {
         @Override
         public Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> $$0x) {
            return $$0.get($$0);
         }
      };
   }

   static <T> net.minecraft.core.HolderLookup.RegistryLookup<T> lookupFromMap(
      final ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0,
      final Lifecycle $$1,
      net.minecraft.core.HolderOwner<T> $$2,
      final Map<ResourceKey<T>, net.minecraft.core.Holder.Reference<T>> $$3
   ) {
      return new net.minecraft.core.RegistrySetBuilder.EmptyTagRegistryLookup<T>($$2) {
         @Override
         public ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key() {
            return $$0;
         }

         @Override
         public Lifecycle registryLifecycle() {
            return $$1;
         }

         @Override
         public Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> $$0x) {
            return Optional.ofNullable($$3.get($$0));
         }

         @Override
         public Stream<net.minecraft.core.Holder.Reference<T>> listElements() {
            return $$3.values().stream();
         }
      };
   }

   public <T> net.minecraft.core.RegistrySetBuilder add(
      ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, Lifecycle $$1, net.minecraft.core.RegistrySetBuilder.RegistryBootstrap<T> $$2
   ) {
      this.entries.add(new net.minecraft.core.RegistrySetBuilder.RegistryStub<>($$0, $$1, $$2));
      return this;
   }

   public <T> net.minecraft.core.RegistrySetBuilder add(
      ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, net.minecraft.core.RegistrySetBuilder.RegistryBootstrap<T> $$1
   ) {
      return this.add($$0, Lifecycle.stable(), $$1);
   }

   private net.minecraft.core.RegistrySetBuilder.BuildState createState(net.minecraft.core.RegistryAccess $$0) {
      net.minecraft.core.RegistrySetBuilder.BuildState $$1 = net.minecraft.core.RegistrySetBuilder.BuildState.create(
         $$0, this.entries.stream().map(net.minecraft.core.RegistrySetBuilder.RegistryStub::key)
      );
      this.entries.forEach($$1x -> $$1x.apply($$1));
      return $$1;
   }

   private static net.minecraft.core.HolderLookup.Provider buildProviderWithContext(
      net.minecraft.core.RegistrySetBuilder.UniversalOwner $$0,
      net.minecraft.core.RegistryAccess $$1,
      Stream<net.minecraft.core.HolderLookup.RegistryLookup<?>> $$2
   ) {
      record Entry<T>(net.minecraft.core.HolderLookup.RegistryLookup<T> lookup, RegistryInfo<T> opsInfo) {
         public static <T> Entry<T> createForContextRegistry(net.minecraft.core.HolderLookup.RegistryLookup<T> $$0) {
            return new Entry<>(new net.minecraft.core.RegistrySetBuilder.EmptyTagLookupWrapper<>($$0, $$0), RegistryInfo.fromRegistryLookup($$0));
         }

         public static <T> Entry<T> createForNewRegistry(
            net.minecraft.core.RegistrySetBuilder.UniversalOwner $$0, net.minecraft.core.HolderLookup.RegistryLookup<T> $$1
         ) {
            return new Entry<>(
               new net.minecraft.core.RegistrySetBuilder.EmptyTagLookupWrapper<>($$0.cast(), $$1), new RegistryInfo($$0.cast(), $$1, $$1.registryLifecycle())
            );
         }
      }

      final Map<ResourceKey<? extends net.minecraft.core.Registry<?>>, Entry<?>> $$3 = new HashMap<>();
      $$1.registries().forEach($$1x -> $$3.put($$1x.key(), Entry.createForContextRegistry($$1x.value())));
      $$2.forEach($$2x -> $$3.put($$2x.key(), Entry.createForNewRegistry($$0, $$2x)));
      return new net.minecraft.core.HolderLookup.Provider() {
         @Override
         public Stream<ResourceKey<? extends net.minecraft.core.Registry<?>>> listRegistryKeys() {
            return $$3.keySet().stream();
         }

         <T> Optional<Entry<T>> getEntry(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0) {
            return Optional.ofNullable((Entry<T>)$$3.get($$0));
         }

         @Override
         public <T> Optional<net.minecraft.core.HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0) {
            return this.getEntry($$0).map(Entry::lookup);
         }

         @Override
         public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> $$0) {
            return RegistryOps.create($$0, new RegistryInfoLookup() {
               public <T> Optional<RegistryInfo<T>> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$0) {
                  return getEntry($$0).map(Entry::opsInfo);
               }
            });
         }
      };
   }

   public net.minecraft.core.HolderLookup.Provider build(net.minecraft.core.RegistryAccess $$0) {
      net.minecraft.core.RegistrySetBuilder.BuildState $$1 = this.createState($$0);
      Stream<net.minecraft.core.HolderLookup.RegistryLookup<?>> $$2 = this.entries
         .stream()
         .map($$1x -> $$1x.collectRegisteredValues($$1).buildAsLookup($$1.owner));
      net.minecraft.core.HolderLookup.Provider $$3 = buildProviderWithContext($$1.owner, $$0, $$2);
      $$1.reportNotCollectedHolders();
      $$1.reportUnclaimedRegisteredValues();
      $$1.throwOnError();
      return $$3;
   }

   private net.minecraft.core.HolderLookup.Provider createLazyFullPatchedRegistries(
      net.minecraft.core.RegistryAccess $$0,
      net.minecraft.core.HolderLookup.Provider $$1,
      net.minecraft.core.Cloner.Factory $$2,
      Map<ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.core.RegistrySetBuilder.RegistryContents<?>> $$3,
      net.minecraft.core.HolderLookup.Provider $$4
   ) {
      net.minecraft.core.RegistrySetBuilder.UniversalOwner $$5 = new net.minecraft.core.RegistrySetBuilder.UniversalOwner();
      MutableObject<net.minecraft.core.HolderLookup.Provider> $$6 = new MutableObject();
      List<net.minecraft.core.HolderLookup.RegistryLookup<?>> $$7 = $$3.keySet()
         .stream()
         .map($$5x -> this.createLazyFullPatchedRegistries($$5, $$2, $$5x, $$4, $$1, $$6))
         .collect(Collectors.toUnmodifiableList());
      net.minecraft.core.HolderLookup.Provider $$8 = buildProviderWithContext($$5, $$0, $$7.stream());
      $$6.setValue($$8);
      return $$8;
   }

   private <T> net.minecraft.core.HolderLookup.RegistryLookup<T> createLazyFullPatchedRegistries(
      net.minecraft.core.HolderOwner<T> $$0,
      net.minecraft.core.Cloner.Factory $$1,
      ResourceKey<? extends net.minecraft.core.Registry<? extends T>> $$2,
      net.minecraft.core.HolderLookup.Provider $$3,
      net.minecraft.core.HolderLookup.Provider $$4,
      MutableObject<net.minecraft.core.HolderLookup.Provider> $$5
   ) {
      net.minecraft.core.Cloner<T> $$6 = $$1.cloner($$2);
      if ($$6 == null) {
         throw new NullPointerException("No cloner for " + $$2.identifier());
      } else {
         Map<ResourceKey<T>, net.minecraft.core.Holder.Reference<T>> $$7 = new HashMap<>();
         net.minecraft.core.HolderLookup.RegistryLookup<T> $$8 = $$3.lookupOrThrow($$2);
         $$8.listElements().forEach($$5x -> {
            ResourceKey<T> $$6x = $$5x.key();
            net.minecraft.core.RegistrySetBuilder.LazyHolder<T> $$7x = new net.minecraft.core.RegistrySetBuilder.LazyHolder<>($$0, $$6x);
            $$7x.supplier = () -> $$6.clone((T)$$5x.value(), $$3, (net.minecraft.core.HolderLookup.Provider)$$5.get());
            $$7.put($$6x, $$7x);
         });
         net.minecraft.core.HolderLookup.RegistryLookup<T> $$9 = $$4.lookupOrThrow($$2);
         $$9.listElements().forEach($$5x -> {
            ResourceKey<T> $$6x = $$5x.key();
            $$7.computeIfAbsent($$6x, $$6xx -> {
               net.minecraft.core.RegistrySetBuilder.LazyHolder<T> $$7x = new net.minecraft.core.RegistrySetBuilder.LazyHolder<>($$0, $$6x);
               $$7x.supplier = () -> $$6.clone((T)$$5x.value(), $$4, (net.minecraft.core.HolderLookup.Provider)$$5.get());
               return $$7x;
            });
         });
         Lifecycle $$10 = $$8.registryLifecycle().add($$9.registryLifecycle());
         return lookupFromMap($$2, $$10, $$0, $$7);
      }
   }

   public net.minecraft.core.RegistrySetBuilder.PatchedRegistries buildPatch(
      net.minecraft.core.RegistryAccess $$0, net.minecraft.core.HolderLookup.Provider $$1, net.minecraft.core.Cloner.Factory $$2
   ) {
      net.minecraft.core.RegistrySetBuilder.BuildState $$3 = this.createState($$0);
      Map<ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.core.RegistrySetBuilder.RegistryContents<?>> $$4 = new HashMap<>();
      this.entries.stream().map($$1x -> $$1x.collectRegisteredValues($$3)).forEach($$1x -> $$4.put($$1x.key, $$1x));
      Set<ResourceKey<? extends net.minecraft.core.Registry<?>>> $$5 = $$0.listRegistryKeys().collect(Collectors.toUnmodifiableSet());
      $$1.listRegistryKeys()
         .filter($$1x -> !$$5.contains($$1x))
         .forEach($$1x -> $$4.putIfAbsent($$1x, new net.minecraft.core.RegistrySetBuilder.RegistryContents($$1x, Lifecycle.stable(), Map.of())));
      Stream<net.minecraft.core.HolderLookup.RegistryLookup<?>> $$6 = $$4.values().stream().map($$1x -> $$1x.buildAsLookup($$3.owner));
      net.minecraft.core.HolderLookup.Provider $$7 = buildProviderWithContext($$3.owner, $$0, $$6);
      $$3.reportUnclaimedRegisteredValues();
      $$3.throwOnError();
      net.minecraft.core.HolderLookup.Provider $$8 = this.createLazyFullPatchedRegistries($$0, $$1, $$2, $$4, $$7);
      return new net.minecraft.core.RegistrySetBuilder.PatchedRegistries($$8, $$7);
   }

   record BuildState(
      net.minecraft.core.RegistrySetBuilder.UniversalOwner owner,
      net.minecraft.core.RegistrySetBuilder.UniversalLookup lookup,
      Map<Identifier, net.minecraft.core.HolderGetter<?>> registries,
      Map<ResourceKey<?>, net.minecraft.core.RegistrySetBuilder.RegisteredValue<?>> registeredValues,
      List<RuntimeException> errors
   ) {

      public static net.minecraft.core.RegistrySetBuilder.BuildState create(
         net.minecraft.core.RegistryAccess $$0, Stream<ResourceKey<? extends net.minecraft.core.Registry<?>>> $$1
      ) {
         net.minecraft.core.RegistrySetBuilder.UniversalOwner $$2 = new net.minecraft.core.RegistrySetBuilder.UniversalOwner();
         List<RuntimeException> $$3 = new ArrayList<>();
         net.minecraft.core.RegistrySetBuilder.UniversalLookup $$4 = new net.minecraft.core.RegistrySetBuilder.UniversalLookup($$2);
         Builder<Identifier, net.minecraft.core.HolderGetter<?>> $$5 = ImmutableMap.builder();
         $$0.registries().forEach($$1x -> $$5.put($$1x.key().identifier(), net.minecraft.core.RegistrySetBuilder.wrapContextLookup($$1x.value())));
         $$1.forEach($$2x -> $$5.put($$2x.identifier(), $$4));
         return new net.minecraft.core.RegistrySetBuilder.BuildState($$2, $$4, $$5.build(), new HashMap<>(), $$3);
      }

      public <T> BootstrapContext<T> bootstrapContext() {
         return new BootstrapContext<T>() {
            public net.minecraft.core.Holder.Reference<T> register(ResourceKey<T> $$0, T $$1, Lifecycle $$2) {
               net.minecraft.core.RegistrySetBuilder.RegisteredValue<?> $$3 = BuildState.this.registeredValues
                  .put($$0, new net.minecraft.core.RegistrySetBuilder.RegisteredValue($$1, $$2));
               if ($$3 != null) {
                  BuildState.this.errors.add(new IllegalStateException("Duplicate registration for " + $$0 + ", new=" + $$1 + ", old=" + $$3.value));
               }

               return BuildState.this.lookup.getOrCreate($$0);
            }

            public <S> net.minecraft.core.HolderGetter<S> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends S>> $$0) {
               return (net.minecraft.core.HolderGetter<S>)BuildState.this.registries.getOrDefault($$0.identifier(), BuildState.this.lookup);
            }
         };
      }

      public void reportUnclaimedRegisteredValues() {
         this.registeredValues.forEach(($$0, $$1) -> this.errors.add(new IllegalStateException("Orpaned value " + $$1.value + " for key " + $$0)));
      }

      public void reportNotCollectedHolders() {
         for (ResourceKey<Object> $$0 : this.lookup.holders.keySet()) {
            this.errors.add(new IllegalStateException("Unreferenced key: " + $$0));
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
   }

   abstract static class EmptyTagLookup<T> implements net.minecraft.core.HolderGetter<T> {
      protected final net.minecraft.core.HolderOwner<T> owner;

      protected EmptyTagLookup(net.minecraft.core.HolderOwner<T> $$0) {
         this.owner = $$0;
      }

      @Override
      public Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> $$0) {
         return Optional.of(net.minecraft.core.HolderSet.emptyNamed(this.owner, $$0));
      }
   }

   static class EmptyTagLookupWrapper<T>
      extends net.minecraft.core.RegistrySetBuilder.EmptyTagRegistryLookup<T>
      implements net.minecraft.core.HolderLookup.RegistryLookup.Delegate<T> {
      private final net.minecraft.core.HolderLookup.RegistryLookup<T> parent;

      EmptyTagLookupWrapper(net.minecraft.core.HolderOwner<T> $$0, net.minecraft.core.HolderLookup.RegistryLookup<T> $$1) {
         super($$0);
         this.parent = $$1;
      }

      @Override
      public net.minecraft.core.HolderLookup.RegistryLookup<T> parent() {
         return this.parent;
      }
   }

   abstract static class EmptyTagRegistryLookup<T>
      extends net.minecraft.core.RegistrySetBuilder.EmptyTagLookup<T>
      implements net.minecraft.core.HolderLookup.RegistryLookup<T> {
      protected EmptyTagRegistryLookup(net.minecraft.core.HolderOwner<T> $$0) {
         super($$0);
      }

      @Override
      public Stream<net.minecraft.core.HolderSet.Named<T>> listTags() {
         throw new UnsupportedOperationException("Tags are not available in datagen");
      }
   }

   static class LazyHolder<T> extends net.minecraft.core.Holder.Reference<T> {
      @Nullable
      Supplier<T> supplier;

      protected LazyHolder(net.minecraft.core.HolderOwner<T> $$0, @Nullable ResourceKey<T> $$1) {
         super(net.minecraft.core.Holder.Reference.Type.STAND_ALONE, $$0, $$1, null);
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

   public record PatchedRegistries(net.minecraft.core.HolderLookup.Provider full, net.minecraft.core.HolderLookup.Provider patches) {
   }

   record RegisteredValue<T>(T value, Lifecycle lifecycle) {
   }

   @FunctionalInterface
   public interface RegistryBootstrap<T> {
      void run(BootstrapContext<T> var1);
   }

   record RegistryContents<T>(
      ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key,
      Lifecycle lifecycle,
      Map<ResourceKey<T>, net.minecraft.core.RegistrySetBuilder.ValueAndHolder<T>> values
   ) {

      public net.minecraft.core.HolderLookup.RegistryLookup<T> buildAsLookup(net.minecraft.core.RegistrySetBuilder.UniversalOwner $$0) {
         Map<ResourceKey<T>, net.minecraft.core.Holder.Reference<T>> $$1 = this.values
            .entrySet()
            .stream()
            .collect(
               Collectors.toUnmodifiableMap(
                  java.util.Map.Entry::getKey,
                  $$1x -> {
                     net.minecraft.core.RegistrySetBuilder.ValueAndHolder<T> $$2 = (net.minecraft.core.RegistrySetBuilder.ValueAndHolder<T>)$$1x.getValue();
                     net.minecraft.core.Holder.Reference<T> $$3 = $$2.holder()
                        .orElseGet(() -> net.minecraft.core.Holder.Reference.createStandAlone($$0.cast(), (ResourceKey<T>)$$1x.getKey()));
                     $$3.bindValue($$2.value().value());
                     return $$3;
                  }
               )
            );
         return net.minecraft.core.RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, $$0.cast(), $$1);
      }
   }

   record RegistryStub<T>(
      ResourceKey<? extends net.minecraft.core.Registry<T>> key, Lifecycle lifecycle, net.minecraft.core.RegistrySetBuilder.RegistryBootstrap<T> bootstrap
   ) {
      void apply(net.minecraft.core.RegistrySetBuilder.BuildState $$0) {
         this.bootstrap.run($$0.bootstrapContext());
      }

      public net.minecraft.core.RegistrySetBuilder.RegistryContents<T> collectRegisteredValues(net.minecraft.core.RegistrySetBuilder.BuildState $$0) {
         Map<ResourceKey<T>, net.minecraft.core.RegistrySetBuilder.ValueAndHolder<T>> $$1 = new HashMap<>();
         Iterator<java.util.Map.Entry<ResourceKey<?>, net.minecraft.core.RegistrySetBuilder.RegisteredValue<?>>> $$2 = $$0.registeredValues
            .entrySet()
            .iterator();

         while ($$2.hasNext()) {
            java.util.Map.Entry<ResourceKey<?>, net.minecraft.core.RegistrySetBuilder.RegisteredValue<?>> $$3 = $$2.next();
            ResourceKey<?> $$4 = $$3.getKey();
            if ($$4.isFor(this.key)) {
               net.minecraft.core.RegistrySetBuilder.RegisteredValue<T> $$6 = (net.minecraft.core.RegistrySetBuilder.RegisteredValue<T>)$$3.getValue();
               net.minecraft.core.Holder.Reference<T> $$7 = (net.minecraft.core.Holder.Reference<T>)$$0.lookup.holders.remove($$4);
               $$1.put((ResourceKey<T>)$$4, new net.minecraft.core.RegistrySetBuilder.ValueAndHolder<>($$6, Optional.ofNullable($$7)));
               $$2.remove();
            }
         }

         return new net.minecraft.core.RegistrySetBuilder.RegistryContents<>(this.key, this.lifecycle, $$1);
      }
   }

   static class UniversalLookup extends net.minecraft.core.RegistrySetBuilder.EmptyTagLookup<Object> {
      final Map<ResourceKey<Object>, net.minecraft.core.Holder.Reference<Object>> holders = new HashMap<>();

      public UniversalLookup(net.minecraft.core.HolderOwner<Object> $$0) {
         super($$0);
      }

      @Override
      public Optional<net.minecraft.core.Holder.Reference<Object>> get(ResourceKey<Object> $$0) {
         return Optional.of(this.getOrCreate($$0));
      }

      <T> net.minecraft.core.Holder.Reference<T> getOrCreate(ResourceKey<T> $$0) {
         return (net.minecraft.core.Holder.Reference<T>)this.holders
            .computeIfAbsent($$0, $$0x -> net.minecraft.core.Holder.Reference.createStandAlone(this.owner, $$0x));
      }
   }

   static class UniversalOwner implements net.minecraft.core.HolderOwner<Object> {
      public <T> net.minecraft.core.HolderOwner<T> cast() {
         return this;
      }
   }

   record ValueAndHolder<T>(net.minecraft.core.RegistrySetBuilder.RegisteredValue<T> value, Optional<net.minecraft.core.Holder.Reference<T>> holder) {
   }
}
