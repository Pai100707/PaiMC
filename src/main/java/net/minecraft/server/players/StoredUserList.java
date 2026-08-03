package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final File file;
   private final Map<String, V> map = Maps.newHashMap();
   protected final NotificationService notificationService;

   public StoredUserList(File $$0, NotificationService $$1) {
      this.file = $$0;
      this.notificationService = $$1;
   }

   public File getFile() {
      return this.file;
   }

   public boolean add(V $$0) {
      String $$1 = this.getKeyForUser($$0.getUser());
      V $$2 = this.map.get($$1);
      if ($$0.equals($$2)) {
         return false;
      } else {
         this.map.put($$1, $$0);

         try {
            this.save();
         } catch (IOException var5) {
            LOGGER.warn("Could not save the list after adding a user.", var5);
         }

         return true;
      }
   }

   @Nullable
   public V get(K $$0) {
      this.removeExpired();
      return this.map.get(this.getKeyForUser($$0));
   }

   public boolean remove(K $$0) {
      V $$1 = this.map.remove(this.getKeyForUser($$0));
      if ($$1 == null) {
         return false;
      } else {
         try {
            this.save();
         } catch (IOException var4) {
            LOGGER.warn("Could not save the list after removing a user.", var4);
         }

         return true;
      }
   }

   public boolean remove(StoredUserEntry<K> $$0) {
      return this.remove(Objects.requireNonNull($$0.getUser()));
   }

   public void clear() {
      this.map.clear();

      try {
         this.save();
      } catch (IOException var2) {
         LOGGER.warn("Could not save the list after removing a user.", var2);
      }
   }

   public String[] getUserList() {
      return this.map.keySet().toArray(new String[0]);
   }

   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   protected String getKeyForUser(K $$0) {
      return $$0.toString();
   }

   protected boolean contains(K $$0) {
      return this.map.containsKey(this.getKeyForUser($$0));
   }

   private void removeExpired() {
      List<K> $$0 = Lists.newArrayList();

      for (V $$1 : this.map.values()) {
         if ($$1.hasExpired()) {
            $$0.add($$1.getUser());
         }
      }

      for (K $$2 : $$0) {
         this.map.remove(this.getKeyForUser($$2));
      }
   }

   protected abstract StoredUserEntry<K> createEntry(JsonObject var1);

   public Collection<V> getEntries() {
      return this.map.values();
   }

   public void save() throws IOException {
      JsonArray $$0 = new JsonArray();
      this.map.values().stream().map($$0x -> (JsonObject)Util.make(new JsonObject(), $$0x::serialize)).forEach($$0::add);

      try (BufferedWriter $$1 = Files.newWriter(this.file, StandardCharsets.UTF_8)) {
         GSON.toJson($$0, GSON.newJsonWriter($$1));
      }
   }

   public void load() throws IOException {
      if (this.file.exists()) {
         try (BufferedReader $$0 = Files.newReader(this.file, StandardCharsets.UTF_8)) {
            this.map.clear();
            JsonArray $$1 = (JsonArray)GSON.fromJson($$0, JsonArray.class);
            if ($$1 == null) {
               return;
            }

            for (JsonElement $$2 : $$1) {
               JsonObject $$3 = GsonHelper.convertToJsonObject($$2, "entry");
               StoredUserEntry<K> $$4 = this.createEntry($$3);
               if ($$4.getUser() != null) {
                  this.map.put(this.getKeyForUser($$4.getUser()), (V)$$4);
               }
            }
         }
      }
   }
}
