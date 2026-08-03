package net.minecraft.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface AbortableIterationConsumer<T> {
   net.minecraft.util.AbortableIterationConsumer.Continuation accept(T var1);

   static <T> net.minecraft.util.AbortableIterationConsumer<T> forConsumer(Consumer<T> $$0) {
      return $$1 -> {
         $$0.accept($$1);
         return net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
      };
   }

   public static enum Continuation {
      CONTINUE,
      ABORT;

      public boolean shouldAbort() {
         return this == ABORT;
      }
   }
}
