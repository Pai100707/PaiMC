package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends net.minecraft.util.DependencySorter.Entry<K>> {
   private final Map<K, V> contents = new HashMap<>();

   public net.minecraft.util.DependencySorter<K, V> addEntry(K $$0, V $$1) {
      this.contents.put($$0, $$1);
      return this;
   }

   private void visitDependenciesAndElement(Multimap<K, K> $$0, Set<K> $$1, K $$2, BiConsumer<K, V> $$3) {
      if ($$1.add($$2)) {
         $$0.get($$2).forEach($$3x -> this.visitDependenciesAndElement($$0, $$1, (K)$$3x, $$3));
         V $$4 = this.contents.get($$2);
         if ($$4 != null) {
            $$3.accept($$2, $$4);
         }
      }
   }

   private static <K> boolean isCyclic(Multimap<K, K> $$0, K $$1, K $$2) {
      Collection<K> $$3 = $$0.get($$2);
      return $$3.contains($$1) ? true : $$3.stream().anyMatch($$2x -> isCyclic($$0, $$1, $$2x));
   }

   private static <K> void addDependencyIfNotCyclic(Multimap<K, K> $$0, K $$1, K $$2) {
      if (!isCyclic($$0, $$1, $$2)) {
         $$0.put($$1, $$2);
      }
   }

   public void orderByDependencies(BiConsumer<K, V> $$0) {
      Multimap<K, K> $$1 = HashMultimap.create();
      this.contents.forEach(($$1x, $$2x) -> $$2x.visitRequiredDependencies($$2xx -> addDependencyIfNotCyclic($$1, $$1x, $$2xx)));
      this.contents.forEach(($$1x, $$2x) -> $$2x.visitOptionalDependencies($$2xx -> addDependencyIfNotCyclic($$1, $$1x, $$2xx)));
      Set<K> $$2 = new HashSet<>();
      this.contents.keySet().forEach($$3 -> this.visitDependenciesAndElement($$1, $$2, (K)$$3, $$0));
   }

   public interface Entry<K> {
      void visitRequiredDependencies(Consumer<K> var1);

      void visitOptionalDependencies(Consumer<K> var1);
   }
}
