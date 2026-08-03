package net.minecraft.world.entity.npc;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.phys.AABB;

public class CatSpawner implements CustomSpawner {
   private static final int TICK_DELAY = 1200;
   private int nextTick;

   public void tick(ServerLevel $$0, boolean $$1) {
      this.nextTick--;
      if (this.nextTick <= 0) {
         this.nextTick = 1200;
         Player $$2 = $$0.getRandomPlayer();
         if ($$2 != null) {
            RandomSource $$3 = $$0.random;
            int $$4 = (8 + $$3.nextInt(24)) * ($$3.nextBoolean() ? -1 : 1);
            int $$5 = (8 + $$3.nextInt(24)) * ($$3.nextBoolean() ? -1 : 1);
            BlockPos $$6 = $$2.blockPosition().offset($$4, 0, $$5);
            int $$7 = 10;
            if ($$0.hasChunksAt($$6.getX() - 10, $$6.getZ() - 10, $$6.getX() + 10, $$6.getZ() + 10)) {
               if (net.minecraft.world.entity.SpawnPlacements.isSpawnPositionOk(net.minecraft.world.entity.EntityType.CAT, $$0, $$6)) {
                  if ($$0.isCloseToVillage($$6, 2)) {
                     this.spawnInVillage($$0, $$6);
                  } else if ($$0.structureManager().getStructureWithPieceAt($$6, StructureTags.CATS_SPAWN_IN).isValid()) {
                     this.spawnInHut($$0, $$6);
                  }
               }
            }
         }
      }
   }

   private void spawnInVillage(ServerLevel $$0, BlockPos $$1) {
      int $$2 = 48;
      if ($$0.getPoiManager().getCountInRange($$0x -> $$0x.is(PoiTypes.HOME), $$1, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4L) {
         List<Cat> $$3 = $$0.getEntitiesOfClass(Cat.class, new AABB($$1).inflate(48.0, 8.0, 48.0));
         if ($$3.size() < 5) {
            this.spawnCat($$1, $$0, false);
         }
      }
   }

   private void spawnInHut(ServerLevel $$0, BlockPos $$1) {
      int $$2 = 16;
      List<Cat> $$3 = $$0.getEntitiesOfClass(Cat.class, new AABB($$1).inflate(16.0, 8.0, 16.0));
      if ($$3.isEmpty()) {
         this.spawnCat($$1, $$0, true);
      }
   }

   private void spawnCat(BlockPos $$0, ServerLevel $$1, boolean $$2) {
      Cat $$3 = net.minecraft.world.entity.EntityType.CAT.create($$1, net.minecraft.world.entity.EntitySpawnReason.NATURAL);
      if ($$3 != null) {
         $$3.finalizeSpawn($$1, $$1.getCurrentDifficultyAt($$0), net.minecraft.world.entity.EntitySpawnReason.NATURAL, null);
         if ($$2) {
            $$3.setPersistenceRequired();
         }

         $$3.snapTo($$0, 0.0F, 0.0F);
         $$1.addFreshEntityWithPassengers($$3);
      }
   }
}
