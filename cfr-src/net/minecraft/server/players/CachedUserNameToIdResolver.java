/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.io.Files
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.authlib.GameProfileRepository
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.players;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class CachedUserNameToIdResolver
implements UserNameToIdResolver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int GAMEPROFILES_MRU_LIMIT = 1000;
    private static final int GAMEPROFILES_EXPIRATION_MONTHS = 1;
    private boolean resolveOfflineUsers = true;
    private final Map<String, GameProfileInfo> profilesByName = new ConcurrentHashMap<String, GameProfileInfo>();
    private final Map<UUID, GameProfileInfo> profilesByUUID = new ConcurrentHashMap<UUID, GameProfileInfo>();
    private final GameProfileRepository profileRepository;
    private final Gson gson = new GsonBuilder().create();
    private final File file;
    private final AtomicLong operationCount = new AtomicLong();

    public CachedUserNameToIdResolver(GameProfileRepository $$0, File $$1) {
        this.profileRepository = $$0;
        this.file = $$1;
        Lists.reverse(this.load()).forEach(this::safeAdd);
    }

    private void safeAdd(GameProfileInfo $$0) {
        NameAndId $$1 = $$0.nameAndId();
        $$0.setLastAccess(this.getNextOperation());
        this.profilesByName.put($$1.name().toLowerCase(Locale.ROOT), $$0);
        this.profilesByUUID.put($$1.id(), $$0);
    }

    private Optional<NameAndId> lookupGameProfile(GameProfileRepository $$0, String $$1) {
        if (!StringUtil.isValidPlayerName($$1)) {
            return this.createUnknownProfile($$1);
        }
        Optional<NameAndId> $$2 = $$0.findProfileByName($$1).map(NameAndId::new);
        if ($$2.isEmpty()) {
            return this.createUnknownProfile($$1);
        }
        return $$2;
    }

    private Optional<NameAndId> createUnknownProfile(String $$0) {
        if (this.resolveOfflineUsers) {
            return Optional.of(NameAndId.createOffline($$0));
        }
        return Optional.empty();
    }

    @Override
    public void resolveOfflineUsers(boolean $$0) {
        this.resolveOfflineUsers = $$0;
    }

    @Override
    public void add(NameAndId $$0) {
        this.addInternal($$0);
    }

    private GameProfileInfo addInternal(NameAndId $$0) {
        Calendar $$1 = Calendar.getInstance(TimeZone.getDefault(), Locale.ROOT);
        $$1.setTime(new Date());
        $$1.add(2, 1);
        Date $$2 = $$1.getTime();
        GameProfileInfo $$3 = new GameProfileInfo($$0, $$2);
        this.safeAdd($$3);
        this.save();
        return $$3;
    }

    private long getNextOperation() {
        return this.operationCount.incrementAndGet();
    }

    @Override
    public Optional<NameAndId> get(String $$0) {
        Optional<NameAndId> $$7;
        String $$1 = $$0.toLowerCase(Locale.ROOT);
        GameProfileInfo $$2 = this.profilesByName.get($$1);
        boolean $$3 = false;
        if ($$2 != null && new Date().getTime() >= $$2.expirationDate.getTime()) {
            this.profilesByUUID.remove($$2.nameAndId().id());
            this.profilesByName.remove($$2.nameAndId().name().toLowerCase(Locale.ROOT));
            $$3 = true;
            $$2 = null;
        }
        if ($$2 != null) {
            $$2.setLastAccess(this.getNextOperation());
            Optional<NameAndId> $$4 = Optional.of($$2.nameAndId());
        } else {
            Optional<NameAndId> $$5 = this.lookupGameProfile(this.profileRepository, $$1);
            if ($$5.isPresent()) {
                Optional<NameAndId> $$6 = Optional.of(this.addInternal($$5.get()).nameAndId());
                $$3 = false;
            } else {
                $$7 = Optional.empty();
            }
        }
        if ($$3) {
            this.save();
        }
        return $$7;
    }

    @Override
    public Optional<NameAndId> get(UUID $$0) {
        GameProfileInfo $$1 = this.profilesByUUID.get($$0);
        if ($$1 == null) {
            return Optional.empty();
        }
        $$1.setLastAccess(this.getNextOperation());
        return Optional.of($$1.nameAndId());
    }

    private static DateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private List<GameProfileInfo> load() {
        ArrayList $$0 = Lists.newArrayList();
        try (BufferedReader $$12222 = Files.newReader((File)this.file, (Charset)StandardCharsets.UTF_8);){
            JsonArray $$22 = (JsonArray)this.gson.fromJson((Reader)$$12222, JsonArray.class);
            if ($$22 == null) {
                ArrayList arrayList = $$0;
                return arrayList;
            }
            DateFormat $$3 = CachedUserNameToIdResolver.createDateFormat();
            $$22.forEach($$2 -> CachedUserNameToIdResolver.readGameProfile($$2, $$3).ifPresent($$0::add));
            return $$0;
        }
        catch (FileNotFoundException $$12222) {
            return $$0;
        }
        catch (JsonParseException | IOException $$4) {
            LOGGER.warn("Failed to load profile cache {}", (Object)this.file, (Object)$$4);
        }
        return $$0;
    }

    @Override
    public void save() {
        JsonArray $$0 = new JsonArray();
        DateFormat $$1 = CachedUserNameToIdResolver.createDateFormat();
        this.getTopMRUProfiles(1000).forEach($$2 -> $$0.add(CachedUserNameToIdResolver.writeGameProfile($$2, $$1)));
        String $$22 = this.gson.toJson((JsonElement)$$0);
        try (BufferedWriter $$3 = Files.newWriter((File)this.file, (Charset)StandardCharsets.UTF_8);){
            $$3.write($$22);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private Stream<GameProfileInfo> getTopMRUProfiles(int $$0) {
        return ImmutableList.copyOf(this.profilesByUUID.values()).stream().sorted(Comparator.comparing(GameProfileInfo::lastAccess).reversed()).limit($$0);
    }

    private static JsonElement writeGameProfile(GameProfileInfo $$0, DateFormat $$1) {
        JsonObject $$2 = new JsonObject();
        $$0.nameAndId().appendTo($$2);
        $$2.addProperty("expiresOn", $$1.format($$0.expirationDate()));
        return $$2;
    }

    private static Optional<GameProfileInfo> readGameProfile(JsonElement $$0, DateFormat $$1) {
        JsonElement $$4;
        JsonObject $$2;
        NameAndId $$3;
        if ($$0.isJsonObject() && ($$3 = NameAndId.fromJson($$2 = $$0.getAsJsonObject())) != null && ($$4 = $$2.get("expiresOn")) != null) {
            String $$5 = $$4.getAsString();
            try {
                Date $$6 = $$1.parse($$5);
                return Optional.of(new GameProfileInfo($$3, $$6));
            }
            catch (ParseException $$7) {
                LOGGER.warn("Failed to parse date {}", (Object)$$5, (Object)$$7);
            }
        }
        return Optional.empty();
    }

    static class GameProfileInfo {
        private final NameAndId nameAndId;
        final Date expirationDate;
        private volatile long lastAccess;

        GameProfileInfo(NameAndId $$0, Date $$1) {
            this.nameAndId = $$0;
            this.expirationDate = $$1;
        }

        public NameAndId nameAndId() {
            return this.nameAndId;
        }

        public Date expirationDate() {
            return this.expirationDate;
        }

        public void setLastAccess(long $$0) {
            this.lastAccess = $$0;
        }

        public long lastAccess() {
            return this.lastAccess;
        }
    }
}

