package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;

public final class LevelSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String levelName;
   private final net.minecraft.world.level.GameType gameType;
   private final boolean hardcore;
   private final Difficulty difficulty;
   private final boolean allowCommands;
   private final GameRules gameRules;
   private final net.minecraft.world.level.WorldDataConfiguration dataConfiguration;

   public LevelSettings(
      String $$0,
      net.minecraft.world.level.GameType $$1,
      boolean $$2,
      Difficulty $$3,
      boolean $$4,
      GameRules $$5,
      net.minecraft.world.level.WorldDataConfiguration $$6
   ) {
      this.levelName = $$0;
      this.gameType = $$1;
      this.hardcore = $$2;
      this.difficulty = $$3;
      this.allowCommands = $$4;
      this.gameRules = $$5;
      this.dataConfiguration = $$6;
   }

   public static net.minecraft.world.level.LevelSettings parse(Dynamic<?> $$0, net.minecraft.world.level.WorldDataConfiguration $$1) {
      net.minecraft.world.level.GameType $$2 = net.minecraft.world.level.GameType.byId($$0.get("GameType").asInt(0));
      return new net.minecraft.world.level.LevelSettings(
         $$0.get("LevelName").asString(""),
         $$2,
         $$0.get("hardcore").asBoolean(false),
         $$0.get("Difficulty").asNumber().map($$0x -> Difficulty.byId($$0x.byteValue())).result().orElse(Difficulty.NORMAL),
         $$0.get("allowCommands").asBoolean($$2 == net.minecraft.world.level.GameType.CREATIVE),
         (GameRules)GameRules.codec($$1.enabledFeatures()).parse($$0.get("game_rules").orElseEmptyMap()).resultOrPartial(LOGGER::warn).orElseThrow(),
         $$1
      );
   }

   public String levelName() {
      return this.levelName;
   }

   public net.minecraft.world.level.GameType gameType() {
      return this.gameType;
   }

   public boolean hardcore() {
      return this.hardcore;
   }

   public Difficulty difficulty() {
      return this.difficulty;
   }

   public boolean allowCommands() {
      return this.allowCommands;
   }

   public GameRules gameRules() {
      return this.gameRules;
   }

   public net.minecraft.world.level.WorldDataConfiguration getDataConfiguration() {
      return this.dataConfiguration;
   }

   public net.minecraft.world.level.LevelSettings withGameType(net.minecraft.world.level.GameType $$0) {
      return new net.minecraft.world.level.LevelSettings(
         this.levelName, $$0, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataConfiguration
      );
   }

   public net.minecraft.world.level.LevelSettings withDifficulty(Difficulty $$0) {
      return new net.minecraft.world.level.LevelSettings(
         this.levelName, this.gameType, this.hardcore, $$0, this.allowCommands, this.gameRules, this.dataConfiguration
      );
   }

   public net.minecraft.world.level.LevelSettings withDataConfiguration(net.minecraft.world.level.WorldDataConfiguration $$0) {
      return new net.minecraft.world.level.LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, $$0);
   }

   public net.minecraft.world.level.LevelSettings copy() {
      return new net.minecraft.world.level.LevelSettings(
         this.levelName,
         this.gameType,
         this.hardcore,
         this.difficulty,
         this.allowCommands,
         this.gameRules.copy(this.dataConfiguration.enabledFeatures()),
         this.dataConfiguration
      );
   }
}
