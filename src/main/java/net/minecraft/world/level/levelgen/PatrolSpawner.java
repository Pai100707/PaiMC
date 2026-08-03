package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

public class PatrolSpawner implements net.minecraft.world.level.CustomSpawner {
   private int nextTick;

   @Override
   public void tick(ServerLevel $$0, boolean $$1) {
      if ($$1) {
         if ($$0.getGameRules().get(GameRules.SPAWN_PATROLS)) {
            RandomSource $$2 = $$0.random;
            this.nextTick--;
            if (this.nextTick <= 0) {
               this.nextTick = this.nextTick + 12000 + $$2.nextInt(1200);
               if ($$0.isBrightOutside()) {
                  if ($$2.nextInt(5) == 0) {
                     int $$3 = $$0.players().size();
                     if ($$3 >= 1) {
                        Player $$4 = (Player)$$0.players().get($$2.nextInt($$3));
                        if (!$$4.isSpectator()) {
                           if (!$$0.isCloseToVillage($$4.blockPosition(), 2)) {
                              int $$5 = (24 + $$2.nextInt(24)) * ($$2.nextBoolean() ? -1 : 1);
                              int $$6 = (24 + $$2.nextInt(24)) * ($$2.nextBoolean() ? -1 : 1);
                              MutableBlockPos $$7 = $$4.blockPosition().mutable().move($$5, 0, $$6);
                              int $$8 = 10;
                              if ($$0.hasChunksAt($$7.getX() - 10, $$7.getZ() - 10, $$7.getX() + 10, $$7.getZ() + 10)) {
                                 if ((Boolean)$$0.environmentAttributes().getValue(EnvironmentAttributes.CAN_PILLAGER_PATROL_SPAWN, $$7)) {
                                    int $$9 = (int)Math.ceil($$0.getCurrentDifficultyAt($$7).getEffectiveDifficulty()) + 1;

                                    for (int $$10 = 0; $$10 < $$9; $$10++) {
                                       $$7.setY($$0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, $$7).getY());
                                       if ($$10 == 0) {
                                          if (!this.spawnPatrolMember($$0, $$7, $$2, true)) {
                                             break;
                                          }
                                       } else {
                                          this.spawnPatrolMember($$0, $$7, $$2, false);
                                       }

                                       $$7.setX($$7.getX() + $$2.nextInt(5) - $$2.nextInt(5));
                                       $$7.setZ($$7.getZ() + $$2.nextInt(5) - $$2.nextInt(5));
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean spawnPatrolMember(ServerLevel $$0, BlockPos $$1, RandomSource $$2, boolean $$3) {
      BlockState $$4 = $$0.getBlockState($$1);
      if (!net.minecraft.world.level.NaturalSpawner.isValidEmptySpawnBlock($$0, $$1, $$4, $$4.getFluidState(), EntityType.PILLAGER)) {
         return false;
      } else if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, $$0, EntitySpawnReason.PATROL, $$1, $$2)) {
         return false;
      } else {
         PatrollingMonster $$5 = (PatrollingMonster)EntityType.PILLAGER.create($$0, EntitySpawnReason.PATROL);
         if ($$5 != null) {
            if ($$3) {
               $$5.setPatrolLeader(true);
               $$5.findPatrolTarget();
            }

            $$5.setPos($$1.getX(), $$1.getY(), $$1.getZ());
            $$5.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$1), EntitySpawnReason.PATROL, null);
            $$0.addFreshEntityWithPassengers($$5);
            return true;
         } else {
            return false;
         }
      }
   }
}
