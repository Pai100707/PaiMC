package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation extends PathNavigation {
   private boolean avoidSun;
   private boolean canPathToTargetsBelowSurface;

   public GroundPathNavigation(net.minecraft.world.entity.Mob $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected PathFinder createPathFinder(int $$0) {
      this.nodeEvaluator = new WalkNodeEvaluator();
      return new PathFinder(this.nodeEvaluator, $$0);
   }

   @Override
   protected boolean canUpdatePath() {
      return this.mob.onGround() || this.mob.isInLiquid() || this.mob.isPassenger();
   }

   @Override
   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), this.getSurfaceY(), this.mob.getZ());
   }

   @Override
   public Path createPath(BlockPos $$0, int $$1) {
      LevelChunk $$2 = this.level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord($$0.getX()), SectionPos.blockToSectionCoord($$0.getZ()));
      if ($$2 == null) {
         return null;
      } else {
         if (!this.canPathToTargetsBelowSurface) {
            $$0 = this.findSurfacePosition($$2, $$0, $$1);
         }

         return super.createPath($$0, $$1);
      }
   }

   final BlockPos findSurfacePosition(LevelChunk $$0, BlockPos $$1, int $$2) {
      if ($$0.getBlockState($$1).isAir()) {
         MutableBlockPos $$3 = $$1.mutable().move(Direction.DOWN);

         while ($$3.getY() >= this.level.getMinY() && $$0.getBlockState($$3).isAir()) {
            $$3.move(Direction.DOWN);
         }

         if ($$3.getY() >= this.level.getMinY()) {
            return $$3.above();
         }

         $$3.setY($$1.getY() + 1);

         while ($$3.getY() <= this.level.getMaxY() && $$0.getBlockState($$3).isAir()) {
            $$3.move(Direction.UP);
         }

         $$1 = $$3;
      }

      if (!$$0.getBlockState($$1).isSolid()) {
         return $$1;
      } else {
         MutableBlockPos $$4 = $$1.mutable().move(Direction.UP);

         while ($$4.getY() <= this.level.getMaxY() && $$0.getBlockState($$4).isSolid()) {
            $$4.move(Direction.UP);
         }

         return $$4.immutable();
      }
   }

   @Override
   public Path createPath(net.minecraft.world.entity.Entity $$0, int $$1) {
      return this.createPath($$0.blockPosition(), $$1);
   }

   private int getSurfaceY() {
      if (this.mob.isInWater() && this.canFloat()) {
         int $$0 = this.mob.getBlockY();
         BlockState $$1 = this.level.getBlockState(BlockPos.containing(this.mob.getX(), $$0, this.mob.getZ()));
         int $$2 = 0;

         while ($$1.is(Blocks.WATER)) {
            $$1 = this.level.getBlockState(BlockPos.containing(this.mob.getX(), ++$$0, this.mob.getZ()));
            if (++$$2 > 16) {
               return this.mob.getBlockY();
            }
         }

         return $$0;
      } else {
         return Mth.floor(this.mob.getY() + 0.5);
      }
   }

   @Override
   protected void trimPath() {
      super.trimPath();
      if (this.avoidSun) {
         if (this.level.canSeeSky(BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
            return;
         }

         for (int $$0 = 0; $$0 < this.path.getNodeCount(); $$0++) {
            Node $$1 = this.path.getNode($$0);
            if (this.level.canSeeSky(new BlockPos($$1.x, $$1.y, $$1.z))) {
               this.path.truncateNodes($$0);
               return;
            }
         }
      }
   }

   @Override
   public boolean canNavigateGround() {
      return true;
   }

   protected boolean hasValidPathType(PathType $$0) {
      if ($$0 == PathType.WATER) {
         return false;
      } else {
         return $$0 == PathType.LAVA ? false : $$0 != PathType.OPEN;
      }
   }

   public void setAvoidSun(boolean $$0) {
      this.avoidSun = $$0;
   }

   public void setCanWalkOverFences(boolean $$0) {
      this.nodeEvaluator.setCanWalkOverFences($$0);
   }

   public void setCanPathToTargetsBelowSurface(boolean $$0) {
      this.canPathToTargetsBelowSurface = $$0;
   }
}
