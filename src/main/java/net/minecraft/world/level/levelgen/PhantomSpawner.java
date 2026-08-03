package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;

public class PhantomSpawner implements net.minecraft.world.level.CustomSpawner {
   private int nextTick;

   @Override
   public void tick(ServerLevel $$0, boolean $$1) {
      if ($$1) {
         if ($$0.getGameRules().get(GameRules.SPAWN_PHANTOMS)) {
            RandomSource $$2 = $$0.random;
            this.nextTick--;
            if (this.nextTick <= 0) {
               this.nextTick = this.nextTick + (60 + $$2.nextInt(60)) * 20;
               if ($$0.getSkyDarken() >= 5 || !$$0.dimensionType().hasSkyLight()) {
                  for (ServerPlayer $$3 : $$0.players()) {
                     if (!$$3.isSpectator()) {
                        BlockPos $$4 = $$3.blockPosition();
                        if (!$$0.dimensionType().hasSkyLight() || $$4.getY() >= $$0.getSeaLevel() && $$0.canSeeSky($$4)) {
                           DifficultyInstance $$5 = $$0.getCurrentDifficultyAt($$4);
                           if ($$5.isHarderThan($$2.nextFloat() * 3.0F)) {
                              ServerStatsCounter $$6 = $$3.getStats();
                              int $$7 = Mth.clamp($$6.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
                              int $$8 = 24000;
                              if ($$2.nextInt($$7) >= 72000) {
                                 BlockPos $$9 = $$4.above(20 + $$2.nextInt(15)).east(-10 + $$2.nextInt(21)).south(-10 + $$2.nextInt(21));
                                 BlockState $$10 = $$0.getBlockState($$9);
                                 FluidState $$11 = $$0.getFluidState($$9);
                                 if (net.minecraft.world.level.NaturalSpawner.isValidEmptySpawnBlock($$0, $$9, $$10, $$11, EntityType.PHANTOM)) {
                                    SpawnGroupData $$12 = null;
                                    int $$13 = 1 + $$2.nextInt($$5.getDifficulty().getId() + 1);

                                    for (int $$14 = 0; $$14 < $$13; $$14++) {
                                       Phantom $$15 = (Phantom)EntityType.PHANTOM.create($$0, EntitySpawnReason.NATURAL);
                                       if ($$15 != null) {
                                          $$15.snapTo($$9, 0.0F, 0.0F);
                                          $$12 = $$15.finalizeSpawn($$0, $$5, EntitySpawnReason.NATURAL, $$12);
                                          $$0.addFreshEntityWithPassengers($$15);
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
   }
}
