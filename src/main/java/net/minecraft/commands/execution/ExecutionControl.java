package net.minecraft.commands.execution;

import org.jspecify.annotations.Nullable;

public interface ExecutionControl<T> {
   void queueNext(EntryAction<T> var1);

   void tracer(@Nullable TraceCallbacks var1);

   @Nullable
   TraceCallbacks tracer();

   Frame currentFrame();

   static <T extends net.minecraft.commands.ExecutionCommandSource<T>> ExecutionControl<T> create(final ExecutionContext<T> $$0, final Frame $$1) {
      return new ExecutionControl<T>() {
         @Override
         public void queueNext(EntryAction<T> $$0x) {
            $$0.queueNext(new CommandQueueEntry<>($$1, $$0));
         }

         @Override
         public void tracer(@Nullable TraceCallbacks $$0x) {
            $$0.tracer($$0);
         }

         @Nullable
         @Override
         public TraceCallbacks tracer() {
            return $$0.tracer();
         }

         @Override
         public Frame currentFrame() {
            return $$1;
         }
      };
   }
}
