/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.internal.Streams
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.LegacyTextFilter;
import net.minecraft.server.network.PlayerSafetyServiceTextFilter;
import net.minecraft.server.network.TextFilter;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ConsecutiveExecutor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ServerTextFilter
implements AutoCloseable {
    protected static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ThreadFactory THREAD_FACTORY = $$0 -> {
        Thread $$1 = new Thread($$0);
        $$1.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
        return $$1;
    };
    private final URL chatEndpoint;
    private final MessageEncoder chatEncoder;
    final IgnoreStrategy chatIgnoreStrategy;
    final ExecutorService workerPool;

    protected static ExecutorService createWorkerPool(int $$0) {
        return Executors.newFixedThreadPool($$0, THREAD_FACTORY);
    }

    protected ServerTextFilter(URL $$0, MessageEncoder $$1, IgnoreStrategy $$2, ExecutorService $$3) {
        this.chatIgnoreStrategy = $$2;
        this.workerPool = $$3;
        this.chatEndpoint = $$0;
        this.chatEncoder = $$1;
    }

    protected static URL getEndpoint(URI $$0, @Nullable JsonObject $$1, String $$2, String $$3) throws MalformedURLException {
        String $$4 = ServerTextFilter.getEndpointFromConfig($$1, $$2, $$3);
        return $$0.resolve("/" + $$4).toURL();
    }

    protected static String getEndpointFromConfig(@Nullable JsonObject $$0, String $$1, String $$2) {
        return $$0 != null ? GsonHelper.getAsString($$0, $$1, $$2) : $$2;
    }

    public static @Nullable ServerTextFilter createFromConfig(DedicatedServerProperties $$0) {
        String $$1 = $$0.textFilteringConfig;
        if (StringUtil.isBlank($$1)) {
            return null;
        }
        return switch ($$0.textFilteringVersion) {
            case 0 -> LegacyTextFilter.createTextFilterFromConfig($$1);
            case 1 -> PlayerSafetyServiceTextFilter.createTextFilterFromConfig($$1);
            default -> {
                LOGGER.warn("Could not create text filter - unsupported text filtering version used");
                yield null;
            }
        };
    }

    protected CompletableFuture<FilteredText> requestMessageProcessing(GameProfile $$0, String $$1, IgnoreStrategy $$2, Executor $$3) {
        if ($$1.isEmpty()) {
            return CompletableFuture.completedFuture(FilteredText.EMPTY);
        }
        return CompletableFuture.supplyAsync(() -> {
            JsonObject $$3 = this.chatEncoder.encode($$0, $$1);
            try {
                JsonObject $$4 = this.processRequestResponse($$3, this.chatEndpoint);
                return this.filterText($$1, $$2, $$4);
            }
            catch (Exception $$5) {
                LOGGER.warn("Failed to validate message '{}'", (Object)$$1, (Object)$$5);
                return FilteredText.fullyFiltered($$1);
            }
        }, $$3);
    }

    protected abstract FilteredText filterText(String var1, IgnoreStrategy var2, JsonObject var3);

    protected FilterMask parseMask(String $$0, JsonArray $$1, IgnoreStrategy $$2) {
        if ($$1.isEmpty()) {
            return FilterMask.PASS_THROUGH;
        }
        if ($$2.shouldIgnore($$0, $$1.size())) {
            return FilterMask.FULLY_FILTERED;
        }
        FilterMask $$3 = new FilterMask($$0.length());
        for (int $$4 = 0; $$4 < $$1.size(); ++$$4) {
            $$3.setFiltered($$1.get($$4).getAsInt());
        }
        return $$3;
    }

    @Override
    public void close() {
        this.workerPool.shutdownNow();
    }

    protected void drainStream(InputStream $$0) throws IOException {
        byte[] $$1 = new byte[1024];
        while ($$0.read($$1) != -1) {
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JsonObject processRequestResponse(JsonObject $$0, URL $$1) throws IOException {
        HttpURLConnection $$2 = this.makeRequest($$0, $$1);
        try (InputStream $$3 = $$2.getInputStream();){
            JsonObject jsonObject;
            if ($$2.getResponseCode() == 204) {
                JsonObject jsonObject2 = new JsonObject();
                return jsonObject2;
            }
            try {
                jsonObject = LenientJsonParser.parse(new InputStreamReader($$3, StandardCharsets.UTF_8)).getAsJsonObject();
            }
            catch (Throwable throwable) {
                this.drainStream($$3);
                throw throwable;
            }
            this.drainStream($$3);
            return jsonObject;
        }
    }

    protected HttpURLConnection makeRequest(JsonObject $$0, URL $$1) throws IOException {
        HttpURLConnection $$2 = this.getURLConnection($$1);
        this.setAuthorizationProperty($$2);
        try (OutputStreamWriter $$3 = new OutputStreamWriter($$2.getOutputStream(), StandardCharsets.UTF_8);
             JsonWriter $$4 = new JsonWriter((Writer)$$3);){
            Streams.write((JsonElement)$$0, (JsonWriter)$$4);
        }
        int $$5 = $$2.getResponseCode();
        if ($$5 < 200 || $$5 >= 300) {
            throw new RequestFailedException($$5 + " " + $$2.getResponseMessage());
        }
        return $$2;
    }

    protected abstract void setAuthorizationProperty(HttpURLConnection var1);

    protected int connectionReadTimeout() {
        return 2000;
    }

    protected HttpURLConnection getURLConnection(URL $$0) throws IOException {
        HttpURLConnection $$1 = (HttpURLConnection)$$0.openConnection();
        $$1.setConnectTimeout(15000);
        $$1.setReadTimeout(this.connectionReadTimeout());
        $$1.setUseCaches(false);
        $$1.setDoOutput(true);
        $$1.setDoInput(true);
        $$1.setRequestMethod("POST");
        $$1.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        $$1.setRequestProperty("Accept", "application/json");
        $$1.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().name());
        return $$1;
    }

    public TextFilter createContext(GameProfile $$0) {
        return new PlayerContext($$0);
    }

    @FunctionalInterface
    public static interface IgnoreStrategy {
        public static final IgnoreStrategy NEVER_IGNORE = ($$0, $$1) -> false;
        public static final IgnoreStrategy IGNORE_FULLY_FILTERED = ($$0, $$1) -> $$0.length() == $$1;

        public static IgnoreStrategy ignoreOverThreshold(int $$0) {
            return ($$1, $$2) -> $$2 >= $$0;
        }

        public static IgnoreStrategy select(int $$0) {
            return switch ($$0) {
                case -1 -> NEVER_IGNORE;
                case 0 -> IGNORE_FULLY_FILTERED;
                default -> IgnoreStrategy.ignoreOverThreshold($$0);
            };
        }

        public boolean shouldIgnore(String var1, int var2);
    }

    @FunctionalInterface
    protected static interface MessageEncoder {
        public JsonObject encode(GameProfile var1, String var2);
    }

    protected static class RequestFailedException
    extends RuntimeException {
        protected RequestFailedException(String $$0) {
            super($$0);
        }
    }

    protected class PlayerContext
    implements TextFilter {
        protected final GameProfile profile;
        protected final Executor streamExecutor;

        protected PlayerContext(GameProfile $$1) {
            this.profile = $$1;
            ConsecutiveExecutor $$2 = new ConsecutiveExecutor(ServerTextFilter.this.workerPool, "chat stream for " + $$1.name());
            this.streamExecutor = $$2::schedule;
        }

        @Override
        public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> $$02) {
            List $$1 = (List)$$02.stream().map($$0 -> ServerTextFilter.this.requestMessageProcessing(this.profile, (String)$$0, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor)).collect(ImmutableList.toImmutableList());
            return Util.sequenceFailFast($$1).exceptionally($$0 -> ImmutableList.of());
        }

        @Override
        public CompletableFuture<FilteredText> processStreamMessage(String $$0) {
            return ServerTextFilter.this.requestMessageProcessing(this.profile, $$0, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor);
        }
    }
}

