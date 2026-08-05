/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.minecraft.util.TaskChainer;
import org.slf4j.Logger;

public class FutureChain
implements TaskChainer,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CompletableFuture<?> head = CompletableFuture.completedFuture(null);
    private final Executor executor;
    private volatile boolean closed;

    public FutureChain(Executor $$0) {
        this.executor = $$0;
    }

    @Override
    public <T> void append(CompletableFuture<T> $$02, Consumer<T> $$12) {
        this.head = ((CompletableFuture)((CompletableFuture)this.head.thenCombine($$02, ($$0, $$1) -> $$1)).thenAcceptAsync($$1 -> {
            if (!this.closed) {
                $$12.accept($$1);
            }
        }, this.executor)).exceptionally($$0 -> {
            if ($$0 instanceof CompletionException) {
                CompletionException $$1 = (CompletionException)$$0;
                $$0 = $$1.getCause();
            }
            if ($$0 instanceof CancellationException) {
                CancellationException $$2 = (CancellationException)$$0;
                throw $$2;
            }
            LOGGER.error("Chain link failed, continuing to next one", $$0);
            return null;
        });
    }

    @Override
    public void close() {
        this.closed = true;
    }
}

