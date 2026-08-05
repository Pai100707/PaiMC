package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class WallClimberNavigation extends GroundPathNavigation {
   
   private BlockPos pathToPosition;

   public WallClimberNavigation(net.minecraft.world.entity.Mob $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public Path createPath(BlockPos $$0, int $$1) {
      this.pathToPosition = $$0;
      return super.createPath($$0, $$1);
   }

   @Override
   public Path createPath(net.minecraft.world.entity.Entity $$0, int $$1) {
      this.pathToPosition = $$0.blockPosition();
      return super.createPath($$0, $$1);
   }

   @Override
   public boolean moveTo(net.minecraft.world.entity.Entity $$0, double $$1) {
      Path $$2 = this.createPath($$0, 0);
      if ($$2 != null) {
         return this.moveTo($$2, $$1);
      } else {
         this.pathToPosition = $$0.blockPosition();
         this.speedModifier = $$1;
         return true;
      }
   }

   @Override
   public void tick() {
      if (!this.isDone()) {
         super.tick();
      } else {
         if (this.pathToPosition != null) {
            if (!this.pathToPosition.closerToCenterThan(this.mob.position(), this.mob.getBbWidth())
               && (
                  !(this.mob.getY() > this.pathToPosition.getY())
                     || !BlockPos.containing(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ())
                        .closerToCenterThan(this.mob.position(), this.mob.getBbWidth())
               )) {
               this.mob
                  .getMoveControl()
                  .setWantedPosition(this.pathToPosition.getX(), this.pathToPosition.getY(), this.pathToPosition.getZ(), this.speedModifier);
            } else {
               this.pathToPosition = null;
            }
         }
      }
   }
}
