package net.minecraft.world.entity.ai.village;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class VillageSiege implements CustomSpawner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private boolean hasSetupSiege;
   private VillageSiege.State siegeState = VillageSiege.State.SIEGE_DONE;
   private int zombiesToSpawn;
   private int nextSpawnTime;
   private int spawnX;
   private int spawnY;
   private int spawnZ;

   public void tick(ServerLevel $$0, boolean $$1) {
      if (!$$0.isBrightOutside() && $$1) {
         long $$2 = $$0.getDayTime() % 24000L;
         if ($$2 == 18000L) {
            this.siegeState = $$0.random.nextInt(10) == 0 ? VillageSiege.State.SIEGE_TONIGHT : VillageSiege.State.SIEGE_DONE;
         }

         if (this.siegeState != VillageSiege.State.SIEGE_DONE) {
            if (!this.hasSetupSiege) {
               if (!this.tryToSetupSiege($$0)) {
                  return;
               }

               this.hasSetupSiege = true;
            }

            if (this.nextSpawnTime > 0) {
               this.nextSpawnTime--;
            } else {
               this.nextSpawnTime = 2;
               if (this.zombiesToSpawn > 0) {
                  this.trySpawn($$0);
                  this.zombiesToSpawn--;
               } else {
                  this.siegeState = VillageSiege.State.SIEGE_DONE;
               }
            }
         }
      } else {
         this.siegeState = VillageSiege.State.SIEGE_DONE;
         this.hasSetupSiege = false;
      }
   }

   private boolean tryToSetupSiege(ServerLevel $$0) {
      for (Player $$1 : $$0.players()) {
         if (!$$1.isSpectator()) {
            BlockPos $$2 = $$1.blockPosition();
            if ($$0.isVillage($$2) && !$$0.getBiome($$2).is(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) {
               for (int $$3 = 0; $$3 < 10; $$3++) {
                  float $$4 = $$0.random.nextFloat() * (float) (Math.PI * 2);
                  this.spawnX = $$2.getX() + Mth.floor(Mth.cos($$4) * 32.0F);
                  this.spawnY = $$2.getY();
                  this.spawnZ = $$2.getZ() + Mth.floor(Mth.sin($$4) * 32.0F);
                  if (this.findRandomSpawnPos($$0, new BlockPos(this.spawnX, this.spawnY, this.spawnZ)) != null) {
                     this.nextSpawnTime = 0;
                     this.zombiesToSpawn = 20;
                     break;
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   private void trySpawn(ServerLevel $$0) {
      Vec3 $$1 = this.findRandomSpawnPos($$0, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
      if ($$1 != null) {
         Zombie $$2;
         try {
            $$2 = new Zombie($$0);
            $$2.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$2.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.EVENT, null);
         } catch (Exception var5) {
            LOGGER.warn("Failed to create zombie for village siege at {}", $$1, var5);
            return;
         }

         $$2.snapTo($$1.x, $$1.y, $$1.z, $$0.random.nextFloat() * 360.0F, 0.0F);
         $$0.addFreshEntityWithPassengers($$2);
      }
   }

   
   private Vec3 findRandomSpawnPos(ServerLevel $$0, BlockPos $$1) {
      for (int $$2 = 0; $$2 < 10; $$2++) {
         int $$3 = $$1.getX() + $$0.random.nextInt(16) - 8;
         int $$4 = $$1.getZ() + $$0.random.nextInt(16) - 8;
         int $$5 = $$0.getHeight(Types.WORLD_SURFACE, $$3, $$4);
         BlockPos $$6 = new BlockPos($$3, $$5, $$4);
         if ($$0.isVillage($$6)
            && Monster.checkMonsterSpawnRules(
               net.minecraft.world.entity.EntityType.ZOMBIE, $$0, net.minecraft.world.entity.EntitySpawnReason.EVENT, $$6, $$0.random
            )) {
            return Vec3.atBottomCenterOf($$6);
         }
      }

      return null;
   }

   static enum State {
      SIEGE_CAN_ACTIVATE,
      SIEGE_TONIGHT,
      SIEGE_DONE;
   }
}
