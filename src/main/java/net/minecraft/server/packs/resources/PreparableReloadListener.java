package net.minecraft.server.packs.resources;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@FunctionalInterface
public interface PreparableReloadListener {
   CompletableFuture<Void> reload(PreparableReloadListener.SharedState var1, Executor var2, PreparableReloadListener.PreparationBarrier var3, Executor var4);

   default void prepareSharedState(PreparableReloadListener.SharedState $$0) {
   }

   default String getName() {
      return this.getClass().getSimpleName();
   }

   @FunctionalInterface
   public interface PreparationBarrier {
      <T> CompletableFuture<T> wait(T var1);
   }

   public static final class SharedState {
      private final ResourceManager manager;
      private final Map<PreparableReloadListener.StateKey<?>, Object> state = new IdentityHashMap<>();

      public SharedState(ResourceManager $$0) {
         this.manager = $$0;
      }

      public ResourceManager resourceManager() {
         return this.manager;
      }

      public <T> void set(PreparableReloadListener.StateKey<T> $$0, T $$1) {
         this.state.put($$0, $$1);
      }

      public <T> T get(PreparableReloadListener.StateKey<T> $$0) {
         return Objects.requireNonNull((T)this.state.get($$0));
      }
   }

   public static final class StateKey<T> {
   }
}
