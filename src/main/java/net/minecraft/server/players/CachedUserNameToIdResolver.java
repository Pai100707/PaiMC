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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class CachedUserNameToIdResolver implements UserNameToIdResolver {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int GAMEPROFILES_MRU_LIMIT = 1000;
   private static final int GAMEPROFILES_EXPIRATION_MONTHS = 1;
   private boolean resolveOfflineUsers = true;
   private final Map<String, CachedUserNameToIdResolver.GameProfileInfo> profilesByName = new ConcurrentHashMap<>();
   private final Map<UUID, CachedUserNameToIdResolver.GameProfileInfo> profilesByUUID = new ConcurrentHashMap<>();
   private final GameProfileRepository profileRepository;
   private final Gson gson = new GsonBuilder().create();
   private final File file;
   private final AtomicLong operationCount = new AtomicLong();

   public CachedUserNameToIdResolver(GameProfileRepository $$0, File $$1) {
      this.profileRepository = $$0;
      this.file = $$1;
      Lists.reverse(this.load()).forEach(this::safeAdd);
   }

   private void safeAdd(CachedUserNameToIdResolver.GameProfileInfo $$0) {
      NameAndId $$1 = $$0.nameAndId();
      $$0.setLastAccess(this.getNextOperation());
      this.profilesByName.put($$1.name().toLowerCase(Locale.ROOT), $$0);
      this.profilesByUUID.put($$1.id(), $$0);
   }

   private Optional<NameAndId> lookupGameProfile(GameProfileRepository $$0, String $$1) {
      if (!StringUtil.isValidPlayerName($$1)) {
         return this.createUnknownProfile($$1);
      } else {
         Optional<NameAndId> $$2 = $$0.findProfileByName($$1).map(NameAndId::new);
         return $$2.isEmpty() ? this.createUnknownProfile($$1) : $$2;
      }
   }

   private Optional<NameAndId> createUnknownProfile(String $$0) {
      return this.resolveOfflineUsers ? Optional.of(NameAndId.createOffline($$0)) : Optional.empty();
   }

   @Override
   public void resolveOfflineUsers(boolean $$0) {
      this.resolveOfflineUsers = $$0;
   }

   @Override
   public void add(NameAndId $$0) {
      this.addInternal($$0);
   }

   private CachedUserNameToIdResolver.GameProfileInfo addInternal(NameAndId $$0) {
      Calendar $$1 = Calendar.getInstance(TimeZone.getDefault(), Locale.ROOT);
      $$1.setTime(new Date());
      $$1.add(2, 1);
      Date $$2 = $$1.getTime();
      CachedUserNameToIdResolver.GameProfileInfo $$3 = new CachedUserNameToIdResolver.GameProfileInfo($$0, $$2);
      this.safeAdd($$3);
      this.save();
      return $$3;
   }

   private long getNextOperation() {
      return this.operationCount.incrementAndGet();
   }

   @Override
   public Optional<NameAndId> get(String $$0) {
      String $$1 = $$0.toLowerCase(Locale.ROOT);
      CachedUserNameToIdResolver.GameProfileInfo $$2 = this.profilesByName.get($$1);
      boolean $$3 = false;
      if ($$2 != null && new Date().getTime() >= $$2.expirationDate.getTime()) {
         this.profilesByUUID.remove($$2.nameAndId().id());
         this.profilesByName.remove($$2.nameAndId().name().toLowerCase(Locale.ROOT));
         $$3 = true;
         $$2 = null;
      }

      Optional<NameAndId> $$4;
      if ($$2 != null) {
         $$2.setLastAccess(this.getNextOperation());
         $$4 = Optional.of($$2.nameAndId());
      } else {
         Optional<NameAndId> $$5 = this.lookupGameProfile(this.profileRepository, $$1);
         if ($$5.isPresent()) {
            $$4 = Optional.of(this.addInternal($$5.get()).nameAndId());
            $$3 = false;
         } else {
            $$4 = Optional.empty();
         }
      }

      if ($$3) {
         this.save();
      }

      return $$4;
   }

   @Override
   public Optional<NameAndId> get(UUID $$0) {
      CachedUserNameToIdResolver.GameProfileInfo $$1 = this.profilesByUUID.get($$0);
      if ($$1 == null) {
         return Optional.empty();
      } else {
         $$1.setLastAccess(this.getNextOperation());
         return Optional.of($$1.nameAndId());
      }
   }

   private static DateFormat createDateFormat() {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
   }

   private List<CachedUserNameToIdResolver.GameProfileInfo> load() {
      List<CachedUserNameToIdResolver.GameProfileInfo> $$0 = Lists.newArrayList();

      try {
         Object var9;
         try (Reader $$1 = Files.newReader(this.file, StandardCharsets.UTF_8)) {
            JsonArray $$2 = (JsonArray)this.gson.fromJson($$1, JsonArray.class);
            if ($$2 != null) {
               DateFormat $$3 = createDateFormat();
               $$2.forEach($$2x -> readGameProfile($$2x, $$3).ifPresent($$0::add));
               return $$0;
            }

            var9 = $$0;
         }

         return (List<CachedUserNameToIdResolver.GameProfileInfo>)var9;
      } catch (FileNotFoundException var7) {
      } catch (JsonParseException | IOException var8) {
         LOGGER.warn("Failed to load profile cache {}", this.file, var8);
      }

      return $$0;
   }

   @Override
   public void save() {
      JsonArray $$0 = new JsonArray();
      DateFormat $$1 = createDateFormat();
      this.getTopMRUProfiles(1000).forEach($$2x -> $$0.add(writeGameProfile($$2x, $$1)));
      String $$2 = this.gson.toJson($$0);

      try (Writer $$3 = Files.newWriter(this.file, StandardCharsets.UTF_8)) {
         $$3.write($$2);
      } catch (IOException var9) {
      }
   }

   private Stream<CachedUserNameToIdResolver.GameProfileInfo> getTopMRUProfiles(int $$0) {
      return ImmutableList.copyOf(this.profilesByUUID.values())
         .stream()
         .sorted(Comparator.comparing(CachedUserNameToIdResolver.GameProfileInfo::lastAccess).reversed())
         .limit($$0);
   }

   private static JsonElement writeGameProfile(CachedUserNameToIdResolver.GameProfileInfo $$0, DateFormat $$1) {
      JsonObject $$2 = new JsonObject();
      $$0.nameAndId().appendTo($$2);
      $$2.addProperty("expiresOn", $$1.format($$0.expirationDate()));
      return $$2;
   }

   private static Optional<CachedUserNameToIdResolver.GameProfileInfo> readGameProfile(JsonElement $$0, DateFormat $$1) {
      if ($$0.isJsonObject()) {
         JsonObject $$2 = $$0.getAsJsonObject();
         NameAndId $$3 = NameAndId.fromJson($$2);
         if ($$3 != null) {
            JsonElement $$4 = $$2.get("expiresOn");
            if ($$4 != null) {
               String $$5 = $$4.getAsString();

               try {
                  Date $$6 = $$1.parse($$5);
                  return Optional.of(new CachedUserNameToIdResolver.GameProfileInfo($$3, $$6));
               } catch (ParseException var7) {
                  LOGGER.warn("Failed to parse date {}", $$5, var7);
               }
            }
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
