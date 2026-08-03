package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.ImmutableMap.Builder;
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
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader.LoadResult;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class MappedRegistry<T> implements net.minecraft.core.WritableRegistry<T> {
   private final ResourceKey<? extends net.minecraft.core.Registry<T>> key;
   private final ObjectList<net.minecraft.core.Holder.Reference<T>> byId = new ObjectArrayList(256);
   private final Reference2IntMap<T> toId = (Reference2IntMap<T>)Util.make(new Reference2IntOpenHashMap(), $$0x -> $$0x.defaultReturnValue(-1));
   private final Map<Identifier, net.minecraft.core.Holder.Reference<T>> byLocation = new HashMap<>();
   private final Map<ResourceKey<T>, net.minecraft.core.Holder.Reference<T>> byKey = new HashMap<>();
   private final Map<T, net.minecraft.core.Holder.Reference<T>> byValue = new IdentityHashMap<>();
   private final Map<ResourceKey<T>, net.minecraft.core.RegistrationInfo> registrationInfos = new IdentityHashMap<>();
   private Lifecycle registryLifecycle;
   private final Map<TagKey<T>, net.minecraft.core.HolderSet.Named<T>> frozenTags = new IdentityHashMap<>();
   net.minecraft.core.MappedRegistry.TagSet<T> allTags = net.minecraft.core.MappedRegistry.TagSet.unbound();
   private boolean frozen;
   @Nullable
   private Map<T, net.minecraft.core.Holder.Reference<T>> unregisteredIntrusiveHolders;

   @Override
   public Stream<net.minecraft.core.HolderSet.Named<T>> listTags() {
      return this.getTags();
   }

   public MappedRegistry(ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, Lifecycle $$1) {
      this($$0, $$1, false);
   }

   public MappedRegistry(ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, Lifecycle $$1, boolean $$2) {
      this.key = $$0;
      this.registryLifecycle = $$1;
      if ($$2) {
         this.unregisteredIntrusiveHolders = new IdentityHashMap<>();
      }
   }

   @Override
   public ResourceKey<? extends net.minecraft.core.Registry<T>> key() {
      return this.key;
   }

   @Override
   public String toString() {
      return "Registry[" + this.key + " (" + this.registryLifecycle + ")]";
   }

   private void validateWrite() {
      if (this.frozen) {
         throw new IllegalStateException("Registry is already frozen");
      }
   }

   private void validateWrite(ResourceKey<T> $$0) {
      if (this.frozen) {
         throw new IllegalStateException("Registry is already frozen (trying to add key " + $$0 + ")");
      }
   }

   @Override
   public net.minecraft.core.Holder.Reference<T> register(ResourceKey<T> $$0, T $$1, net.minecraft.core.RegistrationInfo $$2) {
      this.validateWrite($$0);
      Objects.requireNonNull($$0);
      Objects.requireNonNull($$1);
      if (this.byLocation.containsKey($$0.identifier())) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Adding duplicate key '" + $$0 + "' to registry"));
      } else if (this.byValue.containsKey($$1)) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + $$1 + "' to registry"));
      } else {
         net.minecraft.core.Holder.Reference<T> $$3;
         if (this.unregisteredIntrusiveHolders != null) {
            $$3 = this.unregisteredIntrusiveHolders.remove($$1);
            if ($$3 == null) {
               throw new AssertionError("Missing intrusive holder for " + $$0 + ":" + $$1);
            }

            $$3.bindKey($$0);
         } else {
            $$3 = this.byKey.computeIfAbsent($$0, $$0x -> net.minecraft.core.Holder.Reference.createStandAlone(this, $$0x));
         }

         this.byKey.put($$0, $$3);
         this.byLocation.put($$0.identifier(), $$3);
         this.byValue.put($$1, $$3);
         int $$5 = this.byId.size();
         this.byId.add($$3);
         this.toId.put($$1, $$5);
         this.registrationInfos.put($$0, $$2);
         this.registryLifecycle = this.registryLifecycle.add($$2.lifecycle());
         return $$3;
      }
   }

   @Nullable
   @Override
   public Identifier getKey(T $$0) {
      net.minecraft.core.Holder.Reference<T> $$1 = this.byValue.get($$0);
      return $$1 != null ? $$1.key().identifier() : null;
   }

   @Override
   public Optional<ResourceKey<T>> getResourceKey(T $$0) {
      return Optional.ofNullable(this.byValue.get($$0)).map(net.minecraft.core.Holder.Reference::key);
   }

   @Override
   public int getId(@Nullable T $$0) {
      return this.toId.getInt($$0);
   }

   @Nullable
   @Override
   public T getValue(@Nullable ResourceKey<T> $$0) {
      return getValueFromNullable(this.byKey.get($$0));
   }

   @Nullable
   @Override
   public T byId(int $$0) {
      return (T)($$0 >= 0 && $$0 < this.byId.size() ? ((net.minecraft.core.Holder.Reference)this.byId.get($$0)).value() : null);
   }

   @Override
   public Optional<net.minecraft.core.Holder.Reference<T>> get(int $$0) {
      return $$0 >= 0 && $$0 < this.byId.size() ? Optional.ofNullable((net.minecraft.core.Holder.Reference<T>)this.byId.get($$0)) : Optional.empty();
   }

   @Override
   public Optional<net.minecraft.core.Holder.Reference<T>> get(Identifier $$0) {
      return Optional.ofNullable(this.byLocation.get($$0));
   }

   @Override
   public Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> $$0) {
      return Optional.ofNullable(this.byKey.get($$0));
   }

   @Override
   public Optional<net.minecraft.core.Holder.Reference<T>> getAny() {
      return this.byId.isEmpty() ? Optional.empty() : Optional.of((net.minecraft.core.Holder.Reference<T>)this.byId.getFirst());
   }

   @Override
   public net.minecraft.core.Holder<T> wrapAsHolder(T $$0) {
      net.minecraft.core.Holder.Reference<T> $$1 = this.byValue.get($$0);
      return (net.minecraft.core.Holder<T>)($$1 != null ? $$1 : net.minecraft.core.Holder.direct($$0));
   }

   net.minecraft.core.Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> $$0) {
      return this.byKey.computeIfAbsent($$0, $$0x -> {
         if (this.unregisteredIntrusiveHolders != null) {
            throw new IllegalStateException("This registry can't create new holders without value");
         } else {
            this.validateWrite($$0x);
            return net.minecraft.core.Holder.Reference.createStandAlone(this, $$0x);
         }
      });
   }

   @Override
   public int size() {
      return this.byKey.size();
   }

   @Override
   public Optional<net.minecraft.core.RegistrationInfo> registrationInfo(ResourceKey<T> $$0) {
      return Optional.ofNullable(this.registrationInfos.get($$0));
   }

   @Override
   public Lifecycle registryLifecycle() {
      return this.registryLifecycle;
   }

   @Override
   public Iterator<T> iterator() {
      return Iterators.transform(this.byId.iterator(), net.minecraft.core.Holder::value);
   }

   @Nullable
   @Override
   public T getValue(@Nullable Identifier $$0) {
      net.minecraft.core.Holder.Reference<T> $$1 = this.byLocation.get($$0);
      return getValueFromNullable($$1);
   }

   @Nullable
   private static <T> T getValueFromNullable(@Nullable net.minecraft.core.Holder.Reference<T> $$0) {
      return $$0 != null ? $$0.value() : null;
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
   public Set<Entry<ResourceKey<T>, T>> entrySet() {
      return Collections.unmodifiableSet(Util.mapValuesLazy(this.byKey, net.minecraft.core.Holder::value).entrySet());
   }

   @Override
   public Stream<net.minecraft.core.Holder.Reference<T>> listElements() {
      return this.byId.stream();
   }

   @Override
   public Stream<net.minecraft.core.HolderSet.Named<T>> getTags() {
      return this.allTags.getTags();
   }

   net.minecraft.core.HolderSet.Named<T> getOrCreateTagForRegistration(TagKey<T> $$0) {
      return this.frozenTags.computeIfAbsent($$0, this::createTag);
   }

   private net.minecraft.core.HolderSet.Named<T> createTag(TagKey<T> $$0) {
      return new net.minecraft.core.HolderSet.Named<>(this, $$0);
   }

   @Override
   public boolean isEmpty() {
      return this.byKey.isEmpty();
   }

   @Override
   public Optional<net.minecraft.core.Holder.Reference<T>> getRandom(RandomSource $$0) {
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
   public net.minecraft.core.Registry<T> freeze() {
      if (this.frozen) {
         return this;
      } else {
         this.frozen = true;
         this.byValue.forEach(($$0x, $$1x) -> $$1x.bindValue($$0x));
         List<Identifier> $$0 = this.byKey
            .entrySet()
            .stream()
            .filter($$0x -> !((net.minecraft.core.Holder.Reference)$$0x.getValue()).isBound())
            .map($$0x -> ((ResourceKey)$$0x.getKey()).identifier())
            .sorted()
            .toList();
         if (!$$0.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + $$0);
         } else {
            if (this.unregisteredIntrusiveHolders != null) {
               if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                  throw new IllegalStateException("Some intrusive holders were not registered: " + this.unregisteredIntrusiveHolders.values());
               }

               this.unregisteredIntrusiveHolders = null;
            }

            if (this.allTags.isBound()) {
               throw new IllegalStateException("Tags already present before freezing");
            } else {
               List<Identifier> $$1 = this.frozenTags
                  .entrySet()
                  .stream()
                  .filter($$0x -> !((net.minecraft.core.HolderSet.Named)$$0x.getValue()).isBound())
                  .map($$0x -> ((TagKey)$$0x.getKey()).location())
                  .sorted()
                  .toList();
               if (!$$1.isEmpty()) {
                  throw new IllegalStateException("Unbound tags in registry " + this.key() + ": " + $$1);
               } else {
                  this.allTags = net.minecraft.core.MappedRegistry.TagSet.fromMap(this.frozenTags);
                  this.refreshTagsInHolders();
                  return this;
               }
            }
         }
      }
   }

   @Override
   public net.minecraft.core.Holder.Reference<T> createIntrusiveHolder(T $$0) {
      if (this.unregisteredIntrusiveHolders == null) {
         throw new IllegalStateException("This registry can't create intrusive holders");
      } else {
         this.validateWrite();
         return this.unregisteredIntrusiveHolders.computeIfAbsent($$0, $$0x -> net.minecraft.core.Holder.Reference.createIntrusive(this, (T)$$0x));
      }
   }

   @Override
   public Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> $$0) {
      return this.allTags.get($$0);
   }

   private net.minecraft.core.Holder.Reference<T> validateAndUnwrapTagElement(TagKey<T> $$0, net.minecraft.core.Holder<T> $$1) {
      if (!$$1.canSerializeIn(this)) {
         throw new IllegalStateException("Can't create named set " + $$0 + " containing value " + $$1 + " from outside registry " + this);
      } else if ($$1 instanceof net.minecraft.core.Holder.Reference<T> $$2) {
         return $$2;
      } else {
         throw new IllegalStateException("Found direct holder " + $$1 + " value in tag " + $$0);
      }
   }

   @Override
   public void bindTag(TagKey<T> $$0, List<net.minecraft.core.Holder<T>> $$1) {
      this.validateWrite();
      this.getOrCreateTagForRegistration($$0).bind($$1);
   }

   void refreshTagsInHolders() {
      Map<net.minecraft.core.Holder.Reference<T>, List<TagKey<T>>> $$0 = new IdentityHashMap<>();
      this.byKey.values().forEach($$1 -> $$0.put((net.minecraft.core.Holder.Reference<T>)$$1, new ArrayList<>()));
      this.allTags.forEach(($$1, $$2) -> {
         for (net.minecraft.core.Holder<T> $$3 : $$2) {
            net.minecraft.core.Holder.Reference<T> $$4 = this.validateAndUnwrapTagElement((TagKey<T>)$$1, $$3);
            $$0.get($$4).add((TagKey<T>)$$1);
         }
      });
      $$0.forEach(net.minecraft.core.Holder.Reference::bindTags);
   }

   public void bindAllTagsToEmpty() {
      this.validateWrite();
      this.frozenTags.values().forEach($$0 -> $$0.bind(List.of()));
   }

   @Override
   public net.minecraft.core.HolderGetter<T> createRegistrationLookup() {
      this.validateWrite();
      return new net.minecraft.core.HolderGetter<T>() {
         @Override
         public Optional<net.minecraft.core.Holder.Reference<T>> get(ResourceKey<T> $$0) {
            return Optional.of(this.getOrThrow($$0));
         }

         @Override
         public net.minecraft.core.Holder.Reference<T> getOrThrow(ResourceKey<T> $$0) {
            return MappedRegistry.this.getOrCreateHolderOrThrow($$0);
         }

         @Override
         public Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> $$0) {
            return Optional.of(this.getOrThrow($$0));
         }

         @Override
         public net.minecraft.core.HolderSet.Named<T> getOrThrow(TagKey<T> $$0) {
            return MappedRegistry.this.getOrCreateTagForRegistration($$0);
         }
      };
   }

   @Override
   public net.minecraft.core.Registry.PendingTags<T> prepareTagReload(LoadResult<T> $$0) {
      if (!this.frozen) {
         throw new IllegalStateException("Invalid method used for tag loading");
      } else {
         Builder<TagKey<T>, net.minecraft.core.HolderSet.Named<T>> $$1 = ImmutableMap.builder();
         final Map<TagKey<T>, List<net.minecraft.core.Holder<T>>> $$2 = new HashMap<>();
         $$0.tags().forEach(($$2x, $$3x) -> {
            net.minecraft.core.HolderSet.Named<T> $$4x = this.frozenTags.get($$2x);
            if ($$4x == null) {
               $$4x = this.createTag($$2x);
            }

            $$1.put($$2x, $$4x);
            $$2.put($$2x, List.copyOf($$3x));
         });
         final ImmutableMap<TagKey<T>, net.minecraft.core.HolderSet.Named<T>> $$3 = $$1.build();
         final net.minecraft.core.HolderLookup.RegistryLookup<T> $$4 = new net.minecraft.core.HolderLookup.RegistryLookup.Delegate<T>() {
            @Override
            public net.minecraft.core.HolderLookup.RegistryLookup<T> parent() {
               return MappedRegistry.this;
            }

            @Override
            public Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> $$0) {
               return Optional.ofNullable((net.minecraft.core.HolderSet.Named<T>)$$3.get($$0));
            }

            @Override
            public Stream<net.minecraft.core.HolderSet.Named<T>> listTags() {
               return $$3.values().stream();
            }
         };
         return new net.minecraft.core.Registry.PendingTags<T>() {
            @Override
            public ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key() {
               return MappedRegistry.this.key();
            }

            @Override
            public int size() {
               return $$2.size();
            }

            @Override
            public net.minecraft.core.HolderLookup.RegistryLookup<T> lookup() {
               return $$4;
            }

            @Override
            public void apply() {
               $$3.forEach(($$1x, $$2xx) -> {
                  List<net.minecraft.core.Holder<T>> $$3xx = $$2.getOrDefault($$1x, List.of());
                  $$2xx.bind($$3xx);
               });
               MappedRegistry.this.allTags = net.minecraft.core.MappedRegistry.TagSet.fromMap($$3);
               MappedRegistry.this.refreshTagsInHolders();
            }
         };
      }
   }

   interface TagSet<T> {
      static <T> net.minecraft.core.MappedRegistry.TagSet<T> unbound() {
         return new net.minecraft.core.MappedRegistry.TagSet<T>() {
            @Override
            public boolean isBound() {
               return false;
            }

            @Override
            public Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> $$0) {
               throw new IllegalStateException("Tags not bound, trying to access " + $$0);
            }

            @Override
            public void forEach(BiConsumer<? super TagKey<T>, ? super net.minecraft.core.HolderSet.Named<T>> $$0) {
               throw new IllegalStateException("Tags not bound");
            }

            @Override
            public Stream<net.minecraft.core.HolderSet.Named<T>> getTags() {
               throw new IllegalStateException("Tags not bound");
            }
         };
      }

      static <T> net.minecraft.core.MappedRegistry.TagSet<T> fromMap(final Map<TagKey<T>, net.minecraft.core.HolderSet.Named<T>> $$0) {
         return new net.minecraft.core.MappedRegistry.TagSet<T>() {
            @Override
            public boolean isBound() {
               return true;
            }

            @Override
            public Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> $$0x) {
               return Optional.ofNullable($$0.get($$0));
            }

            @Override
            public void forEach(BiConsumer<? super TagKey<T>, ? super net.minecraft.core.HolderSet.Named<T>> $$0x) {
               $$0.forEach($$0);
            }

            @Override
            public Stream<net.minecraft.core.HolderSet.Named<T>> getTags() {
               return $$0.values().stream();
            }
         };
      }

      boolean isBound();

      Optional<net.minecraft.core.HolderSet.Named<T>> get(TagKey<T> var1);

      void forEach(BiConsumer<? super TagKey<T>, ? super net.minecraft.core.HolderSet.Named<T>> var1);

      Stream<net.minecraft.core.HolderSet.Named<T>> getTags();
   }
}
