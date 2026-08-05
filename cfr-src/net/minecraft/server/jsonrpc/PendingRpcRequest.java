/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 */
package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;

public record PendingRpcRequest<Result>(Holder.Reference<? extends OutgoingRpcMethod<?, ? extends Result>> method, CompletableFuture<Result> resultFuture, long timeoutTime) {
    public void accept(JsonElement $$0) {
        try {
            Result $$1 = this.method.value().decodeResult($$0);
            this.resultFuture.complete(Objects.requireNonNull($$1));
        }
        catch (Exception $$2) {
            this.resultFuture.completeExceptionally($$2);
        }
    }

    public boolean timedOut(long $$0) {
        return $$0 > this.timeoutTime;
    }
}

