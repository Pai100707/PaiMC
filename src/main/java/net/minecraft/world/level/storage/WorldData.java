package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Set;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldOptions;

public interface WorldData {
   int ANVIL_VERSION_ID = 19133;
   int MCREGION_VERSION_ID = 19132;

   net.minecraft.world.level.WorldDataConfiguration getDataConfiguration();

   void setDataConfiguration(net.minecraft.world.level.WorldDataConfiguration var1);

   boolean wasModded();

   Set<String> getKnownServerBrands();

   Set<String> getRemovedFeatureFlags();

   void setModdedInfo(String var1, boolean var2);

   default void fillCrashReportCategory(CrashReportCategory $$0) {
      $$0.setDetail("Known server brands", () -> String.join(", ", this.getKnownServerBrands()));
      $$0.setDetail("Removed feature flags", () -> String.join(", ", this.getRemovedFeatureFlags()));
      $$0.setDetail("Level was modded", () -> Boolean.toString(this.wasModded()));
      $$0.setDetail("Level storage version", () -> {
         int $$0x = this.getVersion();
         return String.format(Locale.ROOT, "0x%05X - %s", $$0x, this.getStorageVersionName($$0x));
      });
   }

   default String getStorageVersionName(int $$0) {
      switch ($$0) {
         case 19132:
            return "McRegion";
         case 19133:
            return "Anvil";
         default:
            return "Unknown?";
      }
   }

   
   CompoundTag getCustomBossEvents();

   void setCustomBossEvents(CompoundTag var1);

   ServerLevelData overworldData();

   net.minecraft.world.level.LevelSettings getLevelSettings();

   CompoundTag createTag(RegistryAccess var1, CompoundTag var2);

   boolean isHardcore();

   int getVersion();

   String getLevelName();

   net.minecraft.world.level.GameType getGameType();

   void setGameType(net.minecraft.world.level.GameType var1);

   boolean isAllowCommands();

   Difficulty getDifficulty();

   void setDifficulty(Difficulty var1);

   boolean isDifficultyLocked();

   void setDifficultyLocked(boolean var1);

   GameRules getGameRules();

   
   CompoundTag getLoadedPlayerTag();

   EndDragonFight.Data endDragonFightData();

   void setEndDragonFightData(EndDragonFight.Data var1);

   WorldOptions worldGenOptions();

   boolean isFlatWorld();

   boolean isDebugWorld();

   Lifecycle worldGenSettingsLifecycle();

   default FeatureFlagSet enabledFeatures() {
      return this.getDataConfiguration().enabledFeatures();
   }
}
