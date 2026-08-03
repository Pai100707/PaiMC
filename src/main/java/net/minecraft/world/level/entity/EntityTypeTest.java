package net.minecraft.world.level.entity;

import org.jspecify.annotations.Nullable;

public interface EntityTypeTest<B, T extends B> {
   static <B, T extends B> EntityTypeTest<B, T> forClass(final Class<T> $$0) {
      return new EntityTypeTest<B, T>() {
         @Nullable
         @Override
         public T tryCast(B $$0x) {
            return (T)($$0.isInstance($$0) ? $$0 : null);
         }

         @Override
         public Class<? extends B> getBaseClass() {
            return $$0;
         }
      };
   }

   static <B, T extends B> EntityTypeTest<B, T> forExactClass(final Class<T> $$0) {
      return new EntityTypeTest<B, T>() {
         @Nullable
         @Override
         public T tryCast(B $$0x) {
            return (T)($$0.equals($$0.getClass()) ? $$0 : null);
         }

         @Override
         public Class<? extends B> getBaseClass() {
            return $$0;
         }
      };
   }

   @Nullable
   T tryCast(B var1);

   Class<? extends B> getBaseClass();
}
