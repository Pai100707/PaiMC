package net.minecraft.server.dedicated;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.jsonrpc.security.SecurityConfig;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

public class DedicatedServerProperties extends Settings<DedicatedServerProperties> {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
   private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();
   public static final String MANAGEMENT_SERVER_TLS_ENABLED_KEY = "management-server-tls-enabled";
   public static final String MANAGEMENT_SERVER_TLS_KEYSTORE_KEY = "management-server-tls-keystore";
   public static final String MANAGEMENT_SERVER_TLS_KEYSTORE_PASSWORD_KEY = "management-server-tls-keystore-password";
   public final boolean onlineMode = this.get("online-mode", true);
   public final boolean preventProxyConnections = this.get("prevent-proxy-connections", false);
   public final String serverIp = this.get("server-ip", "");
   public final Settings<DedicatedServerProperties>.MutableValue<Boolean> allowFlight = this.getMutable("allow-flight", false);
   public final Settings<DedicatedServerProperties>.MutableValue<String> motd = this.getMutable("motd", "A Minecraft Server");
   public final boolean codeOfConduct = this.get("enable-code-of-conduct", false);
   public final String bugReportLink = this.get("bug-report-link", "");
   public final Settings<DedicatedServerProperties>.MutableValue<Boolean> forceGameMode = this.getMutable("force-gamemode", false);
   public final Settings<DedicatedServerProperties>.MutableValue<Boolean> enforceWhitelist = this.getMutable("enforce-whitelist", false);
   public final Settings<DedicatedServerProperties>.MutableValue<Difficulty> difficulty = this.getMutable(
      "difficulty", dispatchNumberOrString(Difficulty::byId, Difficulty::byName), Difficulty::getKey, Difficulty.EASY
   );
   public final Settings<DedicatedServerProperties>.MutableValue<GameType> gameMode = this.getMutable(
      "gamemode", dispatchNumberOrString(GameType::byId, GameType::byName), GameType::getName, GameType.SURVIVAL
   );
   public final String levelName = this.get("level-name", "world");
   public final int serverPort = this.get("server-port", 25565);
   public final boolean managementServerEnabled = this.get("management-server-enabled", false);
   public final String managementServerHost = this.get("management-server-host", "localhost");
   public final int managementServerPort = this.get("management-server-port", 0);
   public final String managementServerSecret = this.get("management-server-secret", SecurityConfig.generateSecretKey());
   public final boolean managementServerTlsEnabled = this.get("management-server-tls-enabled", true);
   public final String managementServerTlsKeystore = this.get("management-server-tls-keystore", "");
   public final String managementServerTlsKeystorePassword = this.get("management-server-tls-keystore-password", "");
   public final String managementServerAllowedOrigins = this.get("management-server-allowed-origins", "");
   
