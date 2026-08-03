package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ConsecutiveExecutor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ServerTextFilter implements AutoCloseable {
   protected static final Logger LOGGER = LogUtils.getLogger();
   private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
   private static final ThreadFactory THREAD_FACTORY = $$0 -> {
      Thread $$1 = new Thread($$0);
      $$1.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
      return $$1;
   };
   private final URL chatEndpoint;
   private final ServerTextFilter.MessageEncoder chatEncoder;
   final ServerTextFilter.IgnoreStrategy chatIgnoreStrategy;
   final ExecutorService workerPool;

   protected static ExecutorService createWorkerPool(int $$0) {
      return Executors.newFixedThreadPool($$0, THREAD_FACTORY);
   }

   protected ServerTextFilter(URL $$0, ServerTextFilter.MessageEncoder $$1, ServerTextFilter.IgnoreStrategy $$2, ExecutorService $$3) {
      this.chatIgnoreStrategy = $$2;
      this.workerPool = $$3;
      this.chatEndpoint = $$0;
      this.chatEncoder = $$1;
   }

   protected static URL getEndpoint(URI $$0, @Nullable JsonObject $$1, String $$2, String $$3) throws MalformedURLException {
      String $$4 = getEndpointFromConfig($$1, $$2, $$3);
      return $$0.resolve("/" + $$4).toURL();
   }

   protected static String getEndpointFromConfig(@Nullable JsonObject $$0, String $$1, String $$2) {
      return $$0 != null ? GsonHelper.getAsString($$0, $$1, $$2) : $$2;
   }

   @Nullable
   public static ServerTextFilter createFromConfig(DedicatedServerProperties $$0) {
      String $$1 = $$0.textFilteringConfig;
      if (StringUtil.isBlank($$1)) {
         return null;
      } else {
         return switch ($$0.textFilteringVersion) {
            case 0 -> LegacyTextFilter.createTextFilterFromConfig($$1);
            case 1 -> PlayerSafetyServiceTextFilter.createTextFilterFromConfig($$1);
            default -> {
               LOGGER.warn("Could not create text filter - unsupported text filtering version used");
               yield null;
            }
         };
      }
   }

   protected CompletableFuture<FilteredText> requestMessageProcessing(GameProfile $$0, String $$1, ServerTextFilter.IgnoreStrategy $$2, Executor $$3) {
      return $$1.isEmpty() ? CompletableFuture.completedFuture(FilteredText.EMPTY) : CompletableFuture.supplyAsync(() -> {
         JsonObject $$3x = this.chatEncoder.encode($$0, $$1);

         try {
            JsonObject $$4 = this.processRequestResponse($$3x, this.chatEndpoint);
            return this.filterText($$1, $$2, $$4);
         } catch (Exception var6) {
            LOGGER.warn("Failed to validate message '{}'", $$1, var6);
            return FilteredText.fullyFiltered($$1);
         }
      }, $$3);
   }

   protected abstract FilteredText filterText(String var1, ServerTextFilter.IgnoreStrategy var2, JsonObject var3);

   protected FilterMask parseMask(String $$0, JsonArray $$1, ServerTextFilter.IgnoreStrategy $$2) {
      if ($$1.isEmpty()) {
         return FilterMask.PASS_THROUGH;
      } else if ($$2.shouldIgnore($$0, $$1.size())) {
         return FilterMask.FULLY_FILTERED;
      } else {
         FilterMask $$3 = new FilterMask($$0.length());

         for (int $$4 = 0; $$4 < $$1.size(); $$4++) {
            $$3.setFiltered($$1.get($$4).getAsInt());
         }

         return $$3;
      }
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

   private JsonObject processRequestResponse(JsonObject $$0, URL $$1) throws IOException {
      HttpURLConnection $$2 = this.makeRequest($$0, $$1);

      JsonObject var5;
      try (InputStream $$3 = $$2.getInputStream()) {
         if ($$2.getResponseCode() == 204) {
            return new JsonObject();
         }

         try {
            var5 = LenientJsonParser.parse(new InputStreamReader($$3, StandardCharsets.UTF_8)).getAsJsonObject();
         } finally {
            this.drainStream($$3);
         }
      }

      return var5;
   }

   protected HttpURLConnection makeRequest(JsonObject $$0, URL $$1) throws IOException {
      HttpURLConnection $$2 = this.getURLConnection($$1);
      this.setAuthorizationProperty($$2);
      OutputStreamWriter $$3 = new OutputStreamWriter($$2.getOutputStream(), StandardCharsets.UTF_8);

      try {
         JsonWriter $$4 = new JsonWriter($$3);

         try {
            Streams.write($$0, $$4);
         } catch (Throwable var10) {
            try {
               $$4.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         $$4.close();
      } catch (Throwable var11) {
         try {
            $$3.close();
         } catch (Throwable var8) {
            var11.addSuppressed(var8);
         }

         throw var11;
      }

      $$3.close();
      int $$5 = $$2.getResponseCode();
      if ($$5 >= 200 && $$5 < 300) {
         return $$2;
      } else {
         throw new ServerTextFilter.RequestFailedException($$5 + " " + $$2.getResponseMessage());
      }
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
      return new ServerTextFilter.PlayerContext($$0);
   }

   @FunctionalInterface
   public interface IgnoreStrategy {
      ServerTextFilter.IgnoreStrategy NEVER_IGNORE = ($$0, $$1) -> false;
      ServerTextFilter.IgnoreStrategy IGNORE_FULLY_FILTERED = ($$0, $$1) -> $$0.length() == $$1;

      static ServerTextFilter.IgnoreStrategy ignoreOverThreshold(int $$0) {
         return ($$1, $$2) -> $$2 >= $$0;
      }

      static ServerTextFilter.IgnoreStrategy select(int $$0) {
         return switch ($$0) {
            case -1 -> NEVER_IGNORE;
            case 0 -> IGNORE_FULLY_FILTERED;
            default -> ignoreOverThreshold($$0);
         };
      }

      boolean shouldIgnore(String var1, int var2);
   }

   @FunctionalInterface
   protected interface MessageEncoder {
      JsonObject encode(GameProfile var1, String var2);
   }

   protected class PlayerContext implements TextFilter {
      protected final GameProfile profile;
      protected final Executor streamExecutor;

      protected PlayerContext(final GameProfile $$1) {
         this.profile = $$1;
         ConsecutiveExecutor $$2 = new ConsecutiveExecutor(ServerTextFilter.this.workerPool, "chat stream for " + $$1.name());
         this.streamExecutor = $$2::schedule;
      }

      @Override
      public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> $$0) {
         List<CompletableFuture<FilteredText>> $$1 = $$0.stream()
            .map($$0x -> ServerTextFilter.this.requestMessageProcessing(this.profile, $$0x, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor))
            .collect(ImmutableList.toImmutableList());
         return Util.sequenceFailFast($$1).exceptionally($$0x -> ImmutableList.of());
      }

      @Override
      public CompletableFuture<FilteredText> processStreamMessage(String $$0) {
         return ServerTextFilter.this.requestMessageProcessing(this.profile, $$0, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor);
      }
   }

   protected static class RequestFailedException extends RuntimeException {
      protected RequestFailedException(String $$0) {
         super($$0);
      }
   }
}
