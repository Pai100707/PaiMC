package net.minecraft.core.component;


public interface DataComponentGetter {
   
   <T> T get(DataComponentType<? extends T> var1);

   default <T> T getOrDefault(DataComponentType<? extends T> $$0, T $$1) {
      T $$2 = this.get($$0);
      return $$2 != null ? $$2 : $$1;
   }

   
   default <T> TypedDataComponent<T> getTyped(DataComponentType<T> $$0) {
      T $$1 = this.get($$0);
      return $$1 != null ? new TypedDataComponent<>($$0, $$1) : null;
   }
}
