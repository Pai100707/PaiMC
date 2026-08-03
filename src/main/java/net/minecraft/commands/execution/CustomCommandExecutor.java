package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

public interface CustomCommandExecutor<T> {
   void run(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4);

   public interface CommandAdapter<T> extends Command<T>, CustomCommandExecutor<T> {
      default int run(CommandContext<T> $$0) throws CommandSyntaxException {
         throw new UnsupportedOperationException("This function should not run");
      }
   }

   public abstract static class WithErrorHandling<T extends net.minecraft.commands.ExecutionCommandSource<T>> implements CustomCommandExecutor<T> {
      public final void run(T $$0, ContextChain<T> $$1, ChainModifiers $$2, ExecutionControl<T> $$3) {
         try {
            this.runGuarded($$0, $$1, $$2, $$3);
         } catch (CommandSyntaxException var6) {
            this.onError(var6, $$0, $$2, $$3.tracer());
            $$0.callback().onFailure();
         }
      }

      protected void onError(CommandSyntaxException $$0, T $$1, ChainModifiers $$2, @Nullable TraceCallbacks $$3) {
         $$1.handleError($$0, $$2.isForked(), $$3);
      }

      protected abstract void runGuarded(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4) throws CommandSyntaxException;
   }
}
