package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;

public class FutureChain implements net.minecraft.util.TaskChainer, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private CompletableFuture<?> head = CompletableFuture.completedFuture(null);
   private final Executor executor;
   private volatile boolean closed;

   public FutureChain(Executor $$0) {
      this.executor = $$0;
   }

   @Override
   public <T> void append(CompletableFuture<T> $$0, Consumer<T> $$1) {
      this.head = this.head.<T, Object>thenCombine($$0, ($$0x, $$1x) -> $$1x).thenAcceptAsync($$1x -> {
         if (!this.closed) {
            $$1.accept((T)$$1x);
         }
      }, this.executor).exceptionally($$0x -> {
         if ($$0x instanceof CompletionException $$1x) {
            $$0x = $$1x.getCause();
         }

         if ($$0x instanceof CancellationException $$2) {
            throw $$2;
         } else {
            LOGGER.error("Chain link failed, continuing to next one", $$0x);
            return null;
         }
      });
   }

   @Override
   public void close() {
      this.closed = true;
   }
}
