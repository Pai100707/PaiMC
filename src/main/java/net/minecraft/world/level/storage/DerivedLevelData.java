package net.minecraft.world.level.storage;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.timers.TimerQueue;

public class DerivedLevelData implements ServerLevelData {
   private final WorldData worldData;
   private final ServerLevelData wrapped;

   public DerivedLevelData(WorldData $$0, ServerLevelData $$1) {
      this.worldData = $$0;
      this.wrapped = $$1;
   }

   @Override
   public LevelData.RespawnData getRespawnData() {
      return this.wrapped.getRespawnData();
   }

   @Override
   public long getGameTime() {
      return this.wrapped.getGameTime();
   }

   @Override
   public long getDayTime() {
      return this.wrapped.getDayTime();
   }

   @Override
   public String getLevelName() {
      return this.worldData.getLevelName();
   }

   @Override
   public int getClearWeatherTime() {
      return this.wrapped.getClearWeatherTime();
   }

   @Override
   public void setClearWeatherTime(int $$0) {
   }

   @Override
   public boolean isThundering() {
      return this.wrapped.isThundering();
   }

   @Override
   public int getThunderTime() {
      return this.wrapped.getThunderTime();
   }

   @Override
   public boolean isRaining() {
      return this.wrapped.isRaining();
   }

   @Override
   public int getRainTime() {
      return this.wrapped.getRainTime();
   }

   @Override
   public net.minecraft.world.level.GameType getGameType() {
      return this.worldData.getGameType();
   }

   @Override
   public void setGameTime(long $$0) {
   }

   @Override
   public void setDayTime(long $$0) {
   }

   @Override
   public void setSpawn(LevelData.RespawnData $$0) {
      this.wrapped.setSpawn($$0);
   }

   @Override
   public void setThundering(boolean $$0) {
   }

   @Override
   public void setThunderTime(int $$0) {
   }

   @Override
   public void setRaining(boolean $$0) {
   }

   @Override
   public void setRainTime(int $$0) {
   }

   @Override
   public void setGameType(net.minecraft.world.level.GameType $$0) {
   }

   @Override
   public boolean isHardcore() {
      return this.worldData.isHardcore();
   }

   @Override
   public boolean isAllowCommands() {
      return this.worldData.isAllowCommands();
   }

   @Override
   public boolean isInitialized() {
      return this.wrapped.isInitialized();
   }

   @Override
   public void setInitialized(boolean $$0) {
   }

   @Override
   public GameRules getGameRules() {
      return this.worldData.getGameRules();
   }

   @Override
   public Optional<WorldBorder.Settings> getLegacyWorldBorderSettings() {
      return this.wrapped.getLegacyWorldBorderSettings();
   }

   @Override
   public void setLegacyWorldBorderSettings(Optional<WorldBorder.Settings> $$0) {
   }

   @Override
   public Difficulty getDifficulty() {
      return this.worldData.getDifficulty();
   }

   @Override
   public boolean isDifficultyLocked() {
      return this.worldData.isDifficultyLocked();
   }

   @Override
   public TimerQueue<MinecraftServer> getScheduledEvents() {
      return this.wrapped.getScheduledEvents();
   }

   @Override
   public int getWanderingTraderSpawnDelay() {
      return 0;
   }

   @Override
   public void setWanderingTraderSpawnDelay(int $$0) {
   }

   @Override
   public int getWanderingTraderSpawnChance() {
      return 0;
   }

   @Override
   public void setWanderingTraderSpawnChance(int $$0) {
   }

   @Override
   public UUID getWanderingTraderId() {
      return null;
   }

   @Override
   public void setWanderingTraderId(UUID $$0) {
   }

   @Override
   public void fillCrashReportCategory(CrashReportCategory $$0, net.minecraft.world.level.LevelHeightAccessor $$1) {
      $$0.setDetail("Derived", true);
      this.wrapped.fillCrashReportCategory($$0, $$1);
   }
}
