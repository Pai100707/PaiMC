/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;

public interface ReloadInstance {
    public CompletableFuture<?> done();

    public float getActualProgress();

    default public boolean isDone() {
        return this.done().isDone();
    }

    default public void checkExceptions() {
        CompletableFuture<?> $$0 = this.done();
        if ($$0.isCompletedExceptionally()) {
            $$0.join();
        }
    }
}

