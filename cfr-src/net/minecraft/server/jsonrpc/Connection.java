/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.handler.timeout.ReadTimeoutException
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.JsonRPCErrors;
import net.minecraft.server.jsonrpc.JsonRPCUtils;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.ManagementServer;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.jsonrpc.PendingRpcRequest;
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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Connection
extends SimpleChannelInboundHandler<JsonElement> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger CONNECTION_ID_COUNTER = new AtomicInteger(0);
    private final JsonRpcLogger jsonRpcLogger;
    private final ClientInfo clientInfo;
    private final ManagementServer managementServer;
    private final Channel channel;
    private final MinecraftApi minecraftApi;
    private final AtomicInteger transactionId = new AtomicInteger();
    private final Int2ObjectMap<PendingRpcRequest<?>> pendingRequests = Int2ObjectMaps.synchronize((Int2ObjectMap)new Int2ObjectOpenHashMap());

    public Connection(Channel $$0, ManagementServer $$1, MinecraftApi $$2, JsonRpcLogger $$3) {
        this.clientInfo = ClientInfo.of(CONNECTION_ID_COUNTER.incrementAndGet());
        this.managementServer = $$1;
        this.minecraftApi = $$2;
        this.channel = $$0;
        this.jsonRpcLogger = $$3;
    }

    public void tick() {
        long $$0 = Util.getMillis();
        this.pendingRequests.int2ObjectEntrySet().removeIf($$1 -> {
            boolean $$2 = ((PendingRpcRequest)$$1.getValue()).timedOut($$0);
            if ($$2) {
                ((PendingRpcRequest)$$1.getValue()).resultFuture().completeExceptionally((Throwable)new ReadTimeoutException("RPC method " + String.valueOf(((PendingRpcRequest)$$1.getValue()).method().key().identifier()) + " timed out waiting for response"));
            }
            return $$2;
        });
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
            this.channel.writeAndFlush((Object)JsonRPCErrors.PARSE_ERROR.createWithUnknownId($$1.getMessage()));
            return;
        }
        super.exceptionCaught($$0, $$1);
        this.channel.close().awaitUninterruptibly();
    }

    protected void channelRead0(ChannelHandlerContext $$0, JsonElement $$1) {
        if ($$1.isJsonObject()) {
            JsonObject $$2 = this.handleJsonObject($$1.getAsJsonObject());
            if ($$2 != null) {
                this.channel.writeAndFlush((Object)$$2);
            }
        } else if ($$1.isJsonArray()) {
            this.channel.writeAndFlush((Object)this.handleBatchRequest($$1.getAsJsonArray().asList()));
        } else {
            this.channel.writeAndFlush((Object)JsonRPCErrors.INVALID_REQUEST.createWithUnknownId(null));
        }
    }

    private JsonArray handleBatchRequest(List<JsonElement> $$02) {
        JsonArray $$1 = new JsonArray();
        $$02.stream().map($$0 -> this.handleJsonObject($$0.getAsJsonObject())).filter(Objects::nonNull).forEach(arg_0 -> ((JsonArray)$$1).add(arg_0));
        return $$1;
    }

    public void sendNotification(Holder.Reference<? extends OutgoingRpcMethod<Void, ?>> $$0) {
        this.sendRequest($$0, null, false);
    }

    public <Params> void sendNotification(Holder.Reference<? extends OutgoingRpcMethod<Params, ?>> $$0, Params $$1) {
        this.sendRequest($$0, $$1, false);
    }

    public <Result> CompletableFuture<Result> sendRequest(Holder.Reference<? extends OutgoingRpcMethod<Void, Result>> $$0) {
        return this.sendRequest($$0, null, true);
    }

    public <Params, Result> CompletableFuture<Result> sendRequest(Holder.Reference<? extends OutgoingRpcMethod<Params, Result>> $$0, Params $$1) {
        return this.sendRequest($$0, $$1, true);
    }

    @Contract(value="_,_,false->null;_,_,true->!null")
    private <Params, Result> @Nullable CompletableFuture<Result> sendRequest(Holder.Reference<? extends OutgoingRpcMethod<Params, ? extends Result>> $$0, @Nullable Params $$1, boolean $$2) {
        List<JsonElement> $$3;
        List<JsonElement> list = $$3 = $$1 != null ? List.of(Objects.requireNonNull($$0.value().encodeParams($$1))) : List.of();
        if ($$2) {
            CompletableFuture $$4 = new CompletableFuture();
            int $$5 = this.transactionId.incrementAndGet();
            long $$6 = Util.timeSource.get(TimeUnit.MILLISECONDS);
            this.pendingRequests.put($$5, new PendingRpcRequest($$0, $$4, $$6 + 5000L));
            this.channel.writeAndFlush((Object)JsonRPCUtils.createRequest($$5, $$0.key().identifier(), $$3));
            return $$4;
        }
        this.channel.writeAndFlush((Object)JsonRPCUtils.createRequest(null, $$0.key().identifier(), $$3));
        return null;
    }

    @VisibleForTesting
    @Nullable JsonObject handleJsonObject(JsonObject $$0) {
        try {
            JsonElement $$1 = JsonRPCUtils.getRequestId($$0);
            String $$2 = JsonRPCUtils.getMethodName($$0);
            JsonElement $$3 = JsonRPCUtils.getResult($$0);
            JsonElement $$4 = JsonRPCUtils.getParams($$0);
            JsonObject $$5 = JsonRPCUtils.getError($$0);
            if ($$2 != null && $$3 == null && $$5 == null) {
                if ($$1 != null && !Connection.isValidRequestId($$1)) {
                    return JsonRPCErrors.INVALID_REQUEST.createWithUnknownId("Invalid request id - only String, Number and NULL supported");
                }
                return this.handleIncomingRequest($$1, $$2, $$4);
            }
            if ($$2 == null && $$3 != null && $$5 == null && $$1 != null) {
                if (Connection.isValidResponseId($$1)) {
                    this.handleRequestResponse($$1.getAsInt(), $$3);
                } else {
                    LOGGER.warn("Received respose {} with id {} we did not request", (Object)$$3, (Object)$$1);
                }
                return null;
            }
            if ($$2 == null && $$3 == null && $$5 != null) {
                return this.handleError($$1, $$5);
            }
            return JsonRPCErrors.INVALID_REQUEST.createWithoutData((JsonElement)Objects.requireNonNullElse($$1, JsonNull.INSTANCE));
        }
        catch (Exception $$6) {
            LOGGER.error("Error while handling rpc request", (Throwable)$$6);
            return JsonRPCErrors.INTERNAL_ERROR.createWithUnknownId("Unknown error handling request - check server logs for stack trace");
        }
    }

    private static boolean isValidRequestId(JsonElement $$0) {
        return $$0.isJsonNull() || GsonHelper.isNumberValue($$0) || GsonHelper.isStringValue($$0);
    }

    private static boolean isValidResponseId(JsonElement $$0) {
        return GsonHelper.isNumberValue($$0);
    }

    private @Nullable JsonObject handleIncomingRequest(@Nullable JsonElement $$0, String $$1, @Nullable JsonElement $$2) {
        boolean $$3 = $$0 != null;
        try {
            JsonElement $$4 = this.dispatchIncomingRequest($$1, $$2);
            if ($$4 == null || !$$3) {
                return null;
            }
            return JsonRPCUtils.createSuccessResult($$0, $$4);
        }
        catch (InvalidParameterJsonRpcException $$5) {
            LOGGER.debug("Invalid parameter invocation {}: {}, {}", new Object[]{$$1, $$2, $$5.getMessage()});
            return $$3 ? JsonRPCErrors.INVALID_PARAMS.create($$0, $$5.getMessage()) : null;
        }
        catch (EncodeJsonRpcException $$6) {
            LOGGER.error("Failed to encode json rpc response {}: {}", (Object)$$1, (Object)$$6.getMessage());
            return $$3 ? JsonRPCErrors.INTERNAL_ERROR.create($$0, $$6.getMessage()) : null;
        }
        catch (InvalidRequestJsonRpcException $$7) {
            return $$3 ? JsonRPCErrors.INVALID_REQUEST.create($$0, $$7.getMessage()) : null;
        }
        catch (MethodNotFoundJsonRpcException $$8) {
            return $$3 ? JsonRPCErrors.METHOD_NOT_FOUND.create($$0, $$8.getMessage()) : null;
        }
        catch (Exception $$9) {
            LOGGER.error("Error while dispatching rpc method {}", (Object)$$1, (Object)$$9);
            return $$3 ? JsonRPCErrors.INTERNAL_ERROR.createWithoutData($$0) : null;
        }
    }

    public @Nullable JsonElement dispatchIncomingRequest(String $$0, @Nullable JsonElement $$1) {
        Identifier $$2 = Identifier.tryParse($$0);
        if ($$2 == null) {
            throw new InvalidRequestJsonRpcException("Failed to parse method value: " + $$0);
        }
        Optional<IncomingRpcMethod<?, ?>> $$3 = BuiltInRegistries.INCOMING_RPC_METHOD.getOptional($$2);
        if ($$3.isEmpty()) {
            throw new MethodNotFoundJsonRpcException("Method not found: " + $$0);
        }
        if ($$3.get().attributes().runOnMainThread()) {
            try {
                return this.minecraftApi.submit(() -> ((IncomingRpcMethod)$$3.get()).apply(this.minecraftApi, $$1, this.clientInfo)).join();
            }
            catch (CompletionException $$4) {
                Throwable throwable = $$4.getCause();
                if (throwable instanceof RuntimeException) {
                    RuntimeException $$5 = (RuntimeException)throwable;
                    throw $$5;
                }
                throw $$4;
            }
        }
        return $$3.get().apply(this.minecraftApi, $$1, this.clientInfo);
    }

    private void handleRequestResponse(int $$0, JsonElement $$1) {
        PendingRpcRequest $$2 = (PendingRpcRequest)this.pendingRequests.remove($$0);
        if ($$2 == null) {
            LOGGER.warn("Received unknown response (id: {}): {}", (Object)$$0, (Object)$$1);
        } else {
            $$2.accept($$1);
        }
    }

    private @Nullable JsonObject handleError(@Nullable JsonElement $$0, JsonObject $$1) {
        PendingRpcRequest $$2;
        if ($$0 != null && Connection.isValidResponseId($$0) && ($$2 = (PendingRpcRequest)this.pendingRequests.remove($$0.getAsInt())) != null) {
            $$2.resultFuture().completeExceptionally(new RemoteRpcErrorException($$0, $$1));
        }
        LOGGER.error("Received error (id: {}): {}", (Object)$$0, (Object)$$1);
        return null;
    }

    protected /* synthetic */ void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        this.channelRead0(channelHandlerContext, (JsonElement)object);
    }
}

