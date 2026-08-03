package net.minecraft.resources;

@FunctionalInterface
public interface DependantName<T, V> {
   V get(net.minecraft.resources.ResourceKey<T> var1);

   static <T, V> net.minecraft.resources.DependantName<T, V> fixed(V $$0) {
      return $$1 -> $$0;
   }
}
