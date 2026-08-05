package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class PatchedDataComponentMap implements DataComponentMap {
   private final DataComponentMap prototype;
   private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
   private boolean copyOnWrite;

   public PatchedDataComponentMap(DataComponentMap $$0) {
      this($$0, Reference2ObjectMaps.emptyMap(), true);
   }

   private PatchedDataComponentMap(DataComponentMap $$0, Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$1, boolean $$2) {
      this.prototype = $$0;
      this.patch = $$1;
      this.copyOnWrite = $$2;
   }

   public static PatchedDataComponentMap fromPatch(DataComponentMap $$0, DataComponentPatch $$1) {
      if (isPatchSanitized($$0, $$1.map)) {
         return new PatchedDataComponentMap($$0, $$1.map, true);
      } else {
         PatchedDataComponentMap $$2 = new PatchedDataComponentMap($$0);
         $$2.applyPatch($$1);
         return $$2;
      }
   }

   private static boolean isPatchSanitized(DataComponentMap $$0, Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$1) {
      ObjectIterator var2 = Reference2ObjectMaps.fastIterable($$1).iterator();

      while (var2.hasNext()) {
         Entry<DataComponentType<?>, Optional<?>> $$2 = (Entry<DataComponentType<?>, Optional<?>>)var2.next();
         Object $$3 = $$0.get($$2.getKey());
         Optional<?> $$4 = $$2.getValue();
         if ($$4.isPresent() && $$4.get().equals($$3)) {
            return false;
         }

         if ($$4.isEmpty() && $$3 == null) {
            return false;
         }
      }

      return true;
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      Optional<? extends T> $$1 = (Optional<? extends T>)this.patch.get($$0);
      return (T)($$1 != null ? $$1.orElse(null) : this.prototype.get($$0));
   }

   public boolean hasNonDefault(DataComponentType<?> $$0) {
      return this.patch.containsKey($$0);
   }

   
   public <T> T set(DataComponentType<T> $$0, T $$1) {
      this.ensureMapOwnership();
      T $$2 = this.prototype.get($$0);
      Optional<T> $$3;
      if (Objects.equals($$1, $$2)) {
         $$3 = (Optional<T>)this.patch.remove($$0);
      } else {
         $$3 = (Optional<T>)this.patch.put($$0, Optional.ofNullable($$1));
      }

      return $$3 != null ? $$3.orElse($$2) : $$2;
   }

   
   public <T> T set(TypedDataComponent<T> $$0) {
      return this.set($$0.type(), $$0.value());
   }

   
   public <T> T remove(DataComponentType<? extends T> $$0) {
      this.ensureMapOwnership();
      T $$1 = this.prototype.get($$0);
      Optional<? extends T> $$2;
      if ($$1 != null) {
         $$2 = (Optional<? extends T>)this.patch.put($$0, Optional.empty());
      } else {
         $$2 = (Optional<? extends T>)this.patch.remove($$0);
      }

      return (T)($$2 != null ? $$2.orElse(null) : $$1);
   }

   public void applyPatch(DataComponentPatch $$0) {
      this.ensureMapOwnership();
      ObjectIterator var2 = Reference2ObjectMaps.fastIterable($$0.map).iterator();

      while (var2.hasNext()) {
         Entry<DataComponentType<?>, Optional<?>> $$1 = (Entry<DataComponentType<?>, Optional<?>>)var2.next();
         this.applyPatch($$1.getKey(), $$1.getValue());
      }
   }

   private void applyPatch(DataComponentType<?> $$0, Optional<?> $$1) {
      Object $$2 = this.prototype.get($$0);
      if ($$1.isPresent()) {
         if ($$1.get().equals($$2)) {
            this.patch.remove($$0);
         } else {
            this.patch.put($$0, $$1);
         }
      } else if ($$2 != null) {
         this.patch.put($$0, Optional.empty());
      } else {
         this.patch.remove($$0);
      }
   }

   public void restorePatch(DataComponentPatch $$0) {
      this.ensureMapOwnership();
      this.patch.clear();
      this.patch.putAll($$0.map);
   }

   public void clearPatch() {
      this.ensureMapOwnership();
      this.patch.clear();
   }

   public void setAll(DataComponentMap $$0) {
      for (TypedDataComponent<?> $$1 : $$0) {
         $$1.applyTo(this);
      }
   }

   private void ensureMapOwnership() {
      if (this.copyOnWrite) {
         this.patch = new Reference2ObjectArrayMap(this.patch);
         this.copyOnWrite = false;
      }
   }

   @Override
   public Set<DataComponentType<?>> keySet() {
      if (this.patch.isEmpty()) {
         return this.prototype.keySet();
      } else {
         Set<DataComponentType<?>> $$0 = new ReferenceArraySet(this.prototype.keySet());
         ObjectIterator var2 = Reference2ObjectMaps.fastIterable(this.patch).iterator();

         while (var2.hasNext()) {
            it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> $$1 = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>>)var2.next();
            Optional<?> $$2 = (Optional<?>)$$1.getValue();
            if ($$2.isPresent()) {
               $$0.add((DataComponentType<?>)$$1.getKey());
            } else {
               $$0.remove($$1.getKey());
            }
         }

         return $$0;
      }
   }

   @Override
   public Iterator<TypedDataComponent<?>> iterator() {
      if (this.patch.isEmpty()) {
         return this.prototype.iterator();
      } else {
         List<TypedDataComponent<?>> $$0 = new ArrayList<>(this.patch.size() + this.prototype.size());
         ObjectIterator var2 = Reference2ObjectMaps.fastIterable(this.patch).iterator();

         while (var2.hasNext()) {
            it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> $$1 = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>>)var2.next();
            if (((Optional)$$1.getValue()).isPresent()) {
               $$0.add(TypedDataComponent.createUnchecked((DataComponentType)$$1.getKey(), ((Optional)$$1.getValue()).get()));
            }
         }

         for (TypedDataComponent<?> $$2 : this.prototype) {
            if (!this.patch.containsKey($$2.type())) {
               $$0.add($$2);
            }
         }

         return $$0.iterator();
      }
   }

   @Override
   public int size() {
      int $$0 = this.prototype.size();
      ObjectIterator var2 = Reference2ObjectMaps.fastIterable(this.patch).iterator();

      while (var2.hasNext()) {
         it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> $$1 = (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>>)var2.next();
         boolean $$2 = ((Optional)$$1.getValue()).isPresent();
         boolean $$3 = this.prototype.has((DataComponentType<?>)$$1.getKey());
         if ($$2 != $$3) {
            $$0 += $$2 ? 1 : -1;
         }
      }

      return $$0;
   }

   public DataComponentPatch asPatch() {
      if (this.patch.isEmpty()) {
         return DataComponentPatch.EMPTY;
      } else {
         this.copyOnWrite = true;
         return new DataComponentPatch(this.patch);
      }
   }

   public PatchedDataComponentMap copy() {
      this.copyOnWrite = true;
      return new PatchedDataComponentMap(this.prototype, this.patch, true);
   }

   public DataComponentMap toImmutableMap() {
      return (DataComponentMap)(this.patch.isEmpty() ? this.prototype : this.copy());
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof PatchedDataComponentMap $$1 && this.prototype.equals($$1.prototype) && this.patch.equals($$1.patch);
   }

   @Override
   public int hashCode() {
      return this.prototype.hashCode() + this.patch.hashCode() * 31;
   }

   @Override
   public String toString() {
      return "{" + this.stream().map(TypedDataComponent::toString).collect(Collectors.joining(", ")) + "}";
   }
}
