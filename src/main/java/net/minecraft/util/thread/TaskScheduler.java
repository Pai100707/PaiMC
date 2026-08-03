package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface TaskScheduler<R extends Runnable> extends AutoCloseable {
   String name();

   void schedule(R var1);

   @Override
   default void close() {
   }

   R wrapRunnable(Runnable var1);

   default <Source> CompletableFuture<Source> scheduleWithResult(Consumer<CompletableFuture<Source>> $$0) {
      CompletableFuture<Source> $$1 = new CompletableFuture<>();
      this.schedule(this.wrapRunnable(() -> $$0.accept($$1)));
      return $$1;
   }

   static TaskScheduler<Runnable> wrapExecutor(final String $$0, final Executor $$1) {
      return new TaskScheduler<Runnable>() {
         @Override
         public String name() {
            return $$0;
         }

         @Override
         public void schedule(Runnable $$0x) {
            $$1.execute($$0);
         }

         @Override
         public Runnable wrapRunnable(Runnable $$0x) {
            return $$0;
         }

         @Override
         public String toString() {
            return $$0;
         }
      };
   }
}
