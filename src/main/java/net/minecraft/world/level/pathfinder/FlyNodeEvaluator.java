package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class FlyNodeEvaluator extends WalkNodeEvaluator {
   private final Long2ObjectMap<PathType> pathTypeByPosCache = new Long2ObjectOpenHashMap();
   private static final float SMALL_MOB_SIZE = 1.0F;
   private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.1F;
   private static final int MAX_START_NODE_CANDIDATES = 10;

   @Override
   public void prepare(net.minecraft.world.level.PathNavigationRegion $$0, Mob $$1) {
      super.prepare($$0, $$1);
      this.pathTypeByPosCache.clear();
      $$1.onPathfindingStart();
   }

   @Override
   public void done() {
      this.mob.onPathfindingDone();
      this.pathTypeByPosCache.clear();
      super.done();
   }

   @Override
   public Node getStart() {
      int $$0;
      if (this.canFloat() && this.mob.isInWater()) {
         $$0 = this.mob.getBlockY();
         MutableBlockPos $$1 = new MutableBlockPos(this.mob.getX(), $$0, this.mob.getZ());

         for (BlockState $$2 = this.currentContext.getBlockState($$1); $$2.is(Blocks.WATER); $$2 = this.currentContext.getBlockState($$1)) {
            $$1.set(this.mob.getX(), ++$$0, this.mob.getZ());
         }
      } else {
         $$0 = Mth.floor(this.mob.getY() + 0.5);
      }

      BlockPos $$4 = BlockPos.containing(this.mob.getX(), $$0, this.mob.getZ());
      if (!this.canStartAt($$4)) {
         for (BlockPos $$5 : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
            if (this.canStartAt($$5)) {
               return super.getStartNode($$5);
            }
         }
      }

      return super.getStartNode($$4);
   }

   @Override
   protected boolean canStartAt(BlockPos $$0) {
      PathType $$1 = this.getCachedPathType($$0.getX(), $$0.getY(), $$0.getZ());
      return this.mob.getPathfindingMalus($$1) >= 0.0F;
   }

   @Override
   public Target getTarget(double $$0, double $$1, double $$2) {
      return this.getTargetNodeAt($$0, $$1, $$2);
   }

   @Override
   public int getNeighbors(Node[] $$0, Node $$1) {
      int $$2 = 0;
      Node $$3 = this.findAcceptedNode($$1.x, $$1.y, $$1.z + 1);
      if (this.isOpen($$3)) {
         $$0[$$2++] = $$3;
      }

      Node $$4 = this.findAcceptedNode($$1.x - 1, $$1.y, $$1.z);
      if (this.isOpen($$4)) {
         $$0[$$2++] = $$4;
      }

      Node $$5 = this.findAcceptedNode($$1.x + 1, $$1.y, $$1.z);
      if (this.isOpen($$5)) {
         $$0[$$2++] = $$5;
      }

      Node $$6 = this.findAcceptedNode($$1.x, $$1.y, $$1.z - 1);
      if (this.isOpen($$6)) {
         $$0[$$2++] = $$6;
      }

      Node $$7 = this.findAcceptedNode($$1.x, $$1.y + 1, $$1.z);
      if (this.isOpen($$7)) {
         $$0[$$2++] = $$7;
      }

      Node $$8 = this.findAcceptedNode($$1.x, $$1.y - 1, $$1.z);
      if (this.isOpen($$8)) {
         $$0[$$2++] = $$8;
      }

      Node $$9 = this.findAcceptedNode($$1.x, $$1.y + 1, $$1.z + 1);
      if (this.isOpen($$9) && this.hasMalus($$3) && this.hasMalus($$7)) {
         $$0[$$2++] = $$9;
      }

      Node $$10 = this.findAcceptedNode($$1.x - 1, $$1.y + 1, $$1.z);
      if (this.isOpen($$10) && this.hasMalus($$4) && this.hasMalus($$7)) {
         $$0[$$2++] = $$10;
      }

      Node $$11 = this.findAcceptedNode($$1.x + 1, $$1.y + 1, $$1.z);
      if (this.isOpen($$11) && this.hasMalus($$5) && this.hasMalus($$7)) {
         $$0[$$2++] = $$11;
      }

      Node $$12 = this.findAcceptedNode($$1.x, $$1.y + 1, $$1.z - 1);
      if (this.isOpen($$12) && this.hasMalus($$6) && this.hasMalus($$7)) {
         $$0[$$2++] = $$12;
      }

      Node $$13 = this.findAcceptedNode($$1.x, $$1.y - 1, $$1.z + 1);
      if (this.isOpen($$13) && this.hasMalus($$3) && this.hasMalus($$8)) {
         $$0[$$2++] = $$13;
      }

      Node $$14 = this.findAcceptedNode($$1.x - 1, $$1.y - 1, $$1.z);
      if (this.isOpen($$14) && this.hasMalus($$4) && this.hasMalus($$8)) {
         $$0[$$2++] = $$14;
      }

      Node $$15 = this.findAcceptedNode($$1.x + 1, $$1.y - 1, $$1.z);
      if (this.isOpen($$15) && this.hasMalus($$5) && this.hasMalus($$8)) {
         $$0[$$2++] = $$15;
      }

      Node $$16 = this.findAcceptedNode($$1.x, $$1.y - 1, $$1.z - 1);
      if (this.isOpen($$16) && this.hasMalus($$6) && this.hasMalus($$8)) {
         $$0[$$2++] = $$16;
      }

      Node $$17 = this.findAcceptedNode($$1.x + 1, $$1.y, $$1.z - 1);
      if (this.isOpen($$17) && this.hasMalus($$6) && this.hasMalus($$5)) {
         $$0[$$2++] = $$17;
      }

      Node $$18 = this.findAcceptedNode($$1.x + 1, $$1.y, $$1.z + 1);
      if (this.isOpen($$18) && this.hasMalus($$3) && this.hasMalus($$5)) {
         $$0[$$2++] = $$18;
      }

      Node $$19 = this.findAcceptedNode($$1.x - 1, $$1.y, $$1.z - 1);
      if (this.isOpen($$19) && this.hasMalus($$6) && this.hasMalus($$4)) {
         $$0[$$2++] = $$19;
      }

      Node $$20 = this.findAcceptedNode($$1.x - 1, $$1.y, $$1.z + 1);
      if (this.isOpen($$20) && this.hasMalus($$3) && this.hasMalus($$4)) {
         $$0[$$2++] = $$20;
      }

      Node $$21 = this.findAcceptedNode($$1.x + 1, $$1.y + 1, $$1.z - 1);
      if (this.isOpen($$21)
         && this.hasMalus($$17)
         && this.hasMalus($$6)
         && this.hasMalus($$5)
         && this.hasMalus($$7)
         && this.hasMalus($$12)
         && this.hasMalus($$11)) {
         $$0[$$2++] = $$21;
      }

      Node $$22 = this.findAcceptedNode($$1.x + 1, $$1.y + 1, $$1.z + 1);
      if (this.isOpen($$22)
         && this.hasMalus($$18)
         && this.hasMalus($$3)
         && this.hasMalus($$5)
         && this.hasMalus($$7)
         && this.hasMalus($$9)
         && this.hasMalus($$11)) {
         $$0[$$2++] = $$22;
      }

      Node $$23 = this.findAcceptedNode($$1.x - 1, $$1.y + 1, $$1.z - 1);
      if (this.isOpen($$23)
         && this.hasMalus($$19)
         && this.hasMalus($$6)
         && this.hasMalus($$4)
         && this.hasMalus($$7)
         && this.hasMalus($$12)
         && this.hasMalus($$10)) {
         $$0[$$2++] = $$23;
      }

      Node $$24 = this.findAcceptedNode($$1.x - 1, $$1.y + 1, $$1.z + 1);
      if (this.isOpen($$24)
         && this.hasMalus($$20)
         && this.hasMalus($$3)
         && this.hasMalus($$4)
         && this.hasMalus($$7)
         && this.hasMalus($$9)
         && this.hasMalus($$10)) {
         $$0[$$2++] = $$24;
      }

      Node $$25 = this.findAcceptedNode($$1.x + 1, $$1.y - 1, $$1.z - 1);
      if (this.isOpen($$25)
         && this.hasMalus($$17)
         && this.hasMalus($$6)
         && this.hasMalus($$5)
         && this.hasMalus($$8)
         && this.hasMalus($$16)
         && this.hasMalus($$15)) {
         $$0[$$2++] = $$25;
      }

      Node $$26 = this.findAcceptedNode($$1.x + 1, $$1.y - 1, $$1.z + 1);
      if (this.isOpen($$26)
         && this.hasMalus($$18)
         && this.hasMalus($$3)
         && this.hasMalus($$5)
         && this.hasMalus($$8)
         && this.hasMalus($$13)
         && this.hasMalus($$15)) {
         $$0[$$2++] = $$26;
      }

      Node $$27 = this.findAcceptedNode($$1.x - 1, $$1.y - 1, $$1.z - 1);
      if (this.isOpen($$27)
         && this.hasMalus($$19)
         && this.hasMalus($$6)
         && this.hasMalus($$4)
         && this.hasMalus($$8)
         && this.hasMalus($$16)
         && this.hasMalus($$14)) {
         $$0[$$2++] = $$27;
      }

      Node $$28 = this.findAcceptedNode($$1.x - 1, $$1.y - 1, $$1.z + 1);
      if (this.isOpen($$28)
         && this.hasMalus($$20)
         && this.hasMalus($$3)
         && this.hasMalus($$4)
         && this.hasMalus($$8)
         && this.hasMalus($$13)
         && this.hasMalus($$14)) {
         $$0[$$2++] = $$28;
      }

      return $$2;
   }

   private boolean hasMalus(@Nullable Node $$0) {
      return $$0 != null && $$0.costMalus >= 0.0F;
   }

   private boolean isOpen(@Nullable Node $$0) {
      return $$0 != null && !$$0.closed;
   }

   @Nullable
   protected Node findAcceptedNode(int $$0, int $$1, int $$2) {
      Node $$3 = null;
      PathType $$4 = this.getCachedPathType($$0, $$1, $$2);
      float $$5 = this.mob.getPathfindingMalus($$4);
      if ($$5 >= 0.0F) {
         $$3 = this.getNode($$0, $$1, $$2);
         $$3.type = $$4;
         $$3.costMalus = Math.max($$3.costMalus, $$5);
         if ($$4 == PathType.WALKABLE) {
            $$3.costMalus++;
         }
      }

      return $$3;
   }

   @Override
   protected PathType getCachedPathType(int $$0, int $$1, int $$2) {
      return (PathType)this.pathTypeByPosCache
         .computeIfAbsent(BlockPos.asLong($$0, $$1, $$2), $$3 -> this.getPathTypeOfMob(this.currentContext, $$0, $$1, $$2, this.mob));
   }

   @Override
   public PathType getPathType(PathfindingContext $$0, int $$1, int $$2, int $$3) {
      PathType $$4 = $$0.getPathTypeFromState($$1, $$2, $$3);
      if ($$4 == PathType.OPEN && $$2 >= $$0.level().getMinY() + 1) {
         BlockPos $$5 = new BlockPos($$1, $$2 - 1, $$3);
         PathType $$6 = $$0.getPathTypeFromState($$5.getX(), $$5.getY(), $$5.getZ());
         if ($$6 == PathType.DAMAGE_FIRE || $$6 == PathType.LAVA) {
            $$4 = PathType.DAMAGE_FIRE;
         } else if ($$6 == PathType.DAMAGE_OTHER) {
            $$4 = PathType.DAMAGE_OTHER;
         } else if ($$6 == PathType.COCOA) {
            $$4 = PathType.COCOA;
         } else if ($$6 == PathType.FENCE) {
            if (!$$5.equals($$0.mobPosition())) {
               $$4 = PathType.FENCE;
            }
         } else {
            $$4 = $$6 != PathType.WALKABLE && $$6 != PathType.OPEN && $$6 != PathType.WATER ? PathType.WALKABLE : PathType.OPEN;
         }
      }

      if ($$4 == PathType.WALKABLE || $$4 == PathType.OPEN) {
         $$4 = checkNeighbourBlocks($$0, $$1, $$2, $$3, $$4);
      }

      return $$4;
   }

   private Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions(Mob $$0) {
      AABB $$1 = $$0.getBoundingBox();
      boolean $$2 = $$1.getSize() < 1.0;
      if (!$$2) {
         return List.of(
            BlockPos.containing($$1.minX, $$0.getBlockY(), $$1.minZ),
            BlockPos.containing($$1.minX, $$0.getBlockY(), $$1.maxZ),
            BlockPos.containing($$1.maxX, $$0.getBlockY(), $$1.minZ),
            BlockPos.containing($$1.maxX, $$0.getBlockY(), $$1.maxZ)
         );
      } else {
         double $$3 = Math.max(0.0, 1.1F - $$1.getZsize());
         double $$4 = Math.max(0.0, 1.1F - $$1.getXsize());
         double $$5 = Math.max(0.0, 1.1F - $$1.getYsize());
         AABB $$6 = $$1.inflate($$4, $$5, $$3);
         return BlockPos.randomBetweenClosed(
            $$0.getRandom(), 10, Mth.floor($$6.minX), Mth.floor($$6.minY), Mth.floor($$6.minZ), Mth.floor($$6.maxX), Mth.floor($$6.maxY), Mth.floor($$6.maxZ)
         );
      }
   }
}
