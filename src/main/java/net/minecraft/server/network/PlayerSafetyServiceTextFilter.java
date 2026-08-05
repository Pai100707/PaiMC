package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCertificate;
import com.microsoft.aad.msal4j.ConfidentialClientApplication.Builder;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import net.minecraft.util.GsonHelper;

public class PlayerSafetyServiceTextFilter extends ServerTextFilter {
   private final ConfidentialClientApplication client;
   private final ClientCredentialParameters clientParameters;
   private final Set<String> fullyFilteredEvents;
   private final int connectionReadTimeoutMs;

   private PlayerSafetyServiceTextFilter(
      URL $$0,
      ServerTextFilter.MessageEncoder $$1,
      ServerTextFilter.IgnoreStrategy $$2,
      ExecutorService $$3,
      ConfidentialClientApplication $$4,
      ClientCredentialParameters $$5,
      Set<String> $$6,
      int $$7
   ) {
      super($$0, $$1, $$2, $$3);
      this.client = $$4;
      this.clientParameters = $$5;
      this.fullyFilteredEvents = $$6;
      this.connectionReadTimeoutMs = $$7;
   }

   
   public static ServerTextFilter createTextFilterFromConfig(String $$0) {
      JsonObject $$1 = GsonHelper.parse($$0);
      URI $$2 = URI.create(GsonHelper.getAsString($$1, "apiServer"));
      String $$3 = GsonHelper.getAsString($$1, "apiPath");
      String $$4 = GsonHelper.getAsString($$1, "scope");
      String $$5 = GsonHelper.getAsString($$1, "serverId", "");
      String $$6 = GsonHelper.getAsString($$1, "applicationId");
      String $$7 = GsonHelper.getAsString($$1, "tenantId");
      String $$8 = GsonHelper.getAsString($$1, "roomId", "Java:Chat");
      String $$9 = GsonHelper.getAsString($$1, "certificatePath");
      String $$10 = GsonHelper.getAsString($$1, "certificatePassword", "");
      int $$11 = GsonHelper.getAsInt($$1, "hashesToDrop", -1);
      int $$12 = GsonHelper.getAsInt($$1, "maxConcurrentRequests", 7);
      JsonArray $$13 = GsonHelper.getAsJsonArray($$1, "fullyFilteredEvents");
      Set<String> $$14 = new HashSet<>();
      $$13.forEach($$1x -> $$14.add(GsonHelper.convertToString($$1x, "filteredEvent")));
      int $$15 = GsonHelper.getAsInt($$1, "connectionReadTimeoutMs", 2000);

      URL $$16;
      try {
         $$16 = $$2.resolve($$3).toURL();
      } catch (MalformedURLException var26) {
         throw new RuntimeException(var26);
      }

      ServerTextFilter.MessageEncoder $$19 = ($$2x, $$3x) -> {
         JsonObject $$4x = new JsonObject();
         $$4x.addProperty("userId", $$2x.id().toString());
         $$4x.addProperty("userDisplayName", $$2x.name());
         $$4x.addProperty("server", $$5);
         $$4x.addProperty("room", $$8);
         $$4x.addProperty("area", "JavaChatRealms");
         $$4x.addProperty("data", $$3x);
         $$4x.addProperty("language", "*");
         return $$4x;
      };
      ServerTextFilter.IgnoreStrategy $$20 = ServerTextFilter.IgnoreStrategy.select($$11);
      ExecutorService $$21 = createWorkerPool($$12);

      IClientCertificate $$23;
      try (InputStream $$22 = Files.newInputStream(Path.of($$9))) {
         $$23 = ClientCredentialFactory.createFromCertificate($$22, $$10);
      } catch (Exception var28) {
         LOGGER.warn("Failed to open certificate file");
         return null;
      }

      ConfidentialClientApplication $$27;
      try {
         $$27 = ((Builder)((Builder)ConfidentialClientApplication.builder($$6, $$23).sendX5c(true).executorService($$21))
               .authority(String.format(Locale.ROOT, "https://login.microsoftonline.com/%s/", $$7)))
            .build();
      } catch (Exception var25) {
         LOGGER.warn("Failed to create confidential client application");
         return null;
      }

      ClientCredentialParameters $$30 = ClientCredentialParameters.builder(Set.of($$4)).build();
      return new PlayerSafetyServiceTextFilter($$16, $$19, $$20, $$21, $$27, $$30, $$14, $$15);
   }

   private IAuthenticationResult aquireIAuthenticationResult() {
      return (IAuthenticationResult)this.client.acquireToken(this.clientParameters).join();
   }

   @Override
   protected void setAuthorizationProperty(HttpURLConnection $$0) {
      IAuthenticationResult $$1 = this.aquireIAuthenticationResult();
      $$0.setRequestProperty("Authorization", "Bearer " + $$1.accessToken());
   }

   @Override
   protected FilteredText filterText(String $$0, ServerTextFilter.IgnoreStrategy $$1, JsonObject $$2) {
      JsonObject $$3 = GsonHelper.getAsJsonObject($$2, "result", null);
      if ($$3 == null) {
         return FilteredText.fullyFiltered($$0);
      } else {
         boolean $$4 = GsonHelper.getAsBoolean($$3, "filtered", true);
         if (!$$4) {
            return FilteredText.passThrough($$0);
         } else {
            for (JsonElement $$6 : GsonHelper.getAsJsonArray($$3, "events", new JsonArray())) {
               JsonObject $$7 = $$6.getAsJsonObject();
               String $$8 = GsonHelper.getAsString($$7, "id", "");
               if (this.fullyFilteredEvents.contains($$8)) {
                  return FilteredText.fullyFiltered($$0);
               }
            }

            JsonArray $$9 = GsonHelper.getAsJsonArray($$3, "redactedTextIndex", new JsonArray());
            return new FilteredText($$0, this.parseMask($$0, $$9, $$1));
         }
      }
   }

   @Override
   protected int connectionReadTimeout() {
      return this.connectionReadTimeoutMs;
   }
}
