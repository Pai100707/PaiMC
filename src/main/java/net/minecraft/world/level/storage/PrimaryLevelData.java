package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.slf4j.Logger;

public class PrimaryLevelData implements ServerLevelData, WorldData {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String LEVEL_NAME = "LevelName";
   protected static final String PLAYER = "Player";
   protected static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
   private net.minecraft.world.level.LevelSettings settings;
   private final WorldOptions worldOptions;
   private final PrimaryLevelData.SpecialWorldProperty specialWorldProperty;
   private final Lifecycle worldGenSettingsLifecycle;
   private LevelData.RespawnData respawnData;
   private long gameTime;
   private long dayTime;
   
   private final CompoundTag loadedPlayerTag;
   private final int version;
   private int clearWeatherTime;
   private boolean raining;
   private int rainTime;
   private boolean thundering;
   private int thunderTime;
   private boolean initialized;
   private boolean difficultyLocked;
   @Deprecated
   private Optional<WorldBorder.Settings> legacyWorldBorderSettings;
   private EndDragonFight.Data endDragonFightData;
   
   private CompoundTag customBossEvents;
   private int wanderingTraderSpawnDelay;
   private int wanderingTraderSpawnChance;
   
   private UUID wanderingTraderId;
   private final Set<String> knownServerBrands;
   private boolean wasModded;
   private final Set<String> removedFeatureFlags;
   private final TimerQueue<MinecraftServer> scheduledEvents;

   private PrimaryLevelData(
      CompoundTag $$0,
      boolean $$1,
      LevelData.RespawnData $$2,
      long $$3,
      long $$4,
      int $$5,
      int $$6,
      int $$7,
      boolean $$8,
      int $$9,
      boolean $$10,
      boolean $$11,
      boolean $$12,
      Optional<WorldBorder.Settings> $$13,
      int $$14,
      int $$15,
      UUID $$16,
      Set<String> $$17,
      Set<String> $$18,
      TimerQueue<MinecraftServer> $$19,
      CompoundTag $$20,
      EndDragonFight.Data $$21,
      net.minecraft.world.level.LevelSettings $$22,
      WorldOptions $$23,
      PrimaryLevelData.SpecialWorldProperty $$24,
      Lifecycle $$25
   ) {
      this.wasModded = $$1;
      this.respawnData = $$2;
      this.gameTime = $$3;
      this.dayTime = $$4;
      this.version = $$5;
      this.clearWeatherTime = $$6;
      this.rainTime = $$7;
      this.raining = $$8;
      this.thunderTime = $$9;
      this.thundering = $$10;
      this.initialized = $$11;
      this.difficultyLocked = $$12;
      this.legacyWorldBorderSettings = $$13;
      this.wanderingTraderSpawnDelay = $$14;
      this.wanderingTraderSpawnChance = $$15;
      this.wanderingTraderId = $$16;
      this.knownServerBrands = $$17;
      this.removedFeatureFlags = $$18;
      this.loadedPlayerTag = $$0;
      this.scheduledEvents = $$19;
      this.customBossEvents = $$20;
      this.endDragonFightData = $$21;
      this.settings = $$22;
      this.worldOptions = $$23;
      this.specialWorldProperty = $$24;
      this.worldGenSettingsLifecycle = $$25;
   }

