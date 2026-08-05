/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.microsoft.aad.msal4j.ClientCredentialFactory
 *  com.microsoft.aad.msal4j.ClientCredentialParameters
 *  com.microsoft.aad.msal4j.ConfidentialClientApplication
 *  com.microsoft.aad.msal4j.ConfidentialClientApplication$Builder
 *  com.microsoft.aad.msal4j.IAuthenticationResult
 *  com.microsoft.aad.msal4j.IClientCertificate
 *  com.microsoft.aad.msal4j.IClientCredential
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCertificate;
import com.microsoft.aad.msal4j.IClientCredential;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerTextFilter;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

public class PlayerSafetyServiceTextFilter
extends ServerTextFilter {
    private final ConfidentialClientApplication client;
    private final ClientCredentialParameters clientParameters;
    private final Set<String> fullyFilteredEvents;
    private final int connectionReadTimeoutMs;

    private PlayerSafetyServiceTextFilter(URL $$0, ServerTextFilter.MessageEncoder $$1, ServerTextFilter.IgnoreStrategy $$2, ExecutorService $$3, ConfidentialClientApplication $$4, ClientCredentialParameters $$5, Set<String> $$6, int $$7) {
        super($$0, $$1, $$2, $$3);
        this.client = $$4;
        this.clientParameters = $$5;
        this.fullyFilteredEvents = $$6;
        this.connectionReadTimeoutMs = $$7;
    }

    /*
     * WARNING - void declaration
     */
    public static @Nullable ServerTextFilter createTextFilterFromConfig(String $$0) {
        void $$29;
        void $$18;
        JsonObject $$12 = GsonHelper.parse($$0);
        URI $$22 = URI.create(GsonHelper.getAsString($$12, "apiServer"));
        String $$32 = GsonHelper.getAsString($$12, "apiPath");
        String $$4 = GsonHelper.getAsString($$12, "scope");
        String $$5 = GsonHelper.getAsString($$12, "serverId", "");
        String $$6 = GsonHelper.getAsString($$12, "applicationId");
        String $$7 = GsonHelper.getAsString($$12, "tenantId");
        String $$8 = GsonHelper.getAsString($$12, "roomId", "Java:Chat");
        String $$9 = GsonHelper.getAsString($$12, "certificatePath");
        String $$10 = GsonHelper.getAsString($$12, "certificatePassword", "");
        int $$11 = GsonHelper.getAsInt($$12, "hashesToDrop", -1);
        int $$122 = GsonHelper.getAsInt($$12, "maxConcurrentRequests", 7);
        JsonArray $$13 = GsonHelper.getAsJsonArray($$12, "fullyFilteredEvents");
        HashSet<String> $$14 = new HashSet<String>();
        $$13.forEach($$1 -> $$14.add(GsonHelper.convertToString($$1, "filteredEvent")));
        int $$15 = GsonHelper.getAsInt($$12, "connectionReadTimeoutMs", 2000);
        try {
            URL $$16 = $$22.resolve($$32).toURL();
        }
        catch (MalformedURLException $$17) {
            throw new RuntimeException($$17);
        }
        ServerTextFilter.MessageEncoder $$19 = ($$2, $$3) -> {
            JsonObject $$4 = new JsonObject();
            $$4.addProperty("userId", $$2.id().toString());
            $$4.addProperty("userDisplayName", $$2.name());
            $$4.addProperty("server", $$5);
            $$4.addProperty("room", $$8);
            $$4.addProperty("area", "JavaChatRealms");
            $$4.addProperty("data", $$3);
            $$4.addProperty("language", "*");
            return $$4;
        };
        ServerTextFilter.IgnoreStrategy $$20 = ServerTextFilter.IgnoreStrategy.select($$11);
        ExecutorService $$21 = PlayerSafetyServiceTextFilter.createWorkerPool($$122);
        try (InputStream $$222 = Files.newInputStream(Path.of($$9, new String[0]), new OpenOption[0]);){
            IClientCertificate $$23 = ClientCredentialFactory.createFromCertificate((InputStream)$$222, (String)$$10);
        }
        catch (Exception $$25) {
            LOGGER.warn("Failed to open certificate file");
            return null;
        }
        try {
            void $$26;
            ConfidentialClientApplication $$27 = ((ConfidentialClientApplication.Builder)((ConfidentialClientApplication.Builder)ConfidentialClientApplication.builder((String)$$6, (IClientCredential)$$26).sendX5c(true).executorService($$21)).authority(String.format(Locale.ROOT, "https://login.microsoftonline.com/%s/", $$7))).build();
        }
        catch (Exception $$28) {
            LOGGER.warn("Failed to create confidential client application");
            return null;
        }
        ClientCredentialParameters $$30 = ClientCredentialParameters.builder(Set.of($$4)).build();
        return new PlayerSafetyServiceTextFilter((URL)$$18, $$19, $$20, $$21, (ConfidentialClientApplication)$$29, $$30, $$14, $$15);
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
        }
        boolean $$4 = GsonHelper.getAsBoolean($$3, "filtered", true);
        if (!$$4) {
            return FilteredText.passThrough($$0);
        }
        JsonArray $$5 = GsonHelper.getAsJsonArray($$3, "events", new JsonArray());
        for (JsonElement $$6 : $$5) {
            JsonObject $$7 = $$6.getAsJsonObject();
            String $$8 = GsonHelper.getAsString($$7, "id", "");
            if (!this.fullyFilteredEvents.contains($$8)) continue;
            return FilteredText.fullyFiltered($$0);
        }
        JsonArray $$9 = GsonHelper.getAsJsonArray($$3, "redactedTextIndex", new JsonArray());
        return new FilteredText($$0, this.parseMask($$0, $$9, $$1));
    }

    @Override
    protected int connectionReadTimeout() {
        return this.connectionReadTimeoutMs;
    }
}

