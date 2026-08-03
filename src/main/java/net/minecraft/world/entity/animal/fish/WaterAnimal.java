package net.minecraft.world.entity.animal.fish;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;

public abstract class WaterAnimal extends net.minecraft.world.entity.PathfinderMob {
   public static final int AMBIENT_SOUND_INTERVAL = 120;

   protected WaterAnimal(net.minecraft.world.entity.EntityType<? extends WaterAnimal> $$0, Level $$1) {
      super($$0, $$1);
      this.setPathfindingMalus(PathType.WATER, 0.0F);
   }

   @Override
   public boolean checkSpawnObstruction(LevelReader $$0) {
      return $$0.isUnobstructed(this);
   }

   @Override
   public int getAmbientSoundInterval() {
      return 120;
   }

   @Override
   protected int getBaseExperienceReward(ServerLevel $$0) {
      return 1 + this.random.nextInt(3);
   }

   protected void handleAirSupply(ServerLevel $$0, int $$1) {
      if (this.isAlive() && !this.isInWater()) {
         this.setAirSupply($$1 - 1);
         if (this.shouldTakeDrowningDamage()) {
            this.setAirSupply(0);
            this.hurtServer($$0, this.damageSources().drown(), 2.0F);
         }
      } else {
         this.setAirSupply(300);
      }
   }

   @Override
   public void baseTick() {
      int $$0 = this.getAirSupply();
      super.baseTick();
      if (this.level() instanceof ServerLevel $$1) {
         this.handleAirSupply($$1, $$0);
      }
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public boolean canBeLeashed() {
      return false;
   }

   public static boolean checkSurfaceWaterAnimalSpawnRules(
      net.minecraft.world.entity.EntityType<? extends WaterAnimal> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      int $$5 = $$1.getSeaLevel();
      int $$6 = $$5 - 13;
      return $$3.getY() >= $$6 && $$3.getY() <= $$5 && $$1.getFluidState($$3.below()).is(FluidTags.WATER) && $$1.getBlockState($$3.above()).is(Blocks.WATER);
   }
}