   public PrimaryLevelData(net.minecraft.world.level.LevelSettings $$0, WorldOptions $$1, PrimaryLevelData.SpecialWorldProperty $$2, Lifecycle $$3) {
      this(
         null,
         false,
         LevelData.RespawnData.DEFAULT,
         0L,
         0L,
         19133,
         0,
         0,
         false,
         0,
         false,
         false,
         false,
         Optional.empty(),
         0,
         0,
         null,
         Sets.newLinkedHashSet(),
         new HashSet<>(),
         new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS),
         null,
         EndDragonFight.Data.DEFAULT,
         $$0.copy(),
         $$1,
         $$2,
         $$3
      );
   }

   public static <T> PrimaryLevelData parse(
      Dynamic<T> $$0, net.minecraft.world.level.LevelSettings $$1, PrimaryLevelData.SpecialWorldProperty $$2, WorldOptions $$3, Lifecycle $$4
   ) {
      long $$5 = $$0.get("Time").asLong(0L);
      return new PrimaryLevelData(
         (CompoundTag)$$0.get("Player").flatMap(CompoundTag.CODEC::parse).result().orElse(null),
         $$0.get("WasModded").asBoolean(false),
         $$0.get("spawn").read(LevelData.RespawnData.CODEC).result().orElse(LevelData.RespawnData.DEFAULT),
         $$5,
         $$0.get("DayTime").asLong($$5),
         LevelVersion.parse($$0).levelDataVersion(),
         $$0.get("clearWeatherTime").asInt(0),
         $$0.get("rainTime").asInt(0),
         $$0.get("raining").asBoolean(false),
         $$0.get("thunderTime").asInt(0),
         $$0.get("thundering").asBoolean(false),
         $$0.get("initialized").asBoolean(true),
         $$0.get("DifficultyLocked").asBoolean(false),
         WorldBorder.Settings.CODEC.parse($$0.get("world_border").orElseEmptyMap()).result(),
         $$0.get("WanderingTraderSpawnDelay").asInt(0),
         $$0.get("WanderingTraderSpawnChance").asInt(0),
         (UUID)$$0.get("WanderingTraderId").read(UUIDUtil.CODEC).result().orElse(null),
         $$0.get("ServerBrands").asStream().flatMap($$0x -> $$0x.asString().result().stream()).collect(Collectors.toCollection(Sets::newLinkedHashSet)),
         $$0.get("removed_features").asStream().flatMap($$0x -> $$0x.asString().result().stream()).collect(Collectors.toSet()),
         new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS, $$0.get("ScheduledEvents").asStream()),
         (CompoundTag)$$0.get("CustomBossEvents").orElseEmptyMap().getValue(),
         $$0.get("DragonFight").read(EndDragonFight.Data.CODEC).resultOrPartial(LOGGER::error).orElse(EndDragonFight.Data.DEFAULT),
         $$1,
         $$3,
         $$2,
         $$4
      );
   }

   @Override
   public CompoundTag createTag(RegistryAccess $$0, CompoundTag $$1) {
      if ($$1 == null) {
         $$1 = this.loadedPlayerTag;
      }

      CompoundTag $$2 = new CompoundTag();
      this.setTagData($$0, $$2, $$1);
      return $$2;
   }

   private void setTagData(RegistryAccess $$0, CompoundTag $$1, CompoundTag $$2) {
      $$1.put("ServerBrands", stringCollectionToTag(this.knownServerBrands));
      $$1.putBoolean("WasModded", this.wasModded);
      if (!this.removedFeatureFlags.isEmpty()) {
         $$1.put("removed_features", stringCollectionToTag(this.removedFeatureFlags));
      }

      CompoundTag $$3 = new CompoundTag();
      $$3.putString("Name", SharedConstants.getCurrentVersion().name());
      $$3.putInt("Id", SharedConstants.getCurrentVersion().dataVersion().version());
      $$3.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().stable());
      $$3.putString("Series", SharedConstants.getCurrentVersion().dataVersion().series());
      $$1.put("Version", $$3);
      NbtUtils.addCurrentDataVersion($$1);
      DynamicOps<Tag> $$4 = $$0.createSerializationContext(NbtOps.INSTANCE);
      WorldGenSettings.encode($$4, this.worldOptions, $$0)
         .resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error))
         .ifPresent($$1x -> $$1.put("WorldGenSettings", $$1x));
      $$1.putInt("GameType", this.settings.gameType().getId());
      $$1.store("spawn", LevelData.RespawnData.CODEC, this.respawnData);
      $$1.putLong("Time", this.gameTime);
      $$1.putLong("DayTime", this.dayTime);
      $$1.putLong("LastPlayed", Util.getEpochMillis());
      $$1.putString("LevelName", this.settings.levelName());
      $$1.putInt("version", 19133);
      $$1.putInt("clearWeatherTime", this.clearWeatherTime);
      $$1.putInt("rainTime", this.rainTime);
      $$1.putBoolean("raining", this.raining);
      $$1.putInt("thunderTime", this.thunderTime);
      $$1.putBoolean("thundering", this.thundering);
      $$1.putBoolean("hardcore", this.settings.hardcore());
      $$1.putBoolean("allowCommands", this.settings.allowCommands());
      $$1.putBoolean("initialized", this.initialized);
      this.legacyWorldBorderSettings.ifPresent($$1x -> $$1.store("world_border", WorldBorder.Settings.CODEC, $$1x));
      $$1.putByte("Difficulty", (byte)this.settings.difficulty().getId());
      $$1.putBoolean("DifficultyLocked", this.difficultyLocked);
      $$1.store("game_rules", GameRules.codec(this.enabledFeatures()), this.settings.gameRules());
      $$1.store("DragonFight", EndDragonFight.Data.CODEC, this.endDragonFightData);
      if ($$2 != null) {
         $$1.put("Player", $$2);
      }

      $$1.store(net.minecraft.world.level.WorldDataConfiguration.MAP_CODEC, this.settings.getDataConfiguration());
      if (this.customBossEvents != null) {
         $$1.put("CustomBossEvents", this.customBossEvents);
      }

      $$1.put("ScheduledEvents", this.scheduledEvents.store());
      $$1.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
      $$1.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
      $$1.storeNullable("WanderingTraderId", UUIDUtil.CODEC, this.wanderingTraderId);
   }

   private static ListTag stringCollectionToTag(Set<String> $$0) {
      ListTag $$1 = new ListTag();
      $$0.stream().map(StringTag::valueOf).forEach($$1::add);
      return $$1;
   }

   @Override
   public LevelData.RespawnData getRespawnData() {
      return this.respawnData;
   }

   @Override
   public long getGameTime() {
      return this.gameTime;
   }

   @Override
   public long getDayTime() {
      return this.dayTime;
   }

   
   @Override
   public CompoundTag getLoadedPlayerTag() {
      return this.loadedPlayerTag;
   }

   @Override
   public void setGameTime(long $$0) {
      this.gameTime = $$0;
   }

   @Override
   public void setDayTime(long $$0) {
      this.dayTime = $$0;
   }

   @Override
   public void setSpawn(LevelData.RespawnData $$0) {
      this.respawnData = $$0;
   }

   @Override
   public String getLevelName() {
      return this.settings.levelName();
   }

   @Override
   public int getVersion() {
      return this.version;
   }

   @Override
   public int getClearWeatherTime() {
      return this.clearWeatherTime;
   }

   @Override
   public void setClearWeatherTime(int $$0) {
      this.clearWeatherTime = $$0;
   }

   @Override
   public boolean isThundering() {
      return this.thundering;
   }

   @Override
   public void setThundering(boolean $$0) {
      this.thundering = $$0;
   }

   @Override
   public int getThunderTime() {
      return this.thunderTime;
   }

   @Override
   public void setThunderTime(int $$0) {
      this.thunderTime = $$0;
   }

   @Override
   public boolean isRaining() {
      return this.raining;
   }

   @Override
   public void setRaining(boolean $$0) {
      this.raining = $$0;
   }

   @Override
   public int getRainTime() {
      return this.rainTime;
   }

   @Override
   public void setRainTime(int $$0) {
      this.rainTime = $$0;
   }

   @Override
   public net.minecraft.world.level.GameType getGameType() {
      return this.settings.gameType();
   }

   @Override
   public void setGameType(net.minecraft.world.level.GameType $$0) {
      this.settings = this.settings.withGameType($$0);
   }

   @Override
   public boolean isHardcore() {
      return this.settings.hardcore();
   }

   @Override
   public boolean isAllowCommands() {
      return this.settings.allowCommands();
   }

   @Override
   public boolean isInitialized() {
      return this.initialized;
   }

   @Override
   public void setInitialized(boolean $$0) {
      this.initialized = $$0;
   }

   @Override
   public GameRules getGameRules() {
      return this.settings.gameRules();
   }

   @Override
   public Optional<WorldBorder.Settings> getLegacyWorldBorderSettings() {
      return this.legacyWorldBorderSettings;
   }

   @Override
   public void setLegacyWorldBorderSettings(Optional<WorldBorder.Settings> $$0) {
      this.legacyWorldBorderSettings = $$0;
   }

   @Override
   public Difficulty getDifficulty() {
      return this.settings.difficulty();
   }

   @Override
   public void setDifficulty(Difficulty $$0) {
      this.settings = this.settings.withDifficulty($$0);
   }

   @Override
   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   @Override
   public void setDifficultyLocked(boolean $$0) {
      this.difficultyLocked = $$0;
   }

   @Override
   public TimerQueue<MinecraftServer> getScheduledEvents() {
      return this.scheduledEvents;
   }

   @Override
   public void fillCrashReportCategory(CrashReportCategory $$0, net.minecraft.world.level.LevelHeightAccessor $$1) {
      ServerLevelData.super.fillCrashReportCategory($$0, $$1);
      WorldData.super.fillCrashReportCategory($$0);
   }

   @Override
   public WorldOptions worldGenOptions() {
      return this.worldOptions;
   }

   @Override
   public boolean isFlatWorld() {
      return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.FLAT;
   }

   @Override
   public boolean isDebugWorld() {
      return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.DEBUG;
   }

   @Override
   public Lifecycle worldGenSettingsLifecycle() {
      return this.worldGenSettingsLifecycle;
   }

   @Override
   public EndDragonFight.Data endDragonFightData() {
      return this.endDragonFightData;
   }

   @Override
   public void setEndDragonFightData(EndDragonFight.Data $$0) {
      this.endDragonFightData = $$0;
   }

   @Override
   public net.minecraft.world.level.WorldDataConfiguration getDataConfiguration() {
      return this.settings.getDataConfiguration();
   }

   @Override
   public void setDataConfiguration(net.minecraft.world.level.WorldDataConfiguration $$0) {
      this.settings = this.settings.withDataConfiguration($$0);
   }

   
   @Override
   public CompoundTag getCustomBossEvents() {
      return this.customBossEvents;
   }

   @Override
   public void setCustomBossEvents(CompoundTag $$0) {
      this.customBossEvents = $$0;
   }

   @Override
   public int getWanderingTraderSpawnDelay() {
      return this.wanderingTraderSpawnDelay;
   }

   @Override
   public void setWanderingTraderSpawnDelay(int $$0) {
      this.wanderingTraderSpawnDelay = $$0;
   }

   @Override
   public int getWanderingTraderSpawnChance() {
      return this.wanderingTraderSpawnChance;
   }

   @Override
   public void setWanderingTraderSpawnChance(int $$0) {
      this.wanderingTraderSpawnChance = $$0;
   }

   
   @Override
   public UUID getWanderingTraderId() {
      return this.wanderingTraderId;
   }

   @Override
   public void setWanderingTraderId(UUID $$0) {
      this.wanderingTraderId = $$0;
   }

   @Override
   public void setModdedInfo(String $$0, boolean $$1) {
      this.knownServerBrands.add($$0);
      this.wasModded |= $$1;
   }

   @Override
   public boolean wasModded() {
      return this.wasModded;
   }

   @Override
   public Set<String> getKnownServerBrands() {
      return ImmutableSet.copyOf(this.knownServerBrands);
   }

   @Override
   public Set<String> getRemovedFeatureFlags() {
      return Set.copyOf(this.removedFeatureFlags);
   }

   @Override
   public ServerLevelData overworldData() {
      return this;
   }

   @Override
   public net.minecraft.world.level.LevelSettings getLevelSettings() {
      return this.settings.copy();
   }

   @Deprecated
   public static enum SpecialWorldProperty {
      NONE,
      FLAT,
      DEBUG;
   }
}
