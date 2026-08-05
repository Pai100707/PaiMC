package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SwimNodeEvaluator extends NodeEvaluator {
   private final boolean allowBreaching;
   private final Long2ObjectMap<PathType> pathTypesByPosCache = new Long2ObjectOpenHashMap();

   public SwimNodeEvaluator(boolean $$0) {
      this.allowBreaching = $$0;
   }

   @Override
   public void prepare(net.minecraft.world.level.PathNavigationRegion $$0, Mob $$1) {
      super.prepare($$0, $$1);
      this.pathTypesByPosCache.clear();
   }

   @Override
   public void done() {
      super.done();
      this.pathTypesByPosCache.clear();
   }

   @Override
   public Node getStart() {
      return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ));
   }

   @Override
   public Target getTarget(double $$0, double $$1, double $$2) {
      return this.getTargetNodeAt($$0, $$1, $$2);
   }

   @Override
   public int getNeighbors(Node[] $$0, Node $$1) {
      int $$2 = 0;
      Map<Direction, Node> $$3 = Maps.newEnumMap(Direction.class);

      for (Direction $$4 : Direction.values()) {
         Node $$5 = this.findAcceptedNode($$1.x + $$4.getStepX(), $$1.y + $$4.getStepY(), $$1.z + $$4.getStepZ());
         $$3.put($$4, $$5);
         if (this.isNodeValid($$5)) {
            $$0[$$2++] = $$5;
         }
      }

      for (Direction $$6 : Plane.HORIZONTAL) {
         Direction $$7 = $$6.getClockWise();
         if (hasMalus($$3.get($$6)) && hasMalus($$3.get($$7))) {
            Node $$8 = this.findAcceptedNode($$1.x + $$6.getStepX() + $$7.getStepX(), $$1.y, $$1.z + $$6.getStepZ() + $$7.getStepZ());
            if (this.isNodeValid($$8)) {
               $$0[$$2++] = $$8;
            }
         }
      }

      return $$2;
   }

   protected boolean isNodeValid(Node $$0) {
      return $$0 != null && !$$0.closed;
   }

   private static boolean hasMalus(Node $$0) {
      return $$0 != null && $$0.costMalus >= 0.0F;
   }

   
   protected Node findAcceptedNode(int $$0, int $$1, int $$2) {
      Node $$3 = null;
      PathType $$4 = this.getCachedBlockType($$0, $$1, $$2);
      if (this.allowBreaching && $$4 == PathType.BREACH || $$4 == PathType.WATER) {
         float $$5 = this.mob.getPathfindingMalus($$4);
         if ($$5 >= 0.0F) {
            $$3 = this.getNode($$0, $$1, $$2);
            $$3.type = $$4;
            $$3.costMalus = Math.max($$3.costMalus, $$5);
            if (this.currentContext.level().getFluidState(new BlockPos($$0, $$1, $$2)).isEmpty()) {
               $$3.costMalus += 8.0F;
            }
         }
      }

      return $$3;
   }

   protected PathType getCachedBlockType(int $$0, int $$1, int $$2) {
      return (PathType)this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong($$0, $$1, $$2), $$3 -> this.getPathType(this.currentContext, $$0, $$1, $$2));
   }

   @Override
   public PathType getPathType(PathfindingContext $$0, int $$1, int $$2, int $$3) {
      return this.getPathTypeOfMob($$0, $$1, $$2, $$3, this.mob);
   }

   @Override
   public PathType getPathTypeOfMob(PathfindingContext $$0, int $$1, int $$2, int $$3, Mob $$4) {
      MutableBlockPos $$5 = new MutableBlockPos();

      for (int $$6 = $$1; $$6 < $$1 + this.entityWidth; $$6++) {
         for (int $$7 = $$2; $$7 < $$2 + this.entityHeight; $$7++) {
            for (int $$8 = $$3; $$8 < $$3 + this.entityDepth; $$8++) {
               BlockState $$9 = $$0.getBlockState($$5.set($$6, $$7, $$8));
               FluidState $$10 = $$9.getFluidState();
               if ($$10.isEmpty() && $$9.isPathfindable(PathComputationType.WATER) && $$9.isAir()) {
                  return PathType.BREACH;
               }

               if (!$$10.is(FluidTags.WATER)) {
                  return PathType.BLOCKED;
               }
            }
         }
      }

      BlockState $$11 = $$0.getBlockState($$5);
      return $$11.isPathfindable(PathComputationType.WATER) ? PathType.WATER : PathType.BLOCKED;
   }
}
