package net.minecraft.world.entity.npc.wanderingtrader;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ServerLevelData;

public class WanderingTraderSpawner implements CustomSpawner {
   private static final int DEFAULT_TICK_DELAY = 1200;
   public static final int DEFAULT_SPAWN_DELAY = 24000;
   private static final int MIN_SPAWN_CHANCE = 25;
   private static final int MAX_SPAWN_CHANCE = 75;
   private static final int SPAWN_CHANCE_INCREASE = 25;
   private static final int SPAWN_ONE_IN_X_CHANCE = 10;
   private static final int NUMBER_OF_SPAWN_ATTEMPTS = 10;
   private final RandomSource random = RandomSource.create();
   private final ServerLevelData serverLevelData;
   private int tickDelay;
   private int spawnDelay;
   private int spawnChance;

   public WanderingTraderSpawner(ServerLevelData $$0) {
      this.serverLevelData = $$0;
      this.tickDelay = 1200;
      this.spawnDelay = $$0.getWanderingTraderSpawnDelay();
      this.spawnChance = $$0.getWanderingTraderSpawnChance();
      if (this.spawnDelay == 0 && this.spawnChance == 0) {
         this.spawnDelay = 24000;
         $$0.setWanderingTraderSpawnDelay(this.spawnDelay);
         this.spawnChance = 25;
         $$0.setWanderingTraderSpawnChance(this.spawnChance);
      }
   }

   public void tick(ServerLevel $$0, boolean $$1) {
      if ((Boolean)$$0.getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS)) {
         if (--this.tickDelay <= 0) {
            this.tickDelay = 1200;
            this.spawnDelay -= 1200;
            this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
            if (this.spawnDelay <= 0) {
               this.spawnDelay = 24000;
               int $$2 = this.spawnChance;
               this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
               this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
               if (this.random.nextInt(100) <= $$2) {
                  if (this.spawn($$0)) {
                     this.spawnChance = 25;
                  }
               }
            }
         }
      }
   }

   private boolean spawn(ServerLevel $$0) {
      Player $$1 = $$0.getRandomPlayer();
      if ($$1 == null) {
         return true;
      } else if (this.random.nextInt(10) != 0) {
         return false;
      } else {
         BlockPos $$2 = $$1.blockPosition();
         int $$3 = 48;
         PoiManager $$4 = $$0.getPoiManager();
         Optional<BlockPos> $$5 = $$4.find($$0x -> $$0x.is(PoiTypes.MEETING), $$0x -> true, $$2, 48, PoiManager.Occupancy.ANY);
         BlockPos $$6 = $$5.orElse($$2);
         BlockPos $$7 = this.findSpawnPositionNear($$0, $$6, 48);
         if ($$7 != null && this.hasEnoughSpace($$0, $$7)) {
            if ($$0.getBiome($$7).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
               return false;
            }

            WanderingTrader $$8 = net.minecraft.world.entity.EntityType.WANDERING_TRADER.spawn($$0, $$7, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if ($$8 != null) {
               for (int $$9 = 0; $$9 < 2; $$9++) {
                  this.tryToSpawnLlamaFor($$0, $$8, 4);
               }

               this.serverLevelData.setWanderingTraderId($$8.getUUID());
               $$8.setDespawnDelay(48000);
               $$8.setWanderTarget($$6);
               $$8.setHomeTo($$6, 16);
               return true;
            }
         }

         return false;
      }
   }

   private void tryToSpawnLlamaFor(ServerLevel $$0, WanderingTrader $$1, int $$2) {
      BlockPos $$3 = this.findSpawnPositionNear($$0, $$1.blockPosition(), $$2);
      if ($$3 != null) {
         TraderLlama $$4 = net.minecraft.world.entity.EntityType.TRADER_LLAMA.spawn($$0, $$3, net.minecraft.world.entity.EntitySpawnReason.EVENT);
         if ($$4 != null) {
            $$4.setLeashedTo($$1, true);
         }
      }
   }

   
   private BlockPos findSpawnPositionNear(LevelReader $$0, BlockPos $$1, int $$2) {
      BlockPos $$3 = null;
      net.minecraft.world.entity.SpawnPlacementType $$4 = net.minecraft.world.entity.SpawnPlacements.getPlacementType(
         net.minecraft.world.entity.EntityType.WANDERING_TRADER
      );

      for (int $$5 = 0; $$5 < 10; $$5++) {
         int $$6 = $$1.getX() + this.random.nextInt($$2 * 2) - $$2;
         int $$7 = $$1.getZ() + this.random.nextInt($$2 * 2) - $$2;
         int $$8 = $$0.getHeight(Types.WORLD_SURFACE, $$6, $$7);
         BlockPos $$9 = new BlockPos($$6, $$8, $$7);
         if ($$4.isSpawnPositionOk($$0, $$9, net.minecraft.world.entity.EntityType.WANDERING_TRADER)) {
            $$3 = $$9;
            break;
         }
      }

      return $$3;
   }

   private boolean hasEnoughSpace(BlockGetter $$0, BlockPos $$1) {
      for (BlockPos $$2 : BlockPos.betweenClosed($$1, $$1.offset(1, 2, 1))) {
         if (!$$0.getBlockState($$2).getCollisionShape($$0, $$2).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
