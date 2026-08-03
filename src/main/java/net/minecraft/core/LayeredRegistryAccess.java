package net.minecraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

public class LayeredRegistryAccess<T> {
   private final List<T> keys;
   private final List<net.minecraft.core.RegistryAccess.Frozen> values;
   private final net.minecraft.core.RegistryAccess.Frozen composite;

   public LayeredRegistryAccess(List<T> $$0) {
      this($$0, (List<net.minecraft.core.RegistryAccess.Frozen>)Util.make(() -> {
         net.minecraft.core.RegistryAccess.Frozen[] $$1 = new net.minecraft.core.RegistryAccess.Frozen[$$0.size()];
         Arrays.fill($$1, net.minecraft.core.RegistryAccess.EMPTY);
         return Arrays.asList($$1);
      }));
   }

   private LayeredRegistryAccess(List<T> $$0, List<net.minecraft.core.RegistryAccess.Frozen> $$1) {
      this.keys = List.copyOf($$0);
      this.values = List.copyOf($$1);
      this.composite = new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(collectRegistries($$1.stream())).freeze();
   }

   private int getLayerIndexOrThrow(T $$0) {
      int $$1 = this.keys.indexOf($$0);
      if ($$1 == -1) {
         throw new IllegalStateException("Can't find " + $$0 + " inside " + this.keys);
      } else {
         return $$1;
      }
   }

   public net.minecraft.core.RegistryAccess.Frozen getLayer(T $$0) {
      int $$1 = this.getLayerIndexOrThrow($$0);
      return this.values.get($$1);
   }

   public net.minecraft.core.RegistryAccess.Frozen getAccessForLoading(T $$0) {
      int $$1 = this.getLayerIndexOrThrow($$0);
      return this.getCompositeAccessForLayers(0, $$1);
   }

   public net.minecraft.core.RegistryAccess.Frozen getAccessFrom(T $$0) {
      int $$1 = this.getLayerIndexOrThrow($$0);
      return this.getCompositeAccessForLayers($$1, this.values.size());
   }

   private net.minecraft.core.RegistryAccess.Frozen getCompositeAccessForLayers(int $$0, int $$1) {
      return new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(collectRegistries(this.values.subList($$0, $$1).stream())).freeze();
   }

   public net.minecraft.core.LayeredRegistryAccess<T> replaceFrom(T $$0, net.minecraft.core.RegistryAccess.Frozen... $$1) {
      return this.replaceFrom($$0, Arrays.asList($$1));
   }

   public net.minecraft.core.LayeredRegistryAccess<T> replaceFrom(T $$0, List<net.minecraft.core.RegistryAccess.Frozen> $$1) {
      int $$2 = this.getLayerIndexOrThrow($$0);
      if ($$1.size() > this.values.size() - $$2) {
         throw new IllegalStateException("Too many values to replace");
      } else {
         List<net.minecraft.core.RegistryAccess.Frozen> $$3 = new ArrayList<>();

         for (int $$4 = 0; $$4 < $$2; $$4++) {
            $$3.add(this.values.get($$4));
         }

         $$3.addAll($$1);

         while ($$3.size() < this.values.size()) {
            $$3.add(net.minecraft.core.RegistryAccess.EMPTY);
         }

         return new net.minecraft.core.LayeredRegistryAccess<>(this.keys, $$3);
      }
   }

   public net.minecraft.core.RegistryAccess.Frozen compositeAccess() {
      return this.composite;
   }

   private static Map<ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.core.Registry<?>> collectRegistries(
      Stream<? extends net.minecraft.core.RegistryAccess> $$0
   ) {
      Map<ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.core.Registry<?>> $$1 = new HashMap<>();
      $$0.forEach($$1x -> $$1x.registries().forEach($$1xx -> {
         if ($$1.put($$1xx.key(), $$1xx.value()) != null) {
            throw new IllegalStateException("Duplicated registry " + $$1xx.key());
         }
      }));
      return $$1;
   }
}
