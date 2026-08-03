package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder.Reference;

public record PendingRpcRequest<Result>(
   Reference<? extends OutgoingRpcMethod<?, ? extends Result>> method, CompletableFuture<Result> resultFuture, long timeoutTime
) {
   public void accept(JsonElement $$0) {
      try {
         Result $$1 = (Result)((OutgoingRpcMethod)this.method.value()).decodeResult($$0);
         this.resultFuture.complete(Objects.requireNonNull($$1));
      } catch (Exception var3) {
         this.resultFuture.completeExceptionally(var3);
      }
   }

   public boolean timedOut(long $$0) {
      return $$0 > this.timeoutTime;
   }
}