   public final Boolean announcePlayerAchievements = this.getLegacyBoolean("announce-player-achievements");
   public final boolean enableQuery = this.get("enable-query", false);
   public final int queryPort = this.get("query.port", 25565);
   public final boolean enableRcon = this.get("enable-rcon", false);
   public final int rconPort = this.get("rcon.port", 25575);
   public final String rconPassword = this.get("rcon.password", "");
   public final boolean hardcore = this.get("hardcore", false);
   public final boolean useNativeTransport = this.get("use-native-transport", true);
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> spawnProtection = this.getMutable("spawn-protection", 16);
   public final Settings<DedicatedServerProperties>.MutableValue<LevelBasedPermissionSet> opPermissions = this.getMutable(
      "op-permission-level", DedicatedServerProperties::deserializePermission, DedicatedServerProperties::serializePermission, LevelBasedPermissionSet.OWNER
   );
   public final LevelBasedPermissionSet functionPermissions = this.get(
      "function-permission-level",
      DedicatedServerProperties::deserializePermission,
      DedicatedServerProperties::serializePermission,
      LevelBasedPermissionSet.GAMEMASTER
   );
   public final long maxTickTime = this.get("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
   public final int maxChainedNeighborUpdates = this.get("max-chained-neighbor-updates", 1000000);
   public final int rateLimitPacketsPerSecond = this.get("rate-limit", 0);
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> viewDistance = this.getMutable("view-distance", 10);
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> simulationDistance = this.getMutable("simulation-distance", 10);
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> maxPlayers = this.getMutable("max-players", 20);
   public final int networkCompressionThreshold = this.get("network-compression-threshold", 256);
   public final boolean broadcastRconToOps = this.get("broadcast-rcon-to-ops", true);
   public final boolean broadcastConsoleToOps = this.get("broadcast-console-to-ops", true);
   public final int maxWorldSize = this.get("max-world-size", $$0x -> Mth.clamp($$0x, 1, 29999984), 29999984);
   public final boolean syncChunkWrites = this.get("sync-chunk-writes", true);
   public final String regionFileComression = this.get("region-file-compression", "deflate");
   public final boolean enableJmxMonitoring = this.get("enable-jmx-monitoring", false);
   public final Settings<DedicatedServerProperties>.MutableValue<Boolean> enableStatus = this.getMutable("enable-status", true);
   public final Settings<DedicatedServerProperties>.MutableValue<Boolean> hideOnlinePlayers = this.getMutable("hide-online-players", false);
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> entityBroadcastRangePercentage = this.getMutable(
      "entity-broadcast-range-percentage", $$0x -> Mth.clamp(Integer.parseInt($$0x), 10, 1000), 100
   );
   public final String textFilteringConfig = this.get("text-filtering-config", "");
   public final int textFilteringVersion = this.get("text-filtering-version", 0);
   public final Optional<net.minecraft.server.MinecraftServer.ServerResourcePackInfo> serverResourcePackInfo;
   public final DataPackConfig initialDataPackConfiguration;
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> playerIdleTimeout = this.getMutable("player-idle-timeout", 0);
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> statusHeartbeatInterval = this.getMutable("status-heartbeat-interval", 0);
   public final Settings<DedicatedServerProperties>.MutableValue<Boolean> whiteList = this.getMutable("white-list", false);
   public final boolean enforceSecureProfile = this.get("enforce-secure-profile", true);
   public final boolean logIPs = this.get("log-ips", true);
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> pauseWhenEmptySeconds = this.getMutable("pause-when-empty-seconds", 60);
   private final DedicatedServerProperties.WorldDimensionData worldDimensionData;
   public final WorldOptions worldOptions;
   public Settings<DedicatedServerProperties>.MutableValue<Boolean> acceptsTransfers = this.getMutable("accepts-transfers", false);

   public DedicatedServerProperties(Properties $$0) {
      super($$0);
      String $$1 = this.get("level-seed", "");
      boolean $$2 = this.get("generate-structures", true);
      long $$3 = WorldOptions.parseSeed($$1).orElse(WorldOptions.randomSeed());
      this.worldOptions = new WorldOptions($$3, $$2, false);
      this.worldDimensionData = new DedicatedServerProperties.WorldDimensionData(
         this.get("generator-settings", $$0x -> GsonHelper.parse(!$$0x.isEmpty() ? $$0x : "{}"), new JsonObject()),
         this.get("level-type", $$0x -> $$0x.toLowerCase(Locale.ROOT), WorldPresets.NORMAL.identifier().toString())
      );
      this.serverResourcePackInfo = getServerPackInfo(
         this.get("resource-pack-id", ""),
         this.get("resource-pack", ""),
         this.get("resource-pack-sha1", ""),
         this.getLegacyString("resource-pack-hash"),
         this.get("require-resource-pack", false),
         this.get("resource-pack-prompt", "")
      );
      this.initialDataPackConfiguration = getDatapackConfig(
         this.get("initial-enabled-packs", String.join(",", WorldDataConfiguration.DEFAULT.dataPacks().getEnabled())),
         this.get("initial-disabled-packs", String.join(",", WorldDataConfiguration.DEFAULT.dataPacks().getDisabled()))
      );
   }

   public static DedicatedServerProperties fromFile(Path $$0) {
      return new DedicatedServerProperties(loadFromFile($$0));
   }

   protected DedicatedServerProperties reload(RegistryAccess $$0, Properties $$1) {
      return new DedicatedServerProperties($$1);
   }

   
   private static Component parseResourcePackPrompt(String $$0) {
      if (!Strings.isNullOrEmpty($$0)) {
         try {
            JsonElement $$1 = StrictJsonParser.parse($$0);
            return (Component)ComponentSerialization.CODEC
               .parse(RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE), $$1)
               .resultOrPartial($$1x -> LOGGER.warn("Failed to parse resource pack prompt '{}': {}", $$0, $$1x))
               .orElse(null);
         } catch (Exception var2) {
            LOGGER.warn("Failed to parse resource pack prompt '{}'", $$0, var2);
         }
      }

      return null;
   }

