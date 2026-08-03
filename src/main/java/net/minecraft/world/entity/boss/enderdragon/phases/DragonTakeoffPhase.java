package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DragonTakeoffPhase extends AbstractDragonPhaseInstance {
   private boolean firstTick;
   @Nullable
   private Path currentPath;
   @Nullable
   private Vec3 targetLocation;

   public DragonTakeoffPhase(EnderDragon $$0) {
      super($$0);
   }

   @Override
   public void doServerTick(ServerLevel $$0) {
      if (!this.firstTick && this.currentPath != null) {
         BlockPos $$1 = $$0.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()));
         if (!$$1.closerToCenterThan(this.dragon.position(), 10.0)) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
         }
      } else {
         this.firstTick = false;
         this.findNewTarget();
      }
   }

   @Override
   public void begin() {
      this.firstTick = true;
      this.currentPath = null;
      this.targetLocation = null;
   }

   private void findNewTarget() {
      int $$0 = this.dragon.findClosestNode();
      Vec3 $$1 = this.dragon.getHeadLookVector(1.0F);
      int $$2 = this.dragon.findClosestNode(-$$1.x * 40.0, 105.0, -$$1.z * 40.0);
      if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
         $$2 %= 12;
         if ($$2 < 0) {
            $$2 += 12;
         }
      } else {
         $$2 -= 12;
         $$2 &= 7;
         $$2 += 12;
      }

      this.currentPath = this.dragon.findPath($$0, $$2, null);
      this.navigateToNextPathNode();
   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null) {
         this.currentPath.advance();
         if (!this.currentPath.isDone()) {
            Vec3i $$0 = this.currentPath.getNextNodePos();
            this.currentPath.advance();

            double $$1;
            do {
               $$1 = $$0.getY() + this.dragon.getRandom().nextFloat() * 20.0F;
            } while ($$1 < $$0.getY());

            this.targetLocation = new Vec3($$0.getX(), $$1, $$0.getZ());
         }
      }
   }

   @Nullable
   @Override
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   @Override
   public EnderDragonPhase<DragonTakeoffPhase> getPhase() {
      return EnderDragonPhase.TAKEOFF;
   }
}
