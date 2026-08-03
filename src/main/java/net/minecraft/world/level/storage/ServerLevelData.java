package net.minecraft.world.level.storage;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.timers.TimerQueue;
import org.jspecify.annotations.Nullable;

public interface ServerLevelData extends WritableLevelData {
   String getLevelName();

   void setThundering(boolean var1);

   int getRainTime();

   void setRainTime(int var1);

   void setThunderTime(int var1);

   int getThunderTime();

   @Override
   default void fillCrashReportCategory(CrashReportCategory $$0, net.minecraft.world.level.LevelHeightAccessor $$1) {
      WritableLevelData.super.fillCrashReportCategory($$0, $$1);
      $$0.setDetail("Level name", this::getLevelName);
      $$0.setDetail(
         "Level game mode",
         () -> String.format(
            Locale.ROOT,
            "Game mode: %s (ID %d). Hardcore: %b. Commands: %b",
            this.getGameType().getName(),
            this.getGameType().getId(),
            this.isHardcore(),
            this.isAllowCommands()
         )
      );
      $$0.setDetail(
         "Level weather",
         () -> String.format(
            Locale.ROOT,
            "Rain time: %d (now: %b), thunder time: %d (now: %b)",
            this.getRainTime(),
            this.isRaining(),
            this.getThunderTime(),
            this.isThundering()
         )
      );
   }

   int getClearWeatherTime();

   void setClearWeatherTime(int var1);

   int getWanderingTraderSpawnDelay();

   void setWanderingTraderSpawnDelay(int var1);

   int getWanderingTraderSpawnChance();

   void setWanderingTraderSpawnChance(int var1);

   @Nullable
   UUID getWanderingTraderId();

   void setWanderingTraderId(UUID var1);

   net.minecraft.world.level.GameType getGameType();

   @Deprecated
   Optional<WorldBorder.Settings> getLegacyWorldBorderSettings();

   @Deprecated
   void setLegacyWorldBorderSettings(Optional<WorldBorder.Settings> var1);

   boolean isInitialized();

   void setInitialized(boolean var1);

   boolean isAllowCommands();

   void setGameType(net.minecraft.world.level.GameType var1);

   TimerQueue<MinecraftServer> getScheduledEvents();

   void setGameTime(long var1);

   void setDayTime(long var1);

   GameRules getGameRules();
}
