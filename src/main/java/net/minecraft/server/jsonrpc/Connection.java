package net.minecraft.server.jsonrpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.EncodeJsonRpcException;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import net.minecraft.server.jsonrpc.methods.InvalidRequestJsonRpcException;
import net.minecraft.server.jsonrpc.methods.MethodNotFoundJsonRpcException;
import net.minecraft.server.jsonrpc.methods.RemoteRpcErrorException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

public class Connection extends SimpleChannelInboundHandler<JsonElement> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final AtomicInteger CONNECTION_ID_COUNTER = new AtomicInteger(0);
   private final JsonRpcLogger jsonRpcLogger;
   private final ClientInfo clientInfo;
   private final ManagementServer managementServer;
   private final Channel channel;
   private final MinecraftApi minecraftApi;
   private final AtomicInteger transactionId = new AtomicInteger();
   private final Int2ObjectMap<PendingRpcRequest<?>> pendingRequests = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap());

   public Connection(Channel $$0, ManagementServer $$1, MinecraftApi $$2, JsonRpcLogger $$3) {
      this.clientInfo = ClientInfo.of(CONNECTION_ID_COUNTER.incrementAndGet());
      this.managementServer = $$1;
      this.minecraftApi = $$2;
      this.channel = $$0;
      this.jsonRpcLogger = $$3;
   }

   public void tick() {
      long $$0 = Util.getMillis();
      this.pendingRequests
         .int2ObjectEntrySet()
         .removeIf(
            $$1 -> {
               boolean $$2 = ((PendingRpcRequest)$$1.getValue()).timedOut($$0);
               if ($$2) {
                  ((PendingRpcRequest)$$1.getValue())
                     .resultFuture()
                     .completeExceptionally(
                        new ReadTimeoutException(
                           "RPC method " + ((PendingRpcRequest)$$1.getValue()).method().key().identifier() + " timed out waiting for response"
                        )
                     );
               }

               return $$2;
            }
         );
   }

   public void channelActive(ChannelHandlerContext $$0) throws Exception {
      this.jsonRpcLogger.log(this.clientInfo, "Management connection opened for {}", this.channel.remoteAddress());
      super.channelActive($$0);
      this.managementServer.onConnected(this);
   }

   public void channelInactive(ChannelHandlerContext $$0) throws Exception {
      this.jsonRpcLogger.log(this.clientInfo, "Management connection closed for {}", this.channel.remoteAddress());
      super.channelInactive($$0);
      this.managementServer.onDisconnected(this);
   }

   public void exceptionCaught(ChannelHandlerContext $$0, Throwable $$1) throws Exception {
      if ($$1.getCause() instanceof JsonParseException) {
         this.channel.writeAndFlush(JsonRPCErrors.PARSE_ERROR.createWithUnknownId($$1.getMessage()));
      } else {
         super.exceptionCaught($$0, $$1);
         this.channel.close().awaitUninterruptibly();
      }
   }

   protected void channelRead0(ChannelHandlerContext $$0, JsonElement $$1) {
      if ($$1.isJsonObject()) {
         JsonObject $$2 = this.handleJsonObject($$1.getAsJsonObject());
         if ($$2 != null) {
            this.channel.writeAndFlush($$2);
         }
      } else if ($$1.isJsonArray()) {
         this.channel.writeAndFlush(this.handleBatchRequest($$1.getAsJsonArray().asList()));
      } else {
         this.channel.writeAndFlush(JsonRPCErrors.INVALID_REQUEST.createWithUnknownId(null));
      }
   }

   private JsonArray handleBatchRequest(List<JsonElement> $$0) {
      JsonArray $$1 = new JsonArray();
      $$0.stream().map($$0x -> this.handleJsonObject($$0x.getAsJsonObject())).filter(Objects::nonNull).forEach($$1::add);
      return $$1;
   }

   public void sendNotification(Reference<? extends OutgoingRpcMethod<Void, ?>> $$0) {
      this.sendRequest($$0, null, false);
   }

   public <Params> void sendNotification(Reference<? extends OutgoingRpcMethod<Params, ?>> $$0, Params $$1) {
      this.sendRequest($$0, $$1, false);
   }

   public <Result> CompletableFuture<Result> sendRequest(Reference<? extends OutgoingRpcMethod<Void, Result>> $$0) {
      return this.sendRequest($$0, null, true);
   }

   public <Params, Result> CompletableFuture<Result> sendRequest(Reference<? extends OutgoingRpcMethod<Params, Result>> $$0, Params $$1) {
      return this.sendRequest($$0, $$1, true);
   }

   @Contract("_,_,false->null;_,_,true->!null")
   
   private <Params, Result> CompletableFuture<Result> sendRequest(
      Reference<? extends OutgoingRpcMethod<Params, ? extends Result>> $$0, Params $$1, boolean $$2
   ) {
      List<JsonElement> $$3 = $$1 != null ? List.of(Objects.requireNonNull(((OutgoingRpcMethod)$$0.value()).encodeParams($$1))) : List.of();
      if ($$2) {
         CompletableFuture<Result> $$4 = new CompletableFuture<>();
         int $$5 = this.transactionId.incrementAndGet();
         long $$6 = Util.timeSource.get(TimeUnit.MILLISECONDS);
         this.pendingRequests.put($$5, new PendingRpcRequest<>($$0, $$4, $$6 + 5000L));
         this.channel.writeAndFlush(JsonRPCUtils.createRequest($$5, $$0.key().identifier(), $$3));
         return $$4;
      } else {
         this.channel.writeAndFlush(JsonRPCUtils.createRequest(null, $$0.key().identifier(), $$3));
         return null;
      }
   }

   @VisibleForTesting
   
   JsonObject handleJsonObject(JsonObject $$0) {
      try {
         JsonElement $$1 = JsonRPCUtils.getRequestId($$0);
         String $$2 = JsonRPCUtils.getMethodName($$0);
         JsonElement $$3 = JsonRPCUtils.getResult($$0);
         JsonElement $$4 = JsonRPCUtils.getParams($$0);
         JsonObject $$5 = JsonRPCUtils.getError($$0);
         if ($$2 != null && $$3 == null && $$5 == null) {
            return $$1 != null && !isValidRequestId($$1)
               ? JsonRPCErrors.INVALID_REQUEST.createWithUnknownId("Invalid request id - only String, Number and NULL supported")
               : this.handleIncomingRequest($$1, $$2, $$4);
         } else if ($$2 == null && $$3 != null && $$5 == null && $$1 != null) {
            if (isValidResponseId($$1)) {
               this.handleRequestResponse($$1.getAsInt(), $$3);
            } else {
               LOGGER.warn("Received respose {} with id {} we did not request", $$3, $$1);
            }

            return null;
         } else {
            return $$2 == null && $$3 == null && $$5 != null
               ? this.handleError($$1, $$5)
               : JsonRPCErrors.INVALID_REQUEST.createWithoutData(Objects.requireNonNullElse($$1, JsonNull.INSTANCE));
         }
      } catch (Exception var7) {
         LOGGER.error("Error while handling rpc request", var7);
         return JsonRPCErrors.INTERNAL_ERROR.createWithUnknownId("Unknown error handling request - check server logs for stack trace");
      }
   }

   private static boolean isValidRequestId(JsonElement $$0) {
      return $$0.isJsonNull() || GsonHelper.isNumberValue($$0) || GsonHelper.isStringValue($$0);
   }

   private static boolean isValidResponseId(JsonElement $$0) {
      return GsonHelper.isNumberValue($$0);
   }

   
   private JsonObject handleIncomingRequest(JsonElement $$0, String $$1, JsonElement $$2) {
      boolean $$3 = $$0 != null;

      try {
         JsonElement $$4 = this.dispatchIncomingRequest($$1, $$2);
         return $$4 != null && $$3 ? JsonRPCUtils.createSuccessResult($$0, $$4) : null;
      } catch (InvalidParameterJsonRpcException var6) {
         LOGGER.debug("Invalid parameter invocation {}: {}, {}", new Object[]{$$1, $$2, var6.getMessage()});
         return $$3 ? JsonRPCErrors.INVALID_PARAMS.create($$0, var6.getMessage()) : null;
      } catch (EncodeJsonRpcException var7) {
         LOGGER.error("Failed to encode json rpc response {}: {}", $$1, var7.getMessage());
         return $$3 ? JsonRPCErrors.INTERNAL_ERROR.create($$0, var7.getMessage()) : null;
      } catch (InvalidRequestJsonRpcException var8) {
         return $$3 ? JsonRPCErrors.INVALID_REQUEST.create($$0, var8.getMessage()) : null;
      } catch (MethodNotFoundJsonRpcException var9) {
         return $$3 ? JsonRPCErrors.METHOD_NOT_FOUND.create($$0, var9.getMessage()) : null;
      } catch (Exception var10) {
         LOGGER.error("Error while dispatching rpc method {}", $$1, var10);
         return $$3 ? JsonRPCErrors.INTERNAL_ERROR.createWithoutData($$0) : null;
      }
   }

   
   public JsonElement dispatchIncomingRequest(String $$0, JsonElement $$1) {
      Identifier $$2 = Identifier.tryParse($$0);
      if ($$2 == null) {
         throw new InvalidRequestJsonRpcException("Failed to parse method value: " + $$0);
      } else {
         Optional<IncomingRpcMethod<?, ?>> $$3 = BuiltInRegistries.INCOMING_RPC_METHOD.getOptional($$2);
         if ($$3.isEmpty()) {
            throw new MethodNotFoundJsonRpcException("Method not found: " + $$0);
         } else if ($$3.get().attributes().runOnMainThread()) {
            try {
               return this.minecraftApi.<JsonElement>submit(() -> $$3.get().apply(this.minecraftApi, $$1, this.clientInfo)).join();
            } catch (CompletionException var8) {
               if (var8.getCause() instanceof RuntimeException $$5) {
                  throw $$5;
               } else {
                  throw var8;
               }
            }
         } else {
            return $$3.get().apply(this.minecraftApi, $$1, this.clientInfo);
         }
      }
   }

   private void handleRequestResponse(int $$0, JsonElement $$1) {
      PendingRpcRequest<?> $$2 = (PendingRpcRequest<?>)this.pendingRequests.remove($$0);
      if ($$2 == null) {
         LOGGER.warn("Received unknown response (id: {}): {}", $$0, $$1);
      } else {
         $$2.accept($$1);
      }
   }

   
   private JsonObject handleError(JsonElement $$0, JsonObject $$1) {
      if ($$0 != null && isValidResponseId($$0)) {
         PendingRpcRequest<?> $$2 = (PendingRpcRequest<?>)this.pendingRequests.remove($$0.getAsInt());
         if ($$2 != null) {
            $$2.resultFuture().completeExceptionally(new RemoteRpcErrorException($$0, $$1));
         }
      }

      LOGGER.error("Received error (id: {}): {}", $$0, $$1);
      return null;
   }
}
