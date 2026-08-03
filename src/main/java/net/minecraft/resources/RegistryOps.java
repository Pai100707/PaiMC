package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends net.minecraft.resources.DelegatingOps<T> {
   private final net.minecraft.resources.RegistryOps.RegistryInfoLookup lookupProvider;

   public static <T> net.minecraft.resources.RegistryOps<T> create(DynamicOps<T> $$0, Provider $$1) {
      return create($$0, new net.minecraft.resources.RegistryOps.HolderLookupAdapter($$1));
   }

   public static <T> net.minecraft.resources.RegistryOps<T> create(DynamicOps<T> $$0, net.minecraft.resources.RegistryOps.RegistryInfoLookup $$1) {
      return new net.minecraft.resources.RegistryOps<>($$0, $$1);
   }

   public static <T> Dynamic<T> injectRegistryContext(Dynamic<T> $$0, Provider $$1) {
      return new Dynamic($$1.createSerializationContext($$0.getOps()), $$0.getValue());
   }

   private RegistryOps(DynamicOps<T> $$0, net.minecraft.resources.RegistryOps.RegistryInfoLookup $$1) {
      super($$0);
      this.lookupProvider = $$1;
   }

   public <U> net.minecraft.resources.RegistryOps<U> withParent(DynamicOps<U> $$0) {
      return (net.minecraft.resources.RegistryOps<U>)($$0 == this.delegate
         ? this
         : new net.minecraft.resources.RegistryOps((DynamicOps<T>)$$0, this.lookupProvider));
   }

   public <E> Optional<HolderOwner<E>> owner(net.minecraft.resources.ResourceKey<? extends Registry<? extends E>> $$0) {
      return this.lookupProvider.lookup($$0).map(net.minecraft.resources.RegistryOps.RegistryInfo::owner);
   }

   public <E> Optional<HolderGetter<E>> getter(net.minecraft.resources.ResourceKey<? extends Registry<? extends E>> $$0) {
      return this.lookupProvider.lookup($$0).map(net.minecraft.resources.RegistryOps.RegistryInfo::getter);
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if ($$0 != null && this.getClass() == $$0.getClass()) {
         net.minecraft.resources.RegistryOps<?> $$1 = (net.minecraft.resources.RegistryOps<?>)$$0;
         return this.delegate.equals($$1.delegate) && this.lookupProvider.equals($$1.lookupProvider);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.delegate.hashCode() * 31 + this.lookupProvider.hashCode();
   }

   public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(net.minecraft.resources.ResourceKey<? extends Registry<? extends E>> $$0) {
      return ExtraCodecs.retrieveContext(
            $$1 -> $$1 instanceof net.minecraft.resources.RegistryOps<?> $$2
               ? $$2.lookupProvider
                  .lookup($$0)
                  .map($$0xx -> DataResult.success($$0xx.getter(), $$0xx.elementsLifecycle()))
                  .orElseGet(() -> DataResult.error(() -> "Unknown registry: " + $$0))
               : DataResult.error(() -> "Not a registry ops")
         )
         .forGetter($$0x -> null);
   }

   public static <E, O> RecordCodecBuilder<O, Reference<E>> retrieveElement(net.minecraft.resources.ResourceKey<E> $$0) {
      net.minecraft.resources.ResourceKey<? extends Registry<E>> $$1 = net.minecraft.resources.ResourceKey.createRegistryKey($$0.registry());
      return ExtraCodecs.retrieveContext(
            $$2 -> $$2 instanceof net.minecraft.resources.RegistryOps<?> $$3
               ? $$3.lookupProvider
                  .lookup($$1)
                  .flatMap($$1xx -> $$1xx.getter().get($$0))
                  .<DataResult>map(DataResult::success)
                  .orElseGet(() -> DataResult.error(() -> "Can't find value: " + $$0))
               : DataResult.error(() -> "Not a registry ops")
         )
         .forGetter($$0x -> null);
   }

   static final class HolderLookupAdapter implements net.minecraft.resources.RegistryOps.RegistryInfoLookup {
      private final Provider lookupProvider;
      private final Map<net.minecraft.resources.ResourceKey<? extends Registry<?>>, Optional<? extends net.minecraft.resources.RegistryOps.RegistryInfo<?>>> lookups = new ConcurrentHashMap<>();

      public HolderLookupAdapter(Provider $$0) {
         this.lookupProvider = $$0;
      }

      @Override
      public <E> Optional<net.minecraft.resources.RegistryOps.RegistryInfo<E>> lookup(net.minecraft.resources.ResourceKey<? extends Registry<? extends E>> $$0) {
         return (Optional<net.minecraft.resources.RegistryOps.RegistryInfo<E>>)this.lookups.computeIfAbsent($$0, this::createLookup);
      }

      private Optional<net.minecraft.resources.RegistryOps.RegistryInfo<Object>> createLookup(net.minecraft.resources.ResourceKey<? extends Registry<?>> $$0) {
         return this.lookupProvider.lookup($$0).map(net.minecraft.resources.RegistryOps.RegistryInfo::fromRegistryLookup);
      }

      @Override
      public boolean equals(Object $$0) {
         return this == $$0
            ? true
            : $$0 instanceof net.minecraft.resources.RegistryOps.HolderLookupAdapter $$1 && this.lookupProvider.equals($$1.lookupProvider);
      }

      @Override
      public int hashCode() {
         return this.lookupProvider.hashCode();
      }
   }

   public record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
      public static <T> net.minecraft.resources.RegistryOps.RegistryInfo<T> fromRegistryLookup(RegistryLookup<T> $$0) {
         return new net.minecraft.resources.RegistryOps.RegistryInfo<>($$0, $$0, $$0.registryLifecycle());
      }
   }

   public interface RegistryInfoLookup {
      <T> Optional<net.minecraft.resources.RegistryOps.RegistryInfo<T>> lookup(net.minecraft.resources.ResourceKey<? extends Registry<? extends T>> var1);
   }
}
