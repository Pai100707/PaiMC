package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T> implements PreparableReloadListener {
   @Override
   public final CompletableFuture<Void> reload(
      PreparableReloadListener.SharedState $$0, Executor $$1, PreparableReloadListener.PreparationBarrier $$2, Executor $$3
   ) {
      ResourceManager $$4 = $$0.resourceManager();
      return CompletableFuture.<T>supplyAsync(() -> this.prepare($$4, Profiler.get()), $$1)
         .thenCompose($$2::wait)
         .thenAcceptAsync($$1x -> this.apply((T)$$1x, $$4, Profiler.get()), $$3);
   }

   protected abstract T prepare(ResourceManager var1, ProfilerFiller var2);

   protected abstract void apply(T var1, ResourceManager var2, ProfilerFiller var3);
}
