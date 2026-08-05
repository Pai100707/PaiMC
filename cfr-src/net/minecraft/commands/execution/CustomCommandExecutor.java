/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.TraceCallbacks;
import org.jspecify.annotations.Nullable;

public interface CustomCommandExecutor<T> {
    public void run(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4);

    public static abstract class WithErrorHandling<T extends ExecutionCommandSource<T>>
    implements CustomCommandExecutor<T> {
        @Override
        public final void run(T $$0, ContextChain<T> $$1, ChainModifiers $$2, ExecutionControl<T> $$3) {
            try {
                this.runGuarded($$0, $$1, $$2, $$3);
            }
            catch (CommandSyntaxException $$4) {
                this.onError($$4, $$0, $$2, $$3.tracer());
                $$0.callback().onFailure();
            }
        }

        protected void onError(CommandSyntaxException $$0, T $$1, ChainModifiers $$2, @Nullable TraceCallbacks $$3) {
            $$1.handleError($$0, $$2.isForked(), $$3);
        }

        protected abstract void runGuarded(T var1, ContextChain<T> var2, ChainModifiers var3, ExecutionControl<T> var4) throws CommandSyntaxException;
    }

    public static interface CommandAdapter<T>
    extends Command<T>,
    CustomCommandExecutor<T> {
        default public int run(CommandContext<T> $$0) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}

