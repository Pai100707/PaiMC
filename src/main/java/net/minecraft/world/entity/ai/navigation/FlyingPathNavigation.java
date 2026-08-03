package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class FlyingPathNavigation extends PathNavigation {
   public FlyingPathNavigation(net.minecraft.world.entity.Mob $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected PathFinder createPathFinder(int $$0) {
      this.nodeEvaluator = new FlyNodeEvaluator();
      return new PathFinder(this.nodeEvaluator, $$0);
   }

   @Override
   protected boolean canMoveDirectly(Vec3 $$0, Vec3 $$1) {
      return isClearForMovementBetween(this.mob, $$0, $$1, true);
   }

   @Override
   protected boolean canUpdatePath() {
      return this.canFloat() && this.mob.isInLiquid() || !this.mob.isPassenger();
   }

   @Override
   protected Vec3 getTempMobPos() {
      return this.mob.position();
   }

   @Override
   public Path createPath(net.minecraft.world.entity.Entity $$0, int $$1) {
      return this.createPath($$0.blockPosition(), $$1);
   }

   @Override
   public void tick() {
      this.tick++;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vec3 $$0 = this.path.getNextEntityPos(this.mob);
            if (this.mob.getBlockX() == Mth.floor($$0.x) && this.mob.getBlockY() == Mth.floor($$0.y) && this.mob.getBlockZ() == Mth.floor($$0.z)) {
               this.path.advance();
            }
         }

         if (!this.isDone()) {
            Vec3 $$1 = this.path.getNextEntityPos(this.mob);
            this.mob.getMoveControl().setWantedPosition($$1.x, $$1.y, $$1.z, this.speedModifier);
         }
      }
   }

   @Override
   public boolean isStableDestination(BlockPos $$0) {
      return this.level.getBlockState($$0).entityCanStandOn(this.level, $$0, this.mob);
   }

   @Override
   public boolean canNavigateGround() {
      return false;
   }
}
