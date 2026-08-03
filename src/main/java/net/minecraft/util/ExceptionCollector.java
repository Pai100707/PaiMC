package net.minecraft.util;

import org.jspecify.annotations.Nullable;

public class ExceptionCollector<T extends Throwable> {
   @Nullable
   private T result;

   public void add(T $$0) {
      if (this.result == null) {
         this.result = $$0;
      } else {
         this.result.addSuppressed($$0);
      }
   }

   public void throwIfPresent() throws T {
      if (this.result != null) {
         throw this.result;
      }
   }
}
