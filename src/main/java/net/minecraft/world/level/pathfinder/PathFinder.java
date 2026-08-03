package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

public class PathFinder {
   private static final float FUDGING = 1.5F;
   private final Node[] neighbors = new Node[32];
   private int maxVisitedNodes;
   private final NodeEvaluator nodeEvaluator;
   private final BinaryHeap openSet = new BinaryHeap();
   private BooleanSupplier captureDebug = () -> false;

   public PathFinder(NodeEvaluator $$0, int $$1) {
      this.nodeEvaluator = $$0;
      this.maxVisitedNodes = $$1;
   }

   public void setCaptureDebug(BooleanSupplier $$0) {
      this.captureDebug = $$0;
   }

   public void setMaxVisitedNodes(int $$0) {
      this.maxVisitedNodes = $$0;
   }

   @Nullable
   public Path findPath(net.minecraft.world.level.PathNavigationRegion $$0, Mob $$1, Set<BlockPos> $$2, float $$3, int $$4, float $$5) {
      this.openSet.clear();
      this.nodeEvaluator.prepare($$0, $$1);
      Node $$6 = this.nodeEvaluator.getStart();
      if ($$6 == null) {
         return null;
      } else {
         Map<Target, BlockPos> $$7 = $$2.stream()
            .collect(Collectors.toMap($$0x -> this.nodeEvaluator.getTarget($$0x.getX(), $$0x.getY(), $$0x.getZ()), Function.identity()));
         Path $$8 = this.findPath($$6, $$7, $$3, $$4, $$5);
         this.nodeEvaluator.done();
         return $$8;
      }
   }

   @Nullable
   private Path findPath(Node $$0, Map<Target, BlockPos> $$1, float $$2, int $$3, float $$4) {
      ProfilerFiller $$5 = Profiler.get();
      $$5.push("find_path");
      $$5.markForCharting(MetricCategory.PATH_FINDING);
      Set<Target> $$6 = $$1.keySet();
      $$0.g = 0.0F;
      $$0.h = this.getBestH($$0, $$6);
      $$0.f = $$0.h;
      this.openSet.clear();
      this.openSet.insert($$0);
      boolean $$7 = this.captureDebug.getAsBoolean();
      Set<Node> $$8 = (Set<Node>)($$7 ? new HashSet<>() : Set.of());
      int $$9 = 0;
      Set<Target> $$10 = Sets.newHashSetWithExpectedSize($$6.size());
      int $$11 = (int)(this.maxVisitedNodes * $$4);

      while (!this.openSet.isEmpty()) {
         if (++$$9 >= $$11) {
            break;
         }

         Node $$12 = this.openSet.pop();
         $$12.closed = true;

         for (Target $$13 : $$6) {
            if ($$12.distanceManhattan($$13) <= $$3) {
               $$13.setReached();
               $$10.add($$13);
            }
         }

         if (!$$10.isEmpty()) {
            break;
         }

         if ($$7) {
            $$8.add($$12);
         }

         if (!($$12.distanceTo($$0) >= $$2)) {
            int $$14 = this.nodeEvaluator.getNeighbors(this.neighbors, $$12);

            for (int $$15 = 0; $$15 < $$14; $$15++) {
               Node $$16 = this.neighbors[$$15];
               float $$17 = this.distance($$12, $$16);
               $$16.walkedDistance = $$12.walkedDistance + $$17;
               float $$18 = $$12.g + $$17 + $$16.costMalus;
               if ($$16.walkedDistance < $$2 && (!$$16.inOpenSet() || $$18 < $$16.g)) {
                  $$16.cameFrom = $$12;
                  $$16.g = $$18;
                  $$16.h = this.getBestH($$16, $$6) * 1.5F;
                  if ($$16.inOpenSet()) {
                     this.openSet.changeCost($$16, $$16.g + $$16.h);
                  } else {
                     $$16.f = $$16.g + $$16.h;
                     this.openSet.insert($$16);
                  }
               }
            }
         }
      }

      Optional<Path> $$19 = !$$10.isEmpty()
         ? $$10.stream().map($$1x -> this.reconstructPath($$1x.getBestNode(), $$1.get($$1x), true)).min(Comparator.comparingInt(Path::getNodeCount))
         : $$6.stream()
            .map($$1x -> this.reconstructPath($$1x.getBestNode(), $$1.get($$1x), false))
            .min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
      $$5.pop();
      if ($$19.isEmpty()) {
         return null;
      } else {
         Path $$20 = $$19.get();
         if ($$7) {
            $$20.setDebug(this.openSet.getHeap(), $$8.toArray(Node[]::new), $$6);
         }

         return $$20;
      }
   }

   protected float distance(Node $$0, Node $$1) {
      return $$0.distanceTo($$1);
   }

   private float getBestH(Node $$0, Set<Target> $$1) {
      float $$2 = Float.MAX_VALUE;

      for (Target $$3 : $$1) {
         float $$4 = $$0.distanceTo($$3);
         $$3.updateBest($$4, $$0);
         $$2 = Math.min($$4, $$2);
      }

      return $$2;
   }

   private Path reconstructPath(Node $$0, BlockPos $$1, boolean $$2) {
      List<Node> $$3 = Lists.newArrayList();
      Node $$4 = $$0;
      $$3.add(0, $$0);

      while ($$4.cameFrom != null) {
         $$4 = $$4.cameFrom;
         $$3.add(0, $$4);
      }

      return new Path($$3, $$1, $$2);
   }
}
