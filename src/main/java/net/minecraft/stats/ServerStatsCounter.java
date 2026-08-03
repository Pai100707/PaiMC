package net.minecraft.stats;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class ServerStatsCounter extends net.minecraft.stats.StatsCounter {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Codec<Map<net.minecraft.stats.Stat<?>, Integer>> STATS_CODEC = Codec.dispatchedMap(
         BuiltInRegistries.STAT_TYPE.byNameCodec(), Util.memoize(net.minecraft.stats.ServerStatsCounter::createTypedStatsCodec)
      )
      .xmap($$0 -> {
         Map<net.minecraft.stats.Stat<?>, Integer> $$1 = new HashMap<>();
         $$0.forEach(($$1x, $$2) -> $$1.putAll((Map<? extends net.minecraft.stats.Stat<?>, ? extends Integer>)$$2));
         return $$1;
      }, $$0 -> $$0.entrySet().stream().collect(Collectors.groupingBy($$0x -> ((net.minecraft.stats.Stat)$$0x.getKey()).getType(), Util.toMap())));
   private final Path file;
   private final Set<net.minecraft.stats.Stat<?>> dirty = Sets.newHashSet();

   private static <T> Codec<Map<net.minecraft.stats.Stat<?>, Integer>> createTypedStatsCodec(net.minecraft.stats.StatType<T> $$0) {
      Codec<T> $$1 = $$0.getRegistry().byNameCodec();
      Codec<net.minecraft.stats.Stat<?>> $$2 = $$1.flatComapMap(
         $$0::get,
         $$1x -> $$1x.getType() == $$0 ? DataResult.success($$1x.getValue()) : DataResult.error(() -> "Expected type " + $$0 + ", but got " + $$1x.getType())
      );
      return Codec.unboundedMap($$2, Codec.INT);
   }

   public ServerStatsCounter(MinecraftServer $$0, Path $$1) {
      this.file = $$1;
      if (Files.isRegularFile($$1)) {
         try (Reader $$2 = Files.newBufferedReader($$1, StandardCharsets.UTF_8)) {
            JsonElement $$3 = StrictJsonParser.parse($$2);
            this.parse($$0.getFixerUpper(), $$3);
         } catch (IOException var8) {
            LOGGER.error("Couldn't read statistics file {}", $$1, var8);
         } catch (JsonParseException var9) {
            LOGGER.error("Couldn't parse statistics file {}", $$1, var9);
         }
      }
   }

   public void save() {
      try {
         FileUtil.createDirectoriesSafe(this.file.getParent());

         try (Writer $$0 = Files.newBufferedWriter(this.file, StandardCharsets.UTF_8)) {
            GSON.toJson(this.toJson(), GSON.newJsonWriter($$0));
         }
      } catch (JsonIOException | IOException var6) {
         LOGGER.error("Couldn't save stats to {}", this.file, var6);
      }
   }

   @Override
   public void setValue(Player $$0, net.minecraft.stats.Stat<?> $$1, int $$2) {
      super.setValue($$0, $$1, $$2);
      this.dirty.add($$1);
   }

   private Set<net.minecraft.stats.Stat<?>> getDirty() {
      Set<net.minecraft.stats.Stat<?>> $$0 = Sets.newHashSet(this.dirty);
      this.dirty.clear();
      return $$0;
   }

   public void parse(DataFixer $$0, JsonElement $$1) {
      Dynamic<JsonElement> $$2 = new Dynamic(JsonOps.INSTANCE, $$1);
      $$2 = DataFixTypes.STATS.updateToCurrentVersion($$0, $$2, NbtUtils.getDataVersion($$2, 1343));
      this.stats
         .putAll(
            STATS_CODEC.parse($$2.get("stats").orElseEmptyMap())
               .resultOrPartial($$0x -> LOGGER.error("Failed to parse statistics for {}: {}", this.file, $$0x))
               .orElse(Map.of())
         );
   }

   protected JsonElement toJson() {
      JsonObject $$0 = new JsonObject();
      $$0.add("stats", (JsonElement)STATS_CODEC.encodeStart(JsonOps.INSTANCE, this.stats).getOrThrow());
      $$0.addProperty("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
      return $$0;
   }

   public void markAllDirty() {
      this.dirty.addAll(this.stats.keySet());
   }

   public void sendStats(ServerPlayer $$0) {
      Object2IntMap<net.minecraft.stats.Stat<?>> $$1 = new Object2IntOpenHashMap();

      for (net.minecraft.stats.Stat<?> $$2 : this.getDirty()) {
         $$1.put($$2, this.getValue($$2));
      }

      $$0.connection.send(new ClientboundAwardStatsPacket($$1));
   }
}