   private static Optional<net.minecraft.server.MinecraftServer.ServerResourcePackInfo> getServerPackInfo(
      String $$0, String $$1, String $$2, String $$3, boolean $$4, String $$5
   ) {
      if ($$1.isEmpty()) {
         return Optional.empty();
      } else {
         String $$6;
         if (!$$2.isEmpty()) {
            $$6 = $$2;
            if (!Strings.isNullOrEmpty($$3)) {
               LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
            }
         } else if (!Strings.isNullOrEmpty($$3)) {
            LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
            $$6 = $$3;
         } else {
            $$6 = "";
         }

         if ($$6.isEmpty()) {
            LOGGER.warn(
               "You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack."
            );
         } else if (!SHA1.matcher($$6).matches()) {
            LOGGER.warn("Invalid sha1 for resource-pack-sha1");
         }

         Component $$9 = parseResourcePackPrompt($$5);
         UUID $$10;
         if ($$0.isEmpty()) {
            $$10 = UUID.nameUUIDFromBytes($$1.getBytes(StandardCharsets.UTF_8));
            LOGGER.warn("resource-pack-id missing, using default of {}", $$10);
         } else {
            try {
               $$10 = UUID.fromString($$0);
            } catch (IllegalArgumentException var10) {
               LOGGER.warn("Failed to parse '{}' into UUID", $$0);
               return Optional.empty();
            }
         }

         return Optional.of(new net.minecraft.server.MinecraftServer.ServerResourcePackInfo($$10, $$1, $$6, $$4, $$9));
      }
   }

   private static DataPackConfig getDatapackConfig(String $$0, String $$1) {
      List<String> $$2 = COMMA_SPLITTER.splitToList($$0);
      List<String> $$3 = COMMA_SPLITTER.splitToList($$1);
      return new DataPackConfig($$2, $$3);
   }

   
   public static LevelBasedPermissionSet deserializePermission(String $$0) {
      try {
         PermissionLevel $$1 = PermissionLevel.byId(Integer.parseInt($$0));
         return LevelBasedPermissionSet.forLevel($$1);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   public static String serializePermission(LevelBasedPermissionSet $$0) {
      return Integer.toString($$0.level().id());
   }

   public WorldDimensions createDimensions(Provider $$0) {
      return this.worldDimensionData.create($$0);
   }

   record WorldDimensionData(JsonObject generatorSettings, String levelType) {
      private static final Map<String, ResourceKey<WorldPreset>> LEGACY_PRESET_NAMES = Map.of(
         "default", WorldPresets.NORMAL, "largebiomes", WorldPresets.LARGE_BIOMES
      );

      public WorldDimensions create(Provider $$0) {
         HolderLookup<WorldPreset> $$1 = $$0.lookupOrThrow(Registries.WORLD_PRESET);
         Reference<WorldPreset> $$2 = $$1.get(WorldPresets.NORMAL)
            .or(() -> $$1.listElements().findAny())
            .orElseThrow(() -> new IllegalStateException("Invalid datapack contents: can't find default preset"));
         Holder<WorldPreset> $$3 = Optional.ofNullable(Identifier.tryParse(this.levelType))
            .map($$0x -> ResourceKey.create(Registries.WORLD_PRESET, $$0x))
            .or(() -> Optional.ofNullable((ResourceKey)LEGACY_PRESET_NAMES.get(this.levelType)))
            .<Holder<WorldPreset>>flatMap($$1::get)
            .orElseGet(() -> {
               DedicatedServerProperties.LOGGER.warn("Failed to parse level-type {}, defaulting to {}", this.levelType, $$2.key().identifier());
               return $$2;
            });
         WorldDimensions $$4 = ((WorldPreset)$$3.value()).createWorldDimensions();
         if ($$3.is(WorldPresets.FLAT)) {
            RegistryOps<JsonElement> $$5 = $$0.createSerializationContext(JsonOps.INSTANCE);
            Optional<FlatLevelGeneratorSettings> $$6 = FlatLevelGeneratorSettings.CODEC
               .parse(new Dynamic($$5, this.generatorSettings()))
               .resultOrPartial(DedicatedServerProperties.LOGGER::error);
            if ($$6.isPresent()) {
               return $$4.replaceOverworldGenerator($$0, new FlatLevelSource($$6.get()));
            }
         }

         return $$4;
      }
   }
}
