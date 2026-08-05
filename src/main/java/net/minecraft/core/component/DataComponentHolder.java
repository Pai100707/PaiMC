package net.minecraft.core.component;

import java.util.stream.Stream;

public interface DataComponentHolder extends DataComponentGetter {
   DataComponentMap getComponents();

   
   @Override
   default <T> T get(DataComponentType<? extends T> $$0) {
      return this.getComponents().get($$0);
   }

   default <T> Stream<T> getAllOfType(Class<? extends T> $$0) {
      return this.getComponents().stream().map(TypedDataComponent::value).filter($$1 -> $$0.isAssignableFrom($$1.getClass())).map($$0x -> (T)$$0x);
   }

   @Override
   default <T> T getOrDefault(DataComponentType<? extends T> $$0, T $$1) {
      return this.getComponents().getOrDefault($$0, $$1);
   }

   default boolean has(DataComponentType<?> $$0) {
      return this.getComponents().has($$0);
   }
}
