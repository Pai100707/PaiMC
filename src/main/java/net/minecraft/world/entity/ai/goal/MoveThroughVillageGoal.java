package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MoveThroughVillageGoal extends Goal {
   protected final net.minecraft.world.entity.PathfinderMob mob;
   private final double speedModifier;
   
   private Path path;
   private BlockPos poiPos;
   private final boolean onlyAtNight;
   private final List<BlockPos> visited = Lists.newArrayList();
   private final int distanceToPoi;
   private final BooleanSupplier canDealWithDoors;

   public MoveThroughVillageGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, boolean $$2, int $$3, BooleanSupplier $$4) {
      this.mob = $$0;
      this.speedModifier = $$1;
      this.onlyAtNight = $$2;
      this.distanceToPoi = $$3;
      this.canDealWithDoors = $$4;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      if (!GoalUtils.hasGroundPathNavigation($$0)) {
         throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
      }
   }

   @Override
   public boolean canUse() {
      if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
         return false;
      } else {
         this.updateVisited();
         if (this.onlyAtNight && this.mob.level().isBrightOutside()) {
            return false;
         } else {
            ServerLevel $$0 = (ServerLevel)this.mob.level();
            BlockPos $$1 = this.mob.blockPosition();
            if (!$$0.isCloseToVillage($$1, 6)) {
               return false;
            } else {
               Vec3 $$2 = LandRandomPos.getPos(
                  this.mob,
                  15,
                  7,
                  $$2x -> {
                     if (!$$0.isVillage($$2x)) {
                        return Double.NEGATIVE_INFINITY;
                     } else {
                        Optional<BlockPos> $$3x = $$0.getPoiManager()
                           .find($$0xx -> $$0xx.is(PoiTypeTags.VILLAGE), this::hasNotVisited, $$2x, 10, PoiManager.Occupancy.IS_OCCUPIED);
                        return $$3x.<Double>map($$1xx -> -$$1xx.distSqr($$1)).orElse(Double.NEGATIVE_INFINITY);
                     }
                  }
               );
               if ($$2 == null) {
                  return false;
               } else {
                  Optional<BlockPos> $$3 = $$0.getPoiManager()
                     .find($$0x -> $$0x.is(PoiTypeTags.VILLAGE), this::hasNotVisited, BlockPos.containing($$2), 10, PoiManager.Occupancy.IS_OCCUPIED);
                  if ($$3.isEmpty()) {
                     return false;
                  } else {
                     this.poiPos = $$3.get().immutable();
                     PathNavigation $$4 = this.mob.getNavigation();
                     $$4.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
                     this.path = $$4.createPath(this.poiPos, 0);
                     $$4.setCanOpenDoors(true);
                     if (this.path == null) {
                        Vec3 $$5 = DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(this.poiPos), (float) (Math.PI / 2));
                        if ($$5 == null) {
                           return false;
                        }

                        $$4.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
                        this.path = this.mob.getNavigation().createPath($$5.x, $$5.y, $$5.z, 0);
                        $$4.setCanOpenDoors(true);
                        if (this.path == null) {
                           return false;
                        }
                     }

                     for (int $$6 = 0; $$6 < this.path.getNodeCount(); $$6++) {
                        Node $$7 = this.path.getNode($$6);
                        BlockPos $$8 = new BlockPos($$7.x, $$7.y + 1, $$7.z);
                        if (DoorBlock.isWoodenDoor(this.mob.level(), $$8)) {
                           this.path = this.mob.getNavigation().createPath($$7.x, $$7.y, $$7.z, 0);
                           break;
                        }
                     }

                     return this.path != null;
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.mob.getNavigation().isDone() ? false : !this.poiPos.closerToCenterThan(this.mob.position(), this.mob.getBbWidth() + this.distanceToPoi);
   }

   @Override
   public void start() {
      this.mob.getNavigation().moveTo(this.path, this.speedModifier);
   }

   @Override
   public void stop() {
      if (this.mob.getNavigation().isDone() || this.poiPos.closerToCenterThan(this.mob.position(), this.distanceToPoi)) {
         this.visited.add(this.poiPos);
      }
   }

   private boolean hasNotVisited(BlockPos $$0) {
      for (BlockPos $$1 : this.visited) {
         if (Objects.equals($$0, $$1)) {
            return false;
         }
      }

      return true;
   }

   private void updateVisited() {
      if (this.visited.size() > 15) {
         this.visited.remove(0);
      }
   }
}
